package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.framework.ProxySelf;
import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.common.CacheKeys;
import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin 权限加载器
 * <p>
 * 走「用户 → 角色 → 菜单 → 权限码」链路加载授权数据：
 * sys_operator_role → sys_role_menu → sys_menu_permission
 * <p>
 * 缓存策略（全部 Spring Cache，TTL 默认）：
 * - user:perms:{uid}     用户最终权限码集合
 * - user:menu_ids:{uid}  用户授权 menuId 集合（跨所有 group，和 menu:all 拼装剪枝）
 * - 变更时反查 userIds 主动清，TTL 仅兜底
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperatorAuthorityLoader implements AuthorityApi, ProxySelf<OperatorAuthorityLoader> {

    private final OperatorRoleService operatorRoleService;
    private final RoleMenuService roleMenuService;
    private final CacheManager cacheManager;

    @Override
    public Collection<GrantedAuthority> loadAuthorities(Long operatorId) {
        Set<String> codes = self().getUserPerms(operatorId);
        return codes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // ==================== 查询方法 ====================

    /** 查询用户权限码（一条 SQL JOIN：operator → role → menu → permission，Spring Cache） */
    @Cacheable(value = CacheKeys.USER_PERMS, key = "#operatorId")
    public Set<String> getUserPerms(Long operatorId) {
        return operatorRoleService.findPermissionsByOperatorId(operatorId, RecordStatus.ACTIVE.name());
    }

    /** 查询用户授权的 menuId 集合（一条 SQL：operator → role_menu，跨所有 group，Spring Cache） */
    @Override
    @Cacheable(value = CacheKeys.USER_MENU_IDS, key = "#operatorId")
    public Set<Long> getUserMenuIds(Long operatorId) {
        return operatorRoleService.findMenuIdsByOperatorId(operatorId, RecordStatus.ACTIVE.name());
    }

    // ==================== 缓存失效 ====================

    /** 角色改菜单时清缓存：反查 userIds → 清 user:perms + user:menu_ids */
    @Async("cacheExecutor")
    @Override
    public void evictRolePerms(Long roleId) {
        try {
            List<Long> ids = operatorRoleService.findOperatorIdsByRoleId(roleId);
            evictBothCaches(ids);
            log.info("角色缓存已清除: roleId={}, 影响用户数={}", roleId, ids.size());
        } catch (Exception e) {
            log.warn("清除角色缓存失败: roleId={}, err={}", roleId, e.getMessage());
        }
    }

    /** 按钮权限码变更时清缓存：反查 userIds → 仅清 user:perms（菜单结构没变） */
    @Async("cacheExecutor")
    @Override
    public void evictPermsByMenuId(Long menuId) {
        try {
            List<Long> ids = roleMenuService.findOperatorIdsByMenuId(menuId);
            evictCache(ids, CacheKeys.USER_PERMS);
            log.info("菜单权限缓存已清除: menuId={}, 影响用户数={}", menuId, ids.size());
        } catch (Exception e) {
            log.warn("清除菜单权限缓存失败: menuId={}, err={}", menuId, e.getMessage());
        }
    }

    /** 菜单删除时清缓存：传入删除前的 roleId 集合 → 反查 userIds → 清 user:perms */
    @Async("cacheExecutor")
    @Override
    public void evictPermsByRoleIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        try {
            Set<Long> userIds = new HashSet<>();
            for (Long roleId : roleIds) {
                userIds.addAll(operatorRoleService.findOperatorIdsByRoleId(roleId));
            }
            if (!userIds.isEmpty()) {
                Cache cache = cacheManager.getCache(CacheKeys.USER_PERMS);
                if (cache != null) userIds.forEach(cache::evict);
            }
            log.info("菜单删除缓存已清除: roleIds={}, 影响用户数={}", roleIds, userIds.size());
        } catch (Exception e) {
            log.warn("清除菜单删除缓存失败: roleIds={}, err={}", roleIds, e.getMessage());
        }
    }

    /** 角色删除时清受影响用户的缓存 */
    @Async("cacheExecutor")
    @Override
    public void evictRolePermsOnDelete(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        try {
            evictBothCaches(userIds);
            log.info("角色删除缓存已清除: 影响用户数={}", userIds.size());
        } catch (Exception e) {
            log.warn("清除角色删除缓存失败: userIds={}, err={}", userIds, e.getMessage());
        }
    }

    /** 批量清 user:perms + user:menu_ids */
    private void evictBothCaches(Collection<Long> userIds) {
        evictCache(userIds, CacheKeys.USER_PERMS);
        evictCache(userIds, CacheKeys.USER_MENU_IDS);
    }

    private void evictCache(Collection<Long> userIds, String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return;
        userIds.forEach(cache::evict);
    }
}
