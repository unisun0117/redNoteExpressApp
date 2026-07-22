package cn.qdm.tob.modules.system.rbac.api.internal;

/**
 * 角色-权限关联表 CRUD 接口（旧链路 sys_role_permission），供 role 模块角色删除时级联清理。
 *
 * @deprecated 权限校验已迁移至 {@code sys_menu_permission}，此接口仅用于角色删除时的遗留数据清理。
 */
public interface RolePermissionApi {
    /** 删除角色的所有权限绑定（角色删除时用） */
    void deleteByRoleId(Long roleId);
}
