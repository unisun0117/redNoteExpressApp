package cn.qdm.tob.modules.system.rbac.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.rbac.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色-菜单关联 Mapper
 */
@Mapper
public interface SysRoleMenuMapper extends TobBaseMapper<SysRoleMenu> {

    default Set<Long> findByRoleId(Long roleId) {
        return lambdaSelect(w -> w.eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
    }

    default void deleteByRoleId(Long roleId) {
        lambdaDelete(w -> w.eq(SysRoleMenu::getRoleId, roleId));
    }

    default void deleteByMenuId(Long menuId) {
        lambdaDelete(w -> w.eq(SysRoleMenu::getMenuId, menuId));
    }

    default Set<Long> findRoleIdsByMenuId(Long menuId) {
        return lambdaSelect(w -> w.eq(SysRoleMenu::getMenuId, menuId))
                .stream()
                .map(SysRoleMenu::getRoleId)
                .collect(Collectors.toSet());
    }

    /** 反查引用某菜单的所有用户 ID（一条 JOIN SQL，用于缓存失效） */
    List<Long> findOperatorIdsByMenuId(Long menuId);

    /** 批量插入 */
    void batchInsert(@Param("list") List<SysRoleMenu> list);

    /** 精准删除指定菜单绑定（增量覆盖用） */
    default void deleteByRoleIdAndMenuIds(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .in(SysRoleMenu::getMenuId, menuIds));
    }
}
