package cn.qdm.tob.infrastructure.security.resolver;

import cn.qdm.tob.infrastructure.security.annotation.CurrentUser;
import cn.qdm.tob.infrastructure.security.domain.UserPrincipal;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentUser} 注解参数解析器
 * <p>
 * 将标注了 {@code @CurrentUser} 且类型为 {@link UserPrincipal} 的参数
 * 自动注入当前登录用户信息。
 * </p>
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && UserPrincipal.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return SecurityUtil.requireCurrentUser();
    }
}
