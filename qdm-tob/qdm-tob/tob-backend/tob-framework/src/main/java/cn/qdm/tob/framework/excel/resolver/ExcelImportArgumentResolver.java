package cn.qdm.tob.framework.excel.resolver;

import cn.qdm.tob.framework.excel.annotation.Importable;
import cn.qdm.tob.framework.excel.interceptor.ImportInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Excel 导入参数解析器，对应 .NET ExcelImportModelBinder / ExcelImportModelBinderProvider。
 * <p>
 * 从 {@code HttpServletRequest.getAttribute("__ExcelImportResult")} 读取中间件解析好的数据，
 * 并绑定到 Controller 方法参数上。
 * </p>
 */
public class ExcelImportArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 必须标记了 @ExcelImportable 注解
        if (!parameter.hasMethodAnnotation(Importable.class)) {
            return false;
        }

        Class<?> paramType = parameter.getParameterType();
        return List.class.isAssignableFrom(paramType)
                || Collection.class.isAssignableFrom(paramType)
                || Iterator.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }

        Object result = request.getAttribute(ImportInterceptor.IMPORT_RESULT_KEY);

        // 处理流式导入：Iterator<T> → 包装以确保兼容性
        if (result instanceof Iterator<?> iter
                && !Iterator.class.isAssignableFrom(parameter.getParameterType())) {
            // 如果参数声明为 List，但实际是 Iterator，全部收集
            List<Object> list = new java.util.ArrayList<>();
            while (iter.hasNext()) {
                list.add(iter.next());
            }
            return list;
        }

        return result;
    }
}
