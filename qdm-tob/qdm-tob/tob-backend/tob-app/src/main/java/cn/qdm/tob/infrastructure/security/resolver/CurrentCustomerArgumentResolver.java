package cn.qdm.tob.infrastructure.security.resolver;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.security.annotation.CurrentCustomer;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentCustomer} 注解参数解析器 — 委托 {@link CustomerContextProvider} 处理业务逻辑
 */
@Component
@RequiredArgsConstructor
public class CurrentCustomerArgumentResolver implements HandlerMethodArgumentResolver {

    private final CustomerContextProvider customerContextProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentCustomer.class)
                && CustomerContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        UserPrincipal user = SecurityUtil.requireCurrentUser();
        CurrentCustomer annotation = parameter.getParameterAnnotation(CurrentCustomer.class);
        String paramName = annotation != null ? annotation.value() : CurrentCustomer.DEFAULT_PARAM_NAME;
        String param = webRequest.getParameter(paramName);
        AssertUtils.notBlank(param, "缺少\"" + paramName + "\"参数");

        return customerContextProvider.resolve(user.getUserId(), Long.valueOf(param));
    }
}
