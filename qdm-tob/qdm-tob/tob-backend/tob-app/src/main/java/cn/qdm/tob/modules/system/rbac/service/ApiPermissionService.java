package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.modules.system.rbac.domain.ApiPermission;
import cn.qdm.tob.modules.system.rbac.mapper.ApiPermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * API 权限映射查询服务。
 * 根据 HTTP 方法和 URI，用 AntPathMatcher 匹配 url_pattern 查询该 API 所需的权限码。
 *
 * @deprecated 当前版本使用基于菜单按钮的静态权限码校验，非动态 URL 匹配。
 *             见 {@link cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader}。
 *             本类及 {@link DynamicAuthorizationManager} 保留供后续按需启用动态 URL 授权。
 */
@Deprecated
@Slf4j
@Service
public class ApiPermissionService {

    private final ApiPermissionMapper apiPermissionMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiPermissionService(ApiPermissionMapper apiPermissionMapper) {
        this.apiPermissionMapper = apiPermissionMapper;
    }

    /**
     * 查询指定 method + uri 所需的权限码集合。
     * 空集表示该 API 无需特定权限（公开或仅需登录）。
     */
    public Set<String> findRequiredPermissions(String httpMethod, String uri) {
        List<ApiPermission> mappings = apiPermissionMapper.findActiveByMethod(httpMethod);

        Set<String> required = new HashSet<>();
        for (ApiPermission mapping : mappings) {
            if (pathMatcher.match(mapping.getUrlPattern(), uri)) {
                required.add(mapping.getPermissionCode());
            }
        }
        return required;
    }
}
