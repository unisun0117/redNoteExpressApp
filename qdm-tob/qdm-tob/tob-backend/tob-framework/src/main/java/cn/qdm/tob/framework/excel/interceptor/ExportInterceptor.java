package cn.qdm.tob.framework.excel.interceptor;

import cn.qdm.tob.framework.description.Description;
import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.framework.excel.annotation.ExcelColumn;
import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.excel.converter.DictionaryConverter;
import cn.qdm.tob.framework.excel.converter.ExcelValueConverter;
import cn.qdm.tob.framework.excel.handler.ResponseExportHandler;
import cn.qdm.tob.framework.excel.handler.ExportHandler;
import cn.qdm.tob.framework.excel.model.ExcelColumnInfo;
import cn.qdm.tob.framework.excel.model.SheetFormat;
import cn.qdm.tob.framework.excel.model.ExportMetadata;
import cn.qdm.tob.framework.util.AssertUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.support.ExcelTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 同步导出 Excel 拦截器
 * <p>
 * 拦截带有 {@link Exportable} 注解且请求头包含 {@code Action: export} 的请求，
 * 通过反射调用 Controller 方法获取数据，使用 Pipe 模式（PipedOutputStream/PipedInputStream）
 * 流式生成 Excel 文件并通过 {@link ExportHandler} 输出。
 * </p>
 *
 * <h3>支持的数据源</h3>
 * <ul>
 *   <li>{@link Iterator Iterator&lt;T&gt;} / {@link Iterable Iterable&lt;T&gt;} — 流式导出</li>
 *   <li>{@link ResultSet} — 数据库流式导出</li>
 *   <li>{@link List List&lt;T&gt;} — 一次性全部导出</li>
 * </ul>
 */
