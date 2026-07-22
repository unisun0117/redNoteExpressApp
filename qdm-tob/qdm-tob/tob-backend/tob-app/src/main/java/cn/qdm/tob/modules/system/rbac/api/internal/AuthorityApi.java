package cn.qdm.tob.modules.system.rbac.api.internal;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

/**
 * RBAC 对外公开接口，供其他模块调用。
 * <p>
 * 其他模块（菜单、角色、Auth）通过此接口查询授权数据或触发缓存清理，
 * 不可直接引用 rbac.service / domain / mapper 等内部实现。
 */
public interface AuthorityApi {

    /** 加载用户权限（供 JwtAuthenticationFilter 用） */
    Collection<GrantedAuthority> loadAuthorities(Long operatorId);

    /** 查询用户授权的 menuId 集合（含缓存，跨所有 group） */
    Set<Long> getUserMenuIds(Long operatorId);

    /** 菜单按钮权限码变更时清 user:perms（供 SysMenuService 用） */
    void evictPermsByMenuId(Long menuId);

    /** 菜单删除时清受影响用户的 user:perms（传入删除前的 roleId 集合） */
    void evictPermsByRoleIds(Set<Long> roleIds);

    /** 角色改菜单时清受影响用户的 user:perms + user:menu_ids（供 SysRoleService 用） */
    void evictRolePerms(Long roleId);

    /** 角色删除时清受影响用户的 user:perms + user:menu_ids（供 SysRoleService 用） */
    void evictRolePermsOnDelete(Collection<Long> userIds);

}
