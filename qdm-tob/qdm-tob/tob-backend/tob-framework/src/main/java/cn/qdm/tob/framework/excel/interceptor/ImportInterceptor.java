package cn.qdm.tob.framework.excel.interceptor;

import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.framework.excel.annotation.Importable;
import cn.qdm.tob.framework.excel.handler.ImportHandler;
import cn.qdm.tob.framework.excel.importer.ExcelImporter;
import cn.qdm.tob.framework.util.AssertUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Excel 导入拦截器
 * <p>
 * 拦截带有 {@link Importable} 注解的 Controller 方法，自动解析上传的 Excel/CSV 文件，
 * 并将解析结果存入 {@code request.setAttribute(IMPORT_RESULT_KEY, result)} 供参数解析器使用。
 * </p>
 *
 * <h3>工作流程</h3>
 * <ol>
 *   <li>检查 HandlerMethod 是否有 @ExcelImportable 注解</li>
 *   <li>从 multipart/form-data 中获取上传文件</li>
 *   <li>校验文件类型和大小</li>
 *   <li>查找方法中的导入参数（{@code List<T>} 或 {@code Iterator<T>}）</li>
 *   <li>创建 ExcelImporter 并解析文件</li>
 *   <li>流式模式：调用 readIterator() 存入 request attribute</li>
 *   <li>批量模式：调用 readAll() 存入 request attribute</li>
 * </ol>
 */
public class ImportInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ImportInterceptor.class);

    private List<DescriptionProvider> descriptionProviders = List.of();

    public void setDescriptionProviders(List<DescriptionProvider> providers) {
        this.descriptionProviders = providers != null ? providers : List.of();
    }

    /** 在 HttpServletRequest attribute 中存储导入结果的 key */
    public static final String IMPORT_RESULT_KEY = "__ExcelImportResult";

    /** 支持的文件 Content-Type */
    private static final Set<String> ACCEPT_CONTENT_TYPES = Set.of(
            "text/csv",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/octet-stream"
    );

    /** 最大文件大小：80MB */
    private static final long MAX_FILE_SIZE = 80 * 1024 * 1024;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Importable attr = handlerMethod.getMethodAnnotation(Importable.class);
        if (attr == null) {
            return true;
        }

        try {
            processImport(request, attr, handlerMethod);
            return true;
        } catch (Exception e) {
            log.error("Excel 导入失败", e);
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(200);
            response.getWriter().write(
                    "{\"code\":400,\"msg\":\"" + escapeJson(e.getMessage()) + "\",\"data\":null}");
            return false;
        }
    }

    private void processImport(HttpServletRequest request,
                               Importable attr,
                               HandlerMethod handlerMethod) throws Exception {
        // 1. 获取上传文件
        AssertUtils.isTrue(request.getContentType() != null
                        && request.getContentType().toLowerCase().startsWith("multipart/form-data"),
                "请求必须是 multipart/form-data 格式");

        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            throw new IllegalArgumentException("无法解析 multipart 请求");
        }

        MultipartFile file = multipartRequest.getFile(attr.fileFieldName());
        AssertUtils.isTrue(file != null && !file.isEmpty(),
                "未找到上传文件，字段名: " + attr.fileFieldName());
        AssertUtils.isTrue(file.getSize() <= MAX_FILE_SIZE,
                "文件大小超过限制 (最大 " + MAX_FILE_SIZE / 1024 / 1024 + "MB)");

        // 2. 校验文件类型
        String contentType = file.getContentType();
        AssertUtils.isTrue(contentType == null || ACCEPT_CONTENT_TYPES.contains(contentType.toLowerCase()),
                "不支持的文件类型: " + contentType + "，仅支持 Excel (.xlsx) 或 CSV (.csv)");

        // 3. 查找 Controller 方法中唯一的导入参数（List<T> 或 Iterator<T>）
        Method method = handlerMethod.getMethod();
        ImportParamInfo importParam = findImportParameter(method);
        AssertUtils.notNull(importParam,
                "Controller 方法 \"" + method.getName() + "\" 必须有且仅有一个 List<T> 或 Iterator<T> 参数");

        // 4. 创建导入器
        ExcelImporter<?> importer = ExcelImporter.create(file, importParam.elementType)
                .descriptionProviders(descriptionProviders);

        if (!attr.sheetName().isBlank()) {
            importer.sheet(attr.sheetName());
        }
        if (attr.headerRowIndex() != 0) {
            importer.headerRow(attr.headerRowIndex());
        }
        // handler 处理
        Class<?> handlerType = attr.handlerType();
        if (handlerType != Void.class) {
            @SuppressWarnings("rawtypes")
            ImportHandler handler = (ImportHandler) handlerType.getDeclaredConstructor().newInstance();
            importer.handler(handler);
        }

        // 5. 读取并存入 request attribute
        if (importParam.isStreaming) {
            // 流式模式：IAsyncEnumerable<T> → Iterator<T>
            request.setAttribute(IMPORT_RESULT_KEY, importer.readIterator());
        } else {
            // 批量模式：List<T>
            request.setAttribute(IMPORT_RESULT_KEY, importer.readAll());
        }

        log.info("Excel 导入成功: 文件={}, 类型={}", file.getOriginalFilename(),
                importParam.elementType.getSimpleName());
    }

    /**
     * 查找方法中唯一的导入参数
     */
    private ImportParamInfo findImportParameter(Method method) {
        ImportParamInfo found = null;
        int count = 0;

        for (var param : method.getParameters()) {
            ImportParamInfo info = resolveImportParam(param.getParameterizedType());
            if (info != null) {
                found = info;
                count++;
            }
        }

        return count == 1 ? found : null;
    }

    /**
     * 解析参数是否为导入类型（List<T> / Collection<T> / Iterator<T>）
     */
    private ImportParamInfo resolveImportParam(Type type) {
        if (!(type instanceof java.lang.reflect.ParameterizedType paramType)) {
            return null;
        }

        Type rawType = paramType.getRawType();
        boolean isCollection = rawType == List.class
                || rawType == Collection.class
                || rawType == Iterable.class;
        boolean isIterator = rawType == Iterator.class;

        if (!isCollection && !isIterator) {
            // 检查是否实现了 Collection 接口
            if (rawType instanceof Class<?> clazz) {
                for (Type iface : clazz.getGenericInterfaces()) {
                    if (iface instanceof ParameterizedType pt
                            && pt.getRawType() == Collection.class) {
                        isCollection = true;
                        break;
                    }
                }
            }
        }

        if (!isCollection && !isIterator) {
            return null;
        }

        Type[] typeArgs = paramType.getActualTypeArguments();
        if (typeArgs.length != 1 || !(typeArgs[0] instanceof Class<?>)) {
            return null;
        }

        return new ImportParamInfo((Class<?>) typeArgs[0], isIterator);
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

    /** 导入参数信息 */
    private record ImportParamInfo(Class<?> elementType, boolean isStreaming) {
    }
}