public class ExportInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ExportInterceptor.class);

    /** 元数据缓存（最多 200 条，30 分钟过期） */
    private static final Cache<Method, ExportMetadata> METADATA_CACHE = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    private List<DescriptionProvider> descriptionProviders = List.of();

    public void setDescriptionProviders(List<DescriptionProvider> providers) {
        this.descriptionProviders = providers != null ? providers : List.of();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Exportable attr = handlerMethod.getMethodAnnotation(Exportable.class);
        if (attr == null) {
            return true;
        }

        // 检查 Action 请求头
        String action = request.getHeader("Action");
        if (!"export".equalsIgnoreCase(action)) {
            return true;
        }

        try {
            exportAndHandle(request, response, attr, handlerMethod);
        } catch (Exception e) {
            log.error("同步导出 Excel 失败", e);
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write(
                    "{\"code\":500,\"msg\":\"同步导出Excel失败，" + escapeJson(e.getMessage()) + "\",\"data\":null}");
        }
        return false; // 不继续执行 Controller
    }

    private void exportAndHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Exportable attr,
                                 HandlerMethod handlerMethod) throws Exception {

        // 1. 获取/缓存元数据
        Method method = handlerMethod.getMethod();
        ExportMetadata metadata = METADATA_CACHE.get(method, m -> buildMetadata(m, attr));

        // 2. 创建导出处理器
        ExportHandler handler;
        if (attr.handlerType() == ResponseExportHandler.class) {
            handler = new ResponseExportHandler();
        } else {
            handler = attr.handlerType().getDeclaredConstructor().newInstance();
        }

        // 3. 使用 Pipe 模式：生产者在独立线程写 Excel，消费者读 Pipe
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos, 8192); // 8KB 缓冲

        SheetFormat format = attr.format();
        ExcelTypeEnum excelType = (format == SheetFormat.CSV) ? ExcelTypeEnum.CSV : ExcelTypeEnum.XLSX;

        // 生产者：写 Excel 到 PipedOutputStream
        CompletableFuture<Void> writeFuture = CompletableFuture.runAsync(() -> {
            try {
                Iterator<Map<String, Object>> dataStream = getDataAsStream(request, attr, handlerMethod, metadata);
                writeExcel(pos, dataStream, metadata, format);
            } catch (Exception e) {
                log.error("Excel 写入失败", e);
                try {
                    pos.close();
                } catch (Exception ignored) {
                }
                throw new RuntimeException(e);
            }
        });

        // 消费者：从 PipedInputStream 读取并交给 handler
        try {
            handler.handle(request, response, pis, attr, metadata);
        } finally {
            // 等待生产者完成
            try {
                writeFuture.get();
            } catch (Exception e) {
                log.warn("Excel 写入线程异常", e);
            }
        }
    }

    /**
     * 流式获取导出数据，支持三种数据源：Iterator / Iterable / List
     */
    private Iterator<Map<String, Object>> getDataAsStream(
            HttpServletRequest request,
            Exportable attr,
            HandlerMethod handlerMethod,
            ExportMetadata metadata) throws Exception {

        Set<String> columnNames = new HashSet<>();
        for (ExcelColumnInfo col : metadata.getColumns()) {
            columnNames.add(col.getName());
        }

        // 调用 Controller 方法
        Object result = invokeControllerMethod(request, handlerMethod, metadata);

        if (result == null) {
            // 返回空数据集时也输出一行表头
            return List.<Map<String, Object>>of(createEmptyRow(columnNames)).iterator();
        }

        // 情况 1：Iterator<T> — 流式
        if (result instanceof Iterator<?> iter) {
            return new Iterator<>() {
                private boolean hasItems = false;

                @Override
                public boolean hasNext() {
                    boolean next = iter.hasNext();
                    if (!next && !hasItems) {
                        // 空数据集
                        pendingEmptyRow = createEmptyRow(columnNames);
                        return false; // will handle specially
                    }
                    return next;
                }

                @Override
                @SuppressWarnings("unchecked")
                public Map<String, Object> next() {
                    Object item = iter.next();
                    hasItems = true;
                    return extractColumns(item, columnNames, metadata, request);
                }

                private Map<String, Object> pendingEmptyRow;
            };
        }

        // 情况 2：Iterable<T> — 转为迭代器
        if (result instanceof Iterable<?> iterable) {
            var it = iterable.iterator();
            return new Iterator<>() {
                private boolean hasItems = false;
                private boolean emptySent = false;

                @Override
                public boolean hasNext() {
                    if (emptySent) return false;
                    boolean next = it.hasNext();
                    if (!next && !hasItems) {
                        emptySent = true;
                        return true; // 返回空行
                    }
                    return next;
                }

                @Override
                public Map<String, Object> next() {
                    if (emptySent) {
                        return createEmptyRow(columnNames);
                    }
                    Object item = it.next();
                    hasItems = true;
                    return extractColumns(item, columnNames, metadata, request);
                }
            };
        }

        // 情况 3：ResultSet — 数据库流式导出
        if (result instanceof ResultSet rs) {
            return new Iterator<>() {
                private boolean hasRows = false;
                private boolean emptySent = false;
                private Map<String, ExcelColumnInfo> columnMetaLookup = new LinkedHashMap<>();

                {
                    for (ExcelColumnInfo col : metadata.getColumns()) {
                        columnMetaLookup.put(col.getName(), col);
                    }
                }

                @Override
                public boolean hasNext() {
                    try {
                        if (emptySent) return false;
                        boolean next = rs.next();
                        if (!next && !hasRows) {
                            emptySent = true;
                            return true;
                        }
                        return next;
                    } catch (Exception e) {
                        throw new RuntimeException("ResultSet 遍历异常", e);
                    }
                }

                @Override
                public Map<String, Object> next() {
                    try {
                        if (emptySent) return createEmptyRow(columnNames);
                        hasRows = true;
                        return extractColumnsFromResultSet(rs, columnNames, columnMetaLookup);
                    } catch (Exception e) {
                        throw new RuntimeException("ResultSet 读取异常", e);
                    }
                }
            };
        }

        // 情况 4：List / Collection — 一次性
        if (result instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                return List.<Map<String, Object>>of(createEmptyRow(columnNames)).iterator();
            }
            return collection.stream()
                    .map(item -> extractColumns(item, columnNames, metadata, request))
                    .iterator();
        }

        // fallback：包装为单元素列表
        return List.of(extractColumns(result, columnNames, metadata, request)).iterator();
    }

    /**
     * 写入 Excel 文件
     */
    private void writeExcel(OutputStream outputStream,
                            Iterator<Map<String, Object>> dataStream,
                            ExportMetadata metadata,
                            SheetFormat format) {

        // 构建表头列表（按 index 排序）
        List<ExcelColumnInfo> sortedColumns = metadata.getColumns().stream()
                .sorted(Comparator.comparingInt(ExcelColumnInfo::getIndex))
                .toList();

        List<String> headers = sortedColumns.stream()
                .map(c -> c.getTitle() != null && !c.getTitle().isBlank() ? c.getTitle() : c.getName())
                .toList();

        // 将 Iterator 转换为 List<List<Object>>
        List<List<Object>> rows = new ArrayList<>();
        while (dataStream.hasNext()) {
            Map<String, Object> row = dataStream.next();
            List<Object> values = new ArrayList<>();
            for (ExcelColumnInfo col : sortedColumns) {
                values.add(row.getOrDefault(col.getName(), null));
            }
            rows.add(values);
        }

        // 使用 fesod-sheet 写入
        ExcelTypeEnum excelType = (format == SheetFormat.CSV) ? ExcelTypeEnum.CSV : ExcelTypeEnum.XLSX;

        FesodSheet.write(outputStream)
                .excelType(excelType)
                .head(List.of(headers))
                .sheet(0)
                .doWrite(rows);
    }

    /**
     * 从实体对象提取列值
     */
    private Map<String, Object> extractColumns(Object item,
                                               Set<String> columnNames,
                                               ExportMetadata metadata,
                                               HttpServletRequest request) {
        Map<String, Object> dict = new LinkedHashMap<>();
        Class<?> templateType = metadata.getTemplateType();
        Field[] fields = templateType.getDeclaredFields();

        for (Field field : fields) {
            if (columnNames.contains(field.getName())) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(item);

                    // 应用值转换和格式化
                    ExcelColumn attr = field.getAnnotation(ExcelColumn.class);
                    if (attr != null) {
                        value = applyValueConversion(value, attr, field);
                    }

                    dict.put(field.getName(), value);
                } catch (Exception e) {
                    dict.put(field.getName(), null);
                }
            }
        }
        return dict;
    }

    /**
     * 从 ResultSet 提取列值
     */
    private Map<String, Object> extractColumnsFromResultSet(
            ResultSet rs,
            Set<String> columnNames,
            Map<String, ExcelColumnInfo> columnMetaLookup) throws Exception {

        Map<String, Object> dict = new LinkedHashMap<>();
        int fieldCount = rs.getMetaData().getColumnCount();

        for (int i = 1; i <= fieldCount; i++) {
            String columnName = rs.getMetaData().getColumnLabel(i);
            if (columnNames.contains(columnName)) {
                Object value = rs.getObject(i);
                if (rs.wasNull()) value = null;

                ExcelColumnInfo columnMeta = columnMetaLookup.get(columnName);
                if (columnMeta != null && value != null) {
                    value = applyValueConversion(value, columnMeta);
                }

                dict.put(columnName, value);
            }
        }
        return dict;
    }

    /**
     * 创建空行（仅含列名，值为 null）
     */
    private Map<String, Object> createEmptyRow(Set<String> columnNames) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (String name : columnNames) {
            row.put(name, null);
        }
        return row;
    }

    /**
     * 应用值转换和格式化（基于 ExcelColumnInfo）
     */
    private Object applyValueConversion(Object value, ExcelColumnInfo columnMeta) {
        if (value == null) return null;

        // 转换器
        if (columnMeta.getConverterType() != null
                && columnMeta.getConverterType() != ExcelValueConverter.class) {
            try {
                ExcelValueConverter converter = createConverter(columnMeta.getConverterType());
                value = converter.convert(value, null);
            } catch (Exception e) {
                log.debug("值转换器应用失败: {}", columnMeta.getConverterType().getName(), e);
            }
        }

        // 格式化
        if (columnMeta.getFormat() != null && !columnMeta.getFormat().isBlank() && value != null) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(columnMeta.getFormat());
                if (value instanceof LocalDateTime ldt) value = ldt.format(dtf);
                else if (value instanceof LocalDate ld) value = ld.format(dtf);
                else if (value instanceof LocalTime lt) value = lt.format(dtf);
            } catch (Exception e) {
                log.debug("格式化应用失败: {}", columnMeta.getFormat(), e);
            }
        }

        return value;
    }

    /**
     * 应用值转换和格式化（基于 @ExcelColumn 注解）
     */
    private Object applyValueConversion(Object value, ExcelColumn attr, Field field) {
        if (value == null) return null;

        // 先应用自定义转换器
        Class<? extends ExcelValueConverter> converterType = attr.converterType();

        // 未指定 ConverterType 但属性上有 @Description，自动使用 DictionaryConverter
        if (converterType == ExcelValueConverter.class
                && field.getAnnotation(Description.class) != null) {
            converterType = DictionaryConverter.class;
        }

        if (converterType != ExcelValueConverter.class) {
            try {
                ExcelValueConverter converter = createConverter(converterType);
                value = converter.convert(value, field);
            } catch (Exception e) {
                log.debug("值转换器应用失败: {}", converterType.getName(), e);
            }
        }

        // 再应用格式化
        String format = attr.format();
        if (!format.isBlank() && value != null) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
                if (value instanceof LocalDateTime ldt) value = ldt.format(dtf);
                else if (value instanceof LocalDate ld) value = ld.format(dtf);
                else if (value instanceof LocalTime lt) value = lt.format(dtf);
            } catch (Exception e) {
                log.debug("格式化应用失败: {}", format, e);
            }
        }

        return value;
    }

    /**
     * 通过反射调用 Controller 方法
     */
    private Object invokeControllerMethod(HttpServletRequest request,
                                          HandlerMethod handlerMethod,
                                          ExportMetadata metadata) throws Exception {

        // 获取 Controller 实例
        Object controller = handlerMethod.getBean();
        Method method = metadata.getMethodInfo();

        // 准备方法参数
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            var param = parameters[i];
            Class<?> paramType = param.getType();

            if (paramType == HttpServletRequest.class) {
                args[i] = request;
            } else if (paramType == HttpServletResponse.class) {
                args[i] = null; // 导出时不需要 response
            } else if (isSimpleType(paramType)) {
                // 简单类型从 QueryString 获取
                String val = request.getParameter(param.getName());
                if (val != null) {
                    args[i] = convertSimpleValue(val, paramType);
                }
            } else {
                // 复杂类型：创建实例并从请求参数绑定
                try {
                    Object paramInstance = paramType.getDeclaredConstructor().newInstance();
                    bindFromRequest(request, paramInstance);
                    args[i] = paramInstance;
                } catch (Exception e) {
                    args[i] = null;
                }
            }
        }

        return method.invoke(controller, args);
    }

    /**
     * 是否简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type == LocalTime.class
                || type == Date.class;
    }

    /**
     * 简单值转换
     */
    private Object convertSimpleValue(String val, Class<?> targetType) {
        if (val == null || val.isBlank()) return null;
        if (targetType == String.class) return val;
        if (targetType == Integer.class || targetType == int.class) return Integer.valueOf(val);
        if (targetType == Long.class || targetType == long.class) return Long.valueOf(val);
        if (targetType == Double.class || targetType == double.class) return Double.valueOf(val);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.valueOf(val);
        if (targetType == LocalDate.class) return LocalDate.parse(val);
        if (targetType == LocalDateTime.class) return LocalDateTime.parse(val);
        return val;
    }

    /**
     * 从请求参数绑定到对象
     */
    private void bindFromRequest(HttpServletRequest request, Object instance) {
        Class<?> type = instance.getClass();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (!field.canAccess(instance)) field.setAccessible(true);

            String val = request.getParameter(field.getName());
            if (val != null) {
                try {
                    Object converted = convertSimpleValue(val, field.getType());
                    field.set(instance, converted);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 构建导出元数据（缓存用）
     */
    private ExportMetadata buildMetadata(Method method, Exportable attr) {
        // 解析模板类型
        Class<?> templateType = attr.templateType();
        if (templateType == Object.class) {
            templateType = resolveReturnTemplateType(method, attr.dataPath());
        }
        AssertUtils.notNull(templateType,
                "无法从方法 \"" + method.getName() + "\" 的返回值类型解析 Excel 导出模板类型");

        // 解析列定义
        List<ExcelColumnInfo> columns = resolveColumns(templateType);
        AssertUtils.notEmpty(columns,
                "类型 \"" + templateType.getName() + "\" 不包含任何 @ExcelColumn 属性");

        return ExportMetadata.builder()
                .columns(columns)
                .templateType(templateType)
                .methodInfo(method)
                .controllerType(method.getDeclaringClass())
                .controllerName(method.getDeclaringClass().getSimpleName())
                .actionName(method.getName())
                .build();
    }

    /**
     * 从 Controller 方法返回值类型解析模板类型
     * <p>
     * 例如返回 {@code ResponseResult<PageResult<OrderDTO>>}，dataPath 为 {@code "data.list"}，
     * 则先解出 {@code ResponseResult<PageResult<OrderDTO>>}，再沿路径取 {@code data} → {@code list}
     * 得到 {@code OrderDTO}。
     * </p>
     */
    private Class<?> resolveReturnTemplateType(Method method, String dataPath) {
        Type returnType = method.getGenericReturnType();

        // 解泛型包裹：如果外层是参数化类型，取第一个类型参数
        // 例如 ResponseResult<PageResult<OrderDTO>> → PageResult<OrderDTO>
        Class<?> rawClass = getRawClass(returnType);

        // 沿 dataPath 导航
        String[] parts = dataPath == null || dataPath.isBlank()
                ? new String[0] : dataPath.split("\\.");

        Class<?> current = rawClass;
        for (String part : parts) {
            if (current == null) break;
            try {
                Field field = findField(current, part);
                if (field != null) {
                    current = field.getType();
                    // 如果是参数化类型（如 List<OrderDTO>），取元素类型
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType pt) {
                        Type[] args = pt.getActualTypeArguments();
                        if (args.length > 0 && args[0] instanceof Class<?>) {
                            current = (Class<?>) args[0];
                        }
                    }
                } else {
                    current = null;
                }
            } catch (Exception e) {
                current = null;
            }
        }

        return current;
    }

    private Class<?> getRawClass(Type type) {
        if (type instanceof Class<?> clazz) return clazz;
        if (type instanceof ParameterizedType pt) return (Class<?>) pt.getRawType();
        return null;
    }

    private Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 解析模板类型的 @ExcelColumn 列定义
     */
    private List<ExcelColumnInfo> resolveColumns(Class<?> templateType) {
        List<Field> fields = cn.qdm.tob.framework.excel.importer.ExcelImporter.getAnnotatedFields(templateType);
        List<ExcelColumnInfo> columns = new ArrayList<>();

        for (Field field : fields) {
            ExcelColumn attr = field.getAnnotation(ExcelColumn.class);

            // 确定 converter 类型
            Class<? extends ExcelValueConverter> converterType = attr.converterType();
            if (converterType == ExcelValueConverter.class
                    && field.getAnnotation(Description.class) != null) {
                converterType = DictionaryConverter.class;
            }

            columns.add(ExcelColumnInfo.builder()
                    .name(field.getName())
                    .title(attr.title().length > 0 ? attr.title()[0] : field.getName())
                    .index(attr.index())
                    .width(attr.width())
                    .format(attr.format())
                    .converterType(converterType != ExcelValueConverter.class ? converterType : null)
                    .build());
        }

        return columns;
    }

    /**
     * 创建 converter 实例，DictionaryConverter 需要注入 providers
     */
    private ExcelValueConverter createConverter(Class<? extends ExcelValueConverter> type) throws Exception {
        if (type == DictionaryConverter.class) {
            return new DictionaryConverter(descriptionProviders);
        }
        return type.getDeclaredConstructor().newInstance();
    }

    /**
     * JSON 字符串转义
     */
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
