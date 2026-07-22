package cn.qdm.tob.modules.system.auth.security;

import cn.qdm.tob.modules.system.rbac.service.ApiPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 动态 URL 授权管理器。
 * <p>
 * 根据请求的 method + uri，从 {@code sys_api_permission} 表 AntPathMatcher 匹配所需权限，再比对用户权限。
 * <p>
 * <b>当前状态：暂不启用。</b>
 * 权限校验走 {@link cn.qdm.tob.infrastructure.security.RequirePermission @RequirePermission} 注解 +
 * {@link cn.qdm.tob.infrastructure.security.RequirePermissionAuthorizationManager} 静态声明方式。
 * SecurityConfiguration 中 {@code /api/admin/**} 仅配置了 {@code .authenticated()}，
 * 待权限体系完善后切换为 {@code .access(dynamicAuthorizationManager)} 启用动态 URL 匹配。
 * <p>
 * 启用条件：
 * <ol>
 *   <li>在 sys_api_permission 表中注册 URL 与权限码的映射关系</li>
 *   <li>将 SecurityConfiguration 中 {@code .authenticated()} 替换为 {@code .access(dynamicAuthorizationManager)}</li>
 * </ol>
 *
 * @deprecated 暂不使用，保留供后续按需启用动态 URL 授权。
 */
@Deprecated
@Slf4j
@Component
public class DynamicAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final ApiPermissionService apiPermissionService;

    public DynamicAuthorizationManager(ApiPermissionService apiPermissionService) {
        this.apiPermissionService = apiPermissionService;
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication,
                                         RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 1. 查询该 API 所需权限
        Set<String> required = apiPermissionService.findRequiredPermissions(method, uri);

        // 2. 无映射 → 仅需登录即可（放行给已认证用户）
        Authentication auth = authentication.get();
        boolean authenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (required.isEmpty()) {
            return new AuthorizationDecision(authenticated);
        }

        // 3. 未认证直接拒绝
        if (!authenticated) {
            return new AuthorizationDecision(false);
        }

        // 4. 用户是否拥有任一所需权限
        Set<String> userAuthorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean granted = required.stream().anyMatch(userAuthorities::contains);
        if (!granted) {
            log.debug("Access denied for {} {}: required={}, user has={}",
                    method, uri, required, userAuthorities);
        }
        return new AuthorizationDecision(granted);
    }
}
