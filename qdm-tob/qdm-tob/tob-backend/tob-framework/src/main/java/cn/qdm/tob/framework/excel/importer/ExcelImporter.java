package cn.qdm.tob.framework.excel.importer;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import cn.qdm.tob.framework.excel.converter.DictionaryConverter;
import cn.qdm.tob.framework.excel.converter.ExcelValueConverter;
import cn.qdm.tob.framework.excel.handler.ImportHandler;
import cn.qdm.tob.framework.excel.model.ExcelImportColumnMapping;
import cn.qdm.tob.framework.excel.model.SheetFormat;
import cn.qdm.tob.framework.util.AssertUtils;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.read.listener.ReadListener;
import org.apache.fesod.sheet.support.ExcelTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Excel 导入器
 * <p>
 * 流式 API 设计，支持链式调用配置，最终通过 {@link #readAll()} 或 {@link #readIterator()} 获取结果。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 一次性读取（中小数据规模）
 * List<OrderDTO> orders = ExcelImporter.create(file, OrderDTO.class)
 *     .sheet("Sheet1")
 *     .headerRow(0)
 *     .enableValidation(true)
 *     .readAll();
 *
 * // 流式读取（大数据规模）
 * Iterator<OrderDTO> iter = ExcelImporter.create(file, OrderDTO.class)
 *     .readIterator();
 * while (iter.hasNext()) {
 *     OrderDTO order = iter.next();
 *     // 逐行处理
 * }
 * }</pre>
 *
 * @param <T> 导入的实体类型
 */
public class ExcelImporter<T> {

    private static final Logger log = LoggerFactory.getLogger(ExcelImporter.class);

    /** EOF 标记 */
    private static final Object EOF = new Object();

    private final InputStream stream;
    private final Class<T> entityClass;
    private String sheetName;
    private int headerRow;
    private ImportHandler<T> handler;
    private SheetFormat format;
    private List<DescriptionProvider> descriptionProviders = List.of();

    private ExcelImporter(InputStream stream, Class<T> entityClass) {
        this.stream = Objects.requireNonNull(stream, "stream 不能为 null");
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass 不能为 null");
        this.format = SheetFormat.XLSX;
    }

    // ================================================================
    // 工厂方法
    // ================================================================

    /**
     * 从 InputStream 创建导入器
     */
    public static <T> ExcelImporter<T> create(InputStream stream, Class<T> entityClass) {
        return new ExcelImporter<>(stream, entityClass);
    }

    /**
     * 从 InputStream 创建导入器，显式指定文件格式
     */
    public static <T> ExcelImporter<T> create(InputStream stream, Class<T> entityClass, SheetFormat format) {
        AssertUtils.notNull(stream, "stream 不能为 null");
        ExcelImporter<T> importer = new ExcelImporter<>(stream, entityClass);
        importer.format = format;
        return importer;
    }

