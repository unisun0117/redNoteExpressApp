package cn.qdm.tob.modules.system.rbac.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OperatorAuthorityLoader 单测
 * 覆盖：权限加载、menuId 查询、缓存清理
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("权限加载器")
class OperatorAuthorityLoaderTest {

    @Mock
    private OperatorRoleService operatorRoleService;
    @Mock
    private RoleMenuService roleMenuService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache permsCache;
    @Mock
    private Cache menuIdsCache;

    private OperatorAuthorityLoader loader;

    @BeforeEach
    void setUp() {
        lenient().when(cacheManager.getCache("tob:system:user:perms:")).thenReturn(permsCache);
        lenient().when(cacheManager.getCache("tob:system:user:menu_ids:")).thenReturn(menuIdsCache);
        loader = new OperatorAuthorityLoader(
                operatorRoleService, roleMenuService, cacheManager);
        ReflectionTestUtils.setField(loader, "self", loader);
    }

    // ==================== 权限加载 ====================

    @Test
    @DisplayName("用户无角色 → 空权限")
    void shouldReturnEmptyWhenNoRoles() {
        when(operatorRoleService.findPermissionsByOperatorId(eq(1L), anyString()))
                .thenReturn(Set.of());

        Collection<GrantedAuthority> authorities = loader.loadAuthorities(1L);

        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("正常链路 → 返回权限码（一条 SQL JOIN）")
    void shouldReturnDistinctPermCodes() {
        when(operatorRoleService.findPermissionsByOperatorId(eq(1L), anyString()))
                .thenReturn(Set.of("sys:user:view", "sys:user:edit"));

        Collection<GrantedAuthority> authorities = loader.loadAuthorities(1L);

        Set<String> codes = new HashSet<>();
        authorities.forEach(a -> codes.add(a.getAuthority()));
        assertThat(codes).containsExactlyInAnyOrder("sys:user:view", "sys:user:edit");
    }

    // ==================== menuId 查询 ====================

    @Test
    @DisplayName("getUserMenuIds - 无角色 → 空集合")
    void getUserMenuIdsShouldReturnEmptyWhenNoRoles() {
        when(operatorRoleService.findMenuIdsByOperatorId(eq(1L), anyString())).thenReturn(Set.of());

        Set<Long> menuIds = loader.getUserMenuIds(1L);

        assertThat(menuIds).isEmpty();
    }

    @Test
    @DisplayName("getUserMenuIds - 多角色 → 聚合 menuId（一条 SQL JOIN）")
    void getUserMenuIdsShouldAggregateFromMultipleRoles() {
        when(operatorRoleService.findMenuIdsByOperatorId(eq(1L), anyString()))
                .thenReturn(Set.of(100L, 101L, 102L));

        Set<Long> menuIds = loader.getUserMenuIds(1L);

        assertThat(menuIds).containsExactlyInAnyOrder(100L, 101L, 102L);
    }

    // ==================== 缓存失效 ====================

    @Test
    @DisplayName("evictRolePerms → 反查用户删 user:perms + user:menu_ids")
    void shouldEvictRolePermsAndAffectedUsers() {
        when(operatorRoleService.findOperatorIdsByRoleId(10L))
                .thenReturn(List.of(1L, 2L));

        loader.evictRolePerms(10L);

        verify(permsCache).evict(1L);
        verify(permsCache).evict(2L);
        verify(menuIdsCache).evict(1L);
        verify(menuIdsCache).evict(2L);
    }

    @Test
    @DisplayName("evictPermsByMenuId → 仅删 user:perms（按钮权限不影响 menuId）")
    void shouldEvictPermsByMenuId() {
        when(roleMenuService.findOperatorIdsByMenuId(100L)).thenReturn(List.of(1L, 2L, 3L));

        loader.evictPermsByMenuId(100L);

        verify(permsCache).evict(1L);
        verify(permsCache).evict(2L);
        verify(permsCache).evict(3L);
        verify(menuIdsCache, never()).evict(any());
    }

    @Test
    @DisplayName("evictRolePermsOnDelete → 删 user:perms + user:menu_ids")
    void shouldEvictOnRoleDelete() {
        loader.evictRolePermsOnDelete(List.of(1L, 2L));

        verify(permsCache).evict(1L);
        verify(permsCache).evict(2L);
        verify(menuIdsCache).evict(1L);
        verify(menuIdsCache).evict(2L);
    }
}
