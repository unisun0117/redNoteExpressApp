package cn.qdm.tob.modules.system.menu.service;

import cn.qdm.tob.framework.ProxySelf;
import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.common.CacheKeys;
import cn.qdm.tob.modules.system.common.SystemConstants;
import cn.qdm.tob.modules.system.menu.MenuMapping;
import cn.qdm.tob.modules.system.menu.enums.MenuGroup;
import cn.qdm.tob.modules.system.menu.enums.MenuType;
import cn.qdm.tob.modules.system.menu.domain.SysMenu;
import cn.qdm.tob.modules.system.menu.dto.MenuEditDTO;
import cn.qdm.tob.modules.system.menu.dto.MenuSaveDTO;
import cn.qdm.tob.modules.system.menu.mapper.SysMenuMapper;
import cn.qdm.tob.modules.system.menu.vo.MenuTreeNodeVO;
import cn.qdm.tob.modules.system.menu.vo.MenuViewVO;
import cn.qdm.tob.modules.system.rbac.api.internal.AuthorityApi;
import cn.qdm.tob.modules.system.rbac.service.MenuPermissionService;
import cn.qdm.tob.modules.system.rbac.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuService extends TobBaseService<SysMenuMapper, SysMenu> implements ProxySelf<SysMenuService> {

    private final MenuMapping menuMapping;
    private final AuthorityApi authorityApi;
    private final MenuPermissionService menuPermissionService;
    private final RoleMenuService roleMenuService;

    // ==================== 管理端 CRUD ====================

    /** 管理端树（所有状态；includeButtons=true 时含按钮，供角色管理页用） */
    public List<MenuTreeNodeVO> getAdminTree(MenuGroup group, boolean includeButtons) {
        return toTree(menuMapping.toTreeNodeList(baseMapper.listByGroup(group, includeButtons)));
    }

    /** 指定页面下的按钮列表（批量查权限码，避免 N+1） */
    public List<MenuTreeNodeVO> getButtons(Long pageId) {
        List<SysMenu> buttons = baseMapper.listByParentId(pageId, null);
        List<MenuTreeNodeVO> nodes = menuMapping.toTreeNodeList(buttons);

        if (!buttons.isEmpty()) {
            Set<Long> buttonIds = buttons.stream().map(SysMenu::getId).collect(Collectors.toSet());
            Map<Long, List<String>> permMap = menuPermissionService.findCodesByMenuIdsGrouped(buttonIds);
            nodes.forEach(node -> node.setPermissionCodes(permMap.getOrDefault(node.getId(), Collections.emptyList())));
        }
        return nodes;
    }

    /** 查看节点详情 */
    public MenuViewVO getById(Long id) {
        SysMenu entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "菜单不存在");
        return menuMapping.toView(entity);
    }

    /** 创建节点 — 只清 menu:all，user:menu_ids 不变（menu:all 刷新后剪枝自然过滤） */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheKeys.MENU_ALL, key = "#dto.group.name()")
    public void create(MenuSaveDTO dto) {
        if (!Objects.equals(SystemConstants.ROOT_MENU_ID, dto.getParentId())) {
            SysMenu parent = baseMapper.selectById(dto.getParentId());
            AssertUtils.notNull(parent, "父级菜单不存在");
        }
        AssertUtils.isNull(baseMapper.findByCode(dto.getCode()), "编码已存在: " + dto.getCode());
        AssertUtils.isFalse(baseMapper.nameExistsAtSameLevel(dto.getParentId(), dto.getName(), null),
                "同级节点下名称已存在: " + dto.getName());

        SysMenu entity = menuMapping.toEntity(dto);
        baseMapper.insert(entity);

        if (dto.getType() == MenuType.BUTTON && CollectionUtils.isNotEmpty(dto.getPermissionCodes())) {
            menuPermissionService.savePermissionCodes(entity.getId(), dto.getPermissionCodes());
        }
    }

    /** 更新节点 — 只清 menu:all；按钮权限码变了才清 user:perms */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheKeys.MENU_ALL, key = "#result.menuGroup.name()")
    public SysMenu update(MenuEditDTO dto) {
        SysMenu existing = baseMapper.selectById(dto.getId());
        AssertUtils.notNull(existing, "菜单不存在");
        AssertUtils.isFalse(baseMapper.nameExistsAtSameLevel(
                existing.getParentId(), dto.getName(), dto.getId()),
                "同级节点下名称已存在: " + dto.getName());

        SysMenu entity = menuMapping.toEntity(dto);
        baseMapper.updateById(entity);

        boolean permChanged = false;
        if (existing.getType() == MenuType.BUTTON) {
            if (CollectionUtils.isNotEmpty(dto.getPermissionCodes())) {
                menuPermissionService.savePermissionCodes(dto.getId(), dto.getPermissionCodes());
            } else {
                menuPermissionService.deleteByMenuId(dto.getId());
            }
            permChanged = true;
        }

        if (permChanged) {
            authorityApi.evictPermsByMenuId(dto.getId());
        }

        return existing;
    }

    /** 删除节点（仅 BUTTON） — 先查 roleIds，再删关联，最后清 user:perms */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheKeys.MENU_ALL, key = "#result.menuGroup.name()")
    public SysMenu delete(Long id) {
        SysMenu entity = baseMapper.selectById(id);
        AssertUtils.notNull(entity, "菜单不存在");
        AssertUtils.isTrue(entity.getType() == MenuType.BUTTON, "仅按钮支持删除");

        Set<Long> roleIds = roleMenuService.deleteByMenuId(id);
        menuPermissionService.deleteByMenuId(id);
        baseMapper.deleteById(id);

        authorityApi.evictPermsByRoleIds(roleIds);

        return entity;
    }

    // ==================== 用户端菜单 ====================

    /** 用户菜单树：menu:all + user:menu_ids → 内存剪枝 */
    public List<MenuTreeNodeVO> getUserTree(MenuGroup group, Long operatorId) {
        List<SysMenu> entities = self().getMenuAll(group);
        List<MenuTreeNodeVO> nodes = menuMapping.toTreeNodeList(entities);

        Set<Long> authorizedMenuIds = authorityApi.getUserMenuIds(operatorId);
        if (authorizedMenuIds.isEmpty()) return Collections.emptyList();

        return pruneTree(toTree(nodes), authorizedMenuIds);
    }

    // ==================== 菜单缓存 ====================

    /** 全量菜单（Spring Cache：cache miss → 查 DB 自动回填，TTL 1 天） */
    @Cacheable(value = CacheKeys.MENU_ALL, key = "#group.name()")
    public List<SysMenu> getMenuAll(MenuGroup group) {
        return baseMapper.listByGroup(group, RecordStatus.ACTIVE);
    }

    // ==================== 树操作 ====================

    private List<MenuTreeNodeVO> toTree(List<MenuTreeNodeVO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) return Collections.emptyList();
        Map<Long, List<MenuTreeNodeVO>> byParentId = nodes.stream()
                .collect(Collectors.groupingBy(MenuTreeNodeVO::getParentId));
        List<MenuTreeNodeVO> roots = byParentId.getOrDefault(SystemConstants.ROOT_MENU_ID, Collections.emptyList());
        roots.forEach(root -> buildChildren(root, byParentId));
        return roots;
    }

    private void buildChildren(MenuTreeNodeVO parent, Map<Long, List<MenuTreeNodeVO>> byParentId) {
        List<MenuTreeNodeVO> children = byParentId.getOrDefault(parent.getId(), Collections.emptyList());
        parent.setChildren(children);
        children.forEach(child -> buildChildren(child, byParentId));
    }

    private List<MenuTreeNodeVO> pruneTree(List<MenuTreeNodeVO> nodes, Set<Long> authorizedIds) {
        if (CollectionUtils.isEmpty(nodes)) return Collections.emptyList();
        List<MenuTreeNodeVO> result = new ArrayList<>();
        for (MenuTreeNodeVO node : nodes) {
            List<MenuTreeNodeVO> visibleChildren = pruneTree(node.getChildren(), authorizedIds);
            if (authorizedIds.contains(node.getId()) || !visibleChildren.isEmpty()) {
                node.setChildren(visibleChildren);
                result.add(node);
            }
        }
        return result;
    }
}
