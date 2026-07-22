package cn.qdm.tob.modules.system.menu.service;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.menu.MenuMapping;
import cn.qdm.tob.modules.system.menu.domain.SysMenu;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import cn.qdm.tob.modules.system.menu.mapper.SysMenuMapper;
import cn.qdm.tob.modules.system.menu.vo.MenuTreeNodeVO;
import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import cn.qdm.tob.modules.system.rbac.service.MenuPermissionService;
import cn.qdm.tob.modules.system.rbac.service.RoleMenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SysMenuService 单测
 * 覆盖菜单树渲染、管理端树
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("菜单 Service")
class SysMenuServiceTest {

    @Mock
    private SysMenuMapper menuMapper;
    @Mock
    private MenuMapping menuMapping;
    @Mock
    private AuthorityApi authorityApi;
    @Mock
    private MenuPermissionService menuPermissionService;
    @Mock
    private RoleMenuService roleMenuService;

    private SysMenuService menuService;

    @BeforeEach
    void setUp() {
        menuService = new SysMenuService(
                menuMapping, authorityApi, menuPermissionService, roleMenuService);
        ReflectionTestUtils.setField(menuService, "baseMapper", menuMapper);
    }

    // ==================== getUserTree ====================

    @Test
    @DisplayName("getUserTree - 无授权 menuId → 空树")
    void shouldReturnEmptyTreeWhenNoMenuIds() {
        when(menuMapper.listByGroup(MenuGroup.WEB, RecordStatus.ACTIVE))
                .thenReturn(List.of(buildMenu(1L, 0L, "M1", MenuType.MENU)));
        when(menuMapping.toTreeNodeList(anyList()))
                .thenReturn(List.of(buildNode(1L, 0L, "M1", MenuType.MENU)));
        when(authorityApi.getUserMenuIds(1L)).thenReturn(Collections.emptySet());

        List<MenuTreeNodeVO> tree = menuService.getUserTree(MenuGroup.WEB, 1L);

        assertThat(tree).isEmpty();
    }

    @Test
    @DisplayName("getUserTree - 有授权 → menu:all + menuIds 内存剪枝")
    void shouldReturnPrunedTree() {
        SysMenu m1 = buildMenu(1L, 0L, "M1", MenuType.MENU);
        SysMenu p1 = buildMenu(2L, 1L, "P1", MenuType.PAGE);
        SysMenu b1 = buildMenu(3L, 2L, "B1", MenuType.BUTTON);
        when(menuMapper.listByGroup(MenuGroup.WEB, RecordStatus.ACTIVE))
                .thenReturn(List.of(m1, p1, b1));
        when(menuMapping.toTreeNodeList(anyList())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            List<SysMenu> menus = inv.getArgument(0);
            return menus.stream().map(m -> buildNode(m.getId(), m.getParentId(), m.getName(), m.getType())).toList();
        });
        when(authorityApi.getUserMenuIds(1L)).thenReturn(Set.of(3L));

        List<MenuTreeNodeVO> tree = menuService.getUserTree(MenuGroup.WEB, 1L);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getId()).isEqualTo(2L);
        assertThat(tree.get(0).getChildren().get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getChildren().get(0).getId()).isEqualTo(3L);
    }

    // ==================== getAdminTree ====================

    @Test
    @DisplayName("getAdminTree - includeButtons=false → 不含按钮")
    void shouldNotIncludeButtonsWhenFalse() {
        SysMenu m1 = buildMenu(1L, 0L, "M1", MenuType.MENU);
        when(menuMapper.listByGroup(MenuGroup.WEB, false))
                .thenReturn(List.of(m1));
        when(menuMapping.toTreeNodeList(anyList())).thenAnswer(inv -> {
            List<SysMenu> menus = inv.getArgument(0);
            return menus.stream().map(m -> buildNode(m.getId(), m.getParentId(), m.getName(), m.getType())).toList();
        });

        List<MenuTreeNodeVO> tree = menuService.getAdminTree(MenuGroup.WEB, false);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).isNullOrEmpty();
        verifyNoInteractions(menuPermissionService);
    }

    @Test
    @DisplayName("getAdminTree - includeButtons=true → 含按钮")
    void shouldIncludeButtonsWhenRequested() {
        SysMenu m1 = buildMenu(1L, 0L, "M1", MenuType.MENU);
        SysMenu b1 = buildMenu(3L, 1L, "B1", MenuType.BUTTON);
        when(menuMapper.listByGroup(MenuGroup.WEB, true))
                .thenReturn(List.of(m1, b1));
        when(menuMapping.toTreeNodeList(anyList())).thenAnswer(inv -> {
            List<SysMenu> menus = inv.getArgument(0);
            return menus.stream().map(m -> buildNode(m.getId(), m.getParentId(), m.getName(), m.getType())).toList();
        });

        List<MenuTreeNodeVO> tree = menuService.getAdminTree(MenuGroup.WEB, true);

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getId()).isEqualTo(3L);
    }

    // ==================== 辅助方法 ====================

    private SysMenu buildMenu(Long id, Long parentId, String name, MenuType type) {
        SysMenu m = new SysMenu();
        m.setId(id);
        m.setParentId(parentId);
        m.setName(name);
        m.setType(type);
        m.setSort(0);
        m.setStatus(RecordStatus.ACTIVE);
        m.setMenuGroup(MenuGroup.WEB);
        return m;
    }

    private MenuTreeNodeVO buildNode(Long id, Long parentId, String name, MenuType type) {
        MenuTreeNodeVO n = new MenuTreeNodeVO();
        n.setId(id);
        n.setParentId(parentId);
        n.setName(name);
        n.setType(type);
        n.setSort(0);
        n.setStatus(RecordStatus.ACTIVE);
        return n;
    }
}
