package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.infrastructure.security.util.SecurityUtil;
import cn.qdm.tob.modules.system.rbac.domain.SysRoleMenu;
import cn.qdm.tob.modules.system.rbac.mapper.SysRoleMenuMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色-菜单关联表服务
 */
@Service
public class RoleMenuService extends TobBaseService<SysRoleMenuMapper, SysRoleMenu> {

    /** 查询角色绑定的菜单 ID 集合 */
    public Set<Long> listByRoleId(Long roleId) {
        return baseMapper.findByRoleId(roleId);
    }

    /** 增量覆盖角色-菜单绑定 */
    public void setRoleMenus(Long roleId, Set<Long> menuIds) {
        // 查现有菜单
        Set<Long> existing = baseMapper.findByRoleId(roleId);

        // 计算增量差异
        Set<Long> toAdd = new HashSet<>(menuIds);
        toAdd.removeAll(existing);

        Set<Long> toRemove = new HashSet<>(existing);
        toRemove.removeAll(menuIds);

        // 删除移除的
        if (!toRemove.isEmpty()) {
            baseMapper.deleteByRoleIdAndMenuIds(roleId, new ArrayList<>(toRemove));
        }

        // 批量插入新增的
        if (!toAdd.isEmpty()) {
            String createdBy = SecurityUtil.getCurrentUser().getName();
            LocalDateTime now = LocalDateTime.now();
            List<SysRoleMenu> list = toAdd.stream()
                    .map(mid -> new SysRoleMenu(roleId, mid, createdBy, now))
                    .toList();
            baseMapper.batchInsert(list);
        }
    }

    /** 删除角色的所有菜单绑定 */
    public void deleteByRoleId(Long roleId) {
        baseMapper.deleteByRoleId(roleId);
    }

    /** 删除菜单的所有角色绑定，返回删除前的 roleId 集合（供缓存失效用） */
    public Set<Long> deleteByMenuId(Long menuId) {
        Set<Long> roleIds = baseMapper.findRoleIdsByMenuId(menuId);
        baseMapper.deleteByMenuId(menuId);
        return roleIds;
    }

    /** 反查引用某菜单的所有用户 ID（一条 JOIN SQL，用于缓存失效） */
    public List<Long> findOperatorIdsByMenuId(Long menuId) {
        return baseMapper.findOperatorIdsByMenuId(menuId);
    }
}
