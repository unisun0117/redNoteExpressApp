package cn.qdm.tob.infrastructure.security;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * {@link RequirePermission} 注解的授权管理器。
 *
 * <p>从方法或类上的 {@link RequirePermission} 注解读取所需权限码，
 * 与当前认证用户的权限集合比对，全部满足则放行。</p>
 */
public class RequirePermissionAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    @Override
    public AuthorizationResult authorize(@NonNull Supplier<? extends Authentication> authentication, MethodInvocation mi) {
        Permissions[] permissions = resolveRequiredPermissions(mi);
        if (permissions.length == 0) {
            return new AuthorizationDecision(true);
        }

        var auth = authentication.get();
        if (Objects.isNull(auth) || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        var authorities = auth
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        var granted = Arrays.stream(permissions)
                .map(Permissions::getValue)
                .allMatch(authorities::contains);

        return new AuthorizationDecision(granted);
    }

    @Override
    public void verify(@NonNull Supplier<? extends Authentication> authentication, MethodInvocation mi) {
        AuthorizationResult result = authorize(authentication, mi);
        if (Objects.nonNull(result) && !result.isGranted()) {
            throw new AuthorizationDeniedException("权限不足", new AuthorizationDecision(false));
        }
    }

    /**
     * 从方法注解 → 类注解的优先级依次解析所需权限码。
     */
    private Permissions[] resolveRequiredPermissions(MethodInvocation mi) {
        Method method = mi.getMethod();

        RequirePermission ann = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        if (Objects.nonNull(ann)) {
            return ann.value();
        }

        ann = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequirePermission.class);
        if (Objects.nonNull(ann)) {
            return ann.value();
        }

        return new Permissions[0];
    }
}