    /**
     * 从 MultipartFile 创建导入器，通过文件扩展名自动检测格式
     */
    public static <T> ExcelImporter<T> create(MultipartFile file, Class<T> entityClass) {
        AssertUtils.notNull(file, "file 不能为 null");

        SheetFormat format = SheetFormat.XLSX;
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".csv")) {
            format = SheetFormat.CSV;
        }

        try {
            ExcelImporter<T> importer = new ExcelImporter<>(file.getInputStream(), entityClass);
            importer.format = format;
            return importer;
        } catch (IOException e) {
            throw new RuntimeException("无法读取上传文件", e);
        }
    }

    // ================================================================
    // 链式配置
    // ================================================================

    /**
     * 设置要读取的工作表名称
     */
    public ExcelImporter<T> sheet(String sheetName) {
        this.sheetName = sheetName;
        return this;
    }

    /**
     * 设置表头行索引（0-based）
     */
    public ExcelImporter<T> headerRow(int rowIndex) {
        this.headerRow = rowIndex;
        return this;
    }

    /**
     * 注入 {@link DescriptionProvider} 列表，供 {@link DictionaryConverter} 使用
     */
    public ExcelImporter<T> descriptionProviders(List<DescriptionProvider> providers) {
        this.descriptionProviders = providers != null ? providers : List.of();
        return this;
    }

    /**
     * 设置行处理器
     */
    @SuppressWarnings("unchecked")
    public ExcelImporter<T> handler(ImportHandler<?> handler) {
        this.handler = (ImportHandler<T>) handler;
        return this;
    }

    // ================================================================
    // 读取方法
    // ================================================================

    /**
     * 一次性读取 Excel 并返回实体列表（适用于中小数据规模）
     */
    public List<T> readAll() {
        List<T> result = new ArrayList<>();
        int count = 0;
        Iterator<T> iter = readIterator();
        while (iter.hasNext()) {
            result.add(iter.next());
            count++;
        }
        if (handler != null) {
            handler.onAfterParse(result, count);
        }
        return result;
    }

    /**
     * 流式读取 Excel，返回惰性迭代器（适用于大数据规模）
     * <p>
     * 内部使用 BlockingQueue 桥接 fesod-sheet 的 push-based ReadListener 和 pull-based Iterator。
     * 在独立守护线程中执行 Excel 解析，迭代器从中逐行取数据。
     * </p>
     */
    public Iterator<T> readIterator() {
        // 第一步：构建列映射（分析类型元数据）
        List<ExcelImportColumnMapping> mappings = buildColumnMappings();

        // 第二步：创建阻塞队列桥接 push-based ReadListener 和 pull-based Iterator
        BlockingQueue<Object> queue = new LinkedBlockingQueue<>(256);

        ExcelTypeEnum excelType = (format == SheetFormat.CSV) ? ExcelTypeEnum.CSV : ExcelTypeEnum.XLSX;

        // 第三步：创建 ReadListener
        ReadListener<Map<Integer, ReadCellData<?>>> listener = new ReadListener<>() {

            /** 只解析一次表头 */
            private boolean headerParsed = false;
            /** 数据行计数器（不含表头行） */
            private int rowIdx = 0;
            /** 绑定的列映射（解析表头后生成） */
            private List<ExcelImportColumnMapping> boundMappings;

            @Override
            public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                if (headerParsed) return;

                // 将 fesod-sheet 的表头 Map<Integer, ReadCellData<?>> 转换为 Map<Integer, String>
                // key = 0-based 列索引，value = 表头文本
                Map<Integer, String> headerRow = new LinkedHashMap<>();
                for (Map.Entry<Integer, ReadCellData<?>> entry : headMap.entrySet()) {
                    ReadCellData<?> cellData = entry.getValue();
                    headerRow.put(entry.getKey(), cellData != null ? cellData.getStringValue() : null);
                }

                // 绑定 Excel 列与实体属性
                boundMappings = bindExcelColumns(mappings, headerRow);

                if (handler != null) {
                    String sn = sheetName != null && !sheetName.isBlank() ? sheetName : "Sheet1";
                    handler.onBeforeParse(sn, Collections.unmodifiableList(boundMappings));
                }
                headerParsed = true;
            }

            @Override
            public void invoke(Map<Integer, ReadCellData<?>> rowData, AnalysisContext context) {
                try {
                    T entity = mapRowToEntity(rowData, boundMappings, rowIdx);
                    if (entity != null) {
                        if (handler != null) {
                            Map<String, Object> cells = new LinkedHashMap<>();
                            for (ExcelImportColumnMapping m : boundMappings) {
                                ReadCellData<?> cell = rowData.get(m.getHeaderIndex());
                                cells.put(m.getPropertyName(), cell != null ? cell.getStringValue() : null);
                            }
                            handler.onRowRead(entity, rowIdx, cells);
                        }
                        queue.put(entity);
                        rowIdx++;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.warn("第 {} 行解析异常: {}", rowIdx + 1, e.getMessage());
                    try {
                        queue.put(new ReadException(e));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                try {
                    queue.put(EOF);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void onException(Exception exception, AnalysisContext context) throws Exception {
                log.error("Excel 解析异常", exception);
                try {
                    queue.put(new ReadException(exception));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                throw exception;
            }

            @Override
            public boolean hasNext(AnalysisContext context) {
                return true;
            }
        };

        // 第四步：在独立守护线程中执行同步阻塞的 doRead
        Thread readThread = new Thread(() -> {
            try {
                var builder = FesodSheet.read(stream, listener)
                        .excelType(excelType)
                        .headRowNumber(headerRow > 0 ? headerRow + 1 : 1) // 1-based
                        .ignoreEmptyRow(true);

                if (sheetName != null && !sheetName.isBlank()) {
                    builder.sheet(sheetName).doRead();
                } else {
                    builder.sheet(0).doRead();
                }

                // 确保 EOF 已发送
                if (!queue.contains(EOF)) {
                    queue.put(EOF);
                }
            } catch (Exception e) {
                log.error("Excel 读取失败", e);
                try {
                    queue.put(new ReadException(e));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "excel-reader");
        readThread.setDaemon(true);
        readThread.start();

        // 第五步：返回包装的 Iterator
        return new Iterator<>() {
            private Object nextItem;
            private boolean done;

            @Override
            public boolean hasNext() {
                if (done) return false;
                if (nextItem != null) return true;

                try {
                    nextItem = queue.poll(30, TimeUnit.SECONDS);
                    if (nextItem == null) {
                        if (!readThread.isAlive()) {
                            done = true;
                            return false;
                        }
                        return hasNext(); // 重试
                    }
                    if (nextItem == EOF) {
                        done = true;
                        nextItem = null;
                        return false;
                    }
                    if (nextItem instanceof ReadException re) {
                        done = true;
                        nextItem = null;
                        throw new RuntimeException("Excel 读取失败", re.cause());
                    }
                    return true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    done = true;
                    return false;
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T result = (T) nextItem;
                nextItem = null;
                return result;
            }
        };
    }

    // ================================================================
    // 内部实现
    // ================================================================

    /** 读取异常包装 */
    private record ReadException(Exception cause) {
    }

    /**
     * 基于模板类构建属性映射（不依赖 Excel 文件，仅分析类型元数据）
     */
    private List<ExcelImportColumnMapping> buildColumnMappings() {
        List<Field> fields = getAnnotatedFields(entityClass);

        AssertUtils.notEmpty(fields,
                "类型 \"" + entityClass.getName() + "\" 不包含任何 @ExcelColumn 属性");

        List<ExcelImportColumnMapping> mappings = new ArrayList<>();
        for (Field field : fields) {
            ExcelColumn attr = field.getAnnotation(ExcelColumn.class);

            // 预创建 converter 实例
            ExcelValueConverter converterInstance = null;
            Class<? extends ExcelValueConverter> converterType = attr.converterType();

            // 未指定 converterType 但属性上有 @Description，自动使用 DictionaryConverter
            if (converterType == ExcelValueConverter.class
                    && field.getAnnotation(Description.class) != null) {
                converterType = DictionaryConverter.class;
            }

            if (converterType != ExcelValueConverter.class) {
                try {
                    if (converterType == DictionaryConverter.class) {
                        converterInstance = new DictionaryConverter(descriptionProviders);
                    } else {
                        converterInstance = converterType.getDeclaredConstructor().newInstance();
                    }
                } catch (Exception e) {
                    log.debug("转换器实例化失败: {}", converterType.getName(), e);
                }
            }

            // 此时尚未与 Excel 列绑定
            mappings.add(ExcelImportColumnMapping.builder()
                    .headerName(null)
                    .headerIndex(null)
                    .propertyName(field.getName())
                    .field(field)
                    .excelColumnAttr(attr)
                    .converterInstance(converterInstance)
                    .build());
        }

        return mappings;
    }

    /**
     * 将预创建的属性映射对象与 Excel 表头列绑定
     *
     * @param mappings  实体属性的列映射（未绑定）
     * @param headerRow Excel 表头行，key = fesod-sheet 0-based 列索引，value = 表头文本
     * @return 绑定后的映射列表
     */
    private List<ExcelImportColumnMapping> bindExcelColumns(
            List<ExcelImportColumnMapping> mappings,
            Map<Integer, String> headerRow) {

        List<ExcelImportColumnMapping> bound = new ArrayList<>();

        for (ExcelImportColumnMapping mapping : mappings) {
            ExcelColumn attr = mapping.getExcelColumnAttr();
            Set<String> titles = attr.title().length > 0
                    ? Set.of(attr.title())
                    : Set.of(mapping.getPropertyName());

            for (Map.Entry<Integer, String> kv : headerRow.entrySet()) {
                String head = kv.getValue();
                if (head != null && !head.isBlank() && titles.contains(head.trim())) {
                    mapping.setHeaderName(head.trim());
                    mapping.setHeaderIndex(kv.getKey()); // 直接使用 fesod-sheet 的列索引
                    bound.add(mapping);
                    break;
                }
            }
        }

        AssertUtils.notEmpty(bound,
                "Excel 表头与 @ExcelColumn 注解不匹配，请检查表头名称是否正确");
        return bound;
    }

    /**
     * 将 Excel 行数据映射为实体对象
     *
     * @param rowData  fesod-sheet 的行数据，key = 列索引
     * @param mappings 绑定的列映射
     * @param rowIndex 数据行索引（0-based，用于错误提示）
     */
    private T mapRowToEntity(Map<Integer, ReadCellData<?>> rowData,
                             List<ExcelImportColumnMapping> mappings,
                             int rowIndex) {
        // 检查是否为空行
        boolean allNull = mappings.stream().allMatch(m -> {
            ReadCellData<?> cell = rowData.get(m.getHeaderIndex());
            return cell == null || cell.getStringValue() == null || cell.getStringValue().isBlank();
        });
        if (allNull) return null;

        T entity;
        try {
            entity = entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法实例化类型: " + entityClass.getName(), e);
        }

        for (ExcelImportColumnMapping mapping : mappings) {
            ReadCellData<?> cellData = rowData.get(mapping.getHeaderIndex());
            if (cellData == null) continue;

            try {
                Object rawValue = cellData.getStringValue();
                if (rawValue == null || rawValue.toString().isBlank()) continue;

                Object convertedValue = convertCellValue(rawValue, mapping);
                mapping.getField().setAccessible(true);
                mapping.getField().set(entity, convertedValue);
            } catch (Exception e) {
                log.debug("第 {} 行属性 {} 值转换失败: {}", rowIndex + 1, mapping.getPropertyName(), e.getMessage());
            }
        }

        return entity;
    }

    /**
     * 单元格值类型转换：适配实体属性类型，并应用字典反向转换
     */
    private Object convertCellValue(Object rawValue, ExcelImportColumnMapping mapping) {
        Field field = mapping.getField();
        Class<?> targetType = field.getType();

        // 使用预创建的 converter 实例进行反向转换（显示文本 → 编码）
        if (mapping.getConverterInstance() != null) {
            try {
                rawValue = mapping.getConverterInstance().revert(rawValue, field);
            } catch (UnsupportedOperationException ignored) {
                // 转换器不支持 revert，继续走默认逻辑
            }
        }

        String strValue = rawValue != null ? rawValue.toString() : null;
        if (strValue == null || strValue.isBlank()) {
            return null;
        }

        // 日期类型：优先使用 ExcelColumn.format 解析
        String format = mapping.getExcelColumnAttr().format();
        if (!format.isBlank()) {
            if (targetType == LocalDateTime.class || targetType == Date.class) {
                return convertToLocalDateTime(strValue, format);
            }
            if (targetType == LocalDate.class) {
                return convertToLocalDate(strValue, format);
            }
            if (targetType == LocalTime.class) {
                return convertToLocalTime(strValue, format);
            }
        }

        // 字符串
        if (targetType == String.class) {
            return strValue;
        }

        // 基础类型转换（自动 trim）
        String trimmed = strValue.trim();
        try {
            if (targetType == Integer.class || targetType == int.class) return Integer.valueOf(trimmed);
            if (targetType == Long.class || targetType == long.class) return Long.valueOf(trimmed);
            if (targetType == Double.class || targetType == double.class) return Double.valueOf(trimmed);
            if (targetType == Float.class || targetType == float.class) return Float.valueOf(trimmed);
            if (targetType == Boolean.class || targetType == boolean.class) return Boolean.valueOf(trimmed);
            if (targetType == BigDecimal.class) return new BigDecimal(trimmed);
            if (targetType == Short.class || targetType == short.class) return Short.valueOf(trimmed);
            if (targetType == Byte.class || targetType == byte.class) return Byte.valueOf(trimmed);
        } catch (NumberFormatException e) {
            log.debug("数值转换失败: {} -> {}", strValue, targetType.getSimpleName());
            return null;
        }

        // 枚举类型
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumValue = Enum.valueOf((Class<Enum>) targetType, trimmed);
            return enumValue;
        }

        return rawValue;
    }

    // ================================================================
    // 日期解析辅助方法
    // ================================================================

    private static LocalDateTime convertToLocalDateTime(String str, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.parse(str, dtf);
    }

    private static LocalDate convertToLocalDate(String str, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(str, dtf);
    }

    private static LocalTime convertToLocalTime(String str, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return LocalTime.parse(str, dtf);
    }

    // ================================================================
    // 反射辅助方法
    // ================================================================

    /**
     * 获取类型中标注了 @ExcelColumn 的所有非静态字段，按 index 排序
     */
    public static List<Field> getAnnotatedFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.isAnnotationPresent(ExcelColumn.class)) {
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        fields.sort(Comparator.comparingInt(f -> {
            ExcelColumn a = f.getAnnotation(ExcelColumn.class);
            return a != null ? a.index() : Integer.MAX_VALUE;
        }));
        return fields;
    }
}
