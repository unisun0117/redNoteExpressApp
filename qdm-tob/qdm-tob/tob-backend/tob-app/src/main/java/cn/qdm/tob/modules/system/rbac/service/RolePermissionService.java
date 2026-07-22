package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.modules.system.rbac.api.internal.RolePermissionApi;
import cn.qdm.tob.modules.system.rbac.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 角色-权限关联表服务（旧链路 sys_role_permission 表）
 *
 * @deprecated 角色授权已迁移至 {@code sys_role_menu}（角色 → 菜单 → 权限码），
 *             见 {@link cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader}。
 *             本类保留供后续按需启用。
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class RolePermissionService implements RolePermissionApi {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public void deleteByRoleId(Long roleId) {
        rolePermissionMapper.deleteByRoleId(roleId);
    }
}
