package cn.qdm.tob.modules.system.rbac.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.rbac.domain.SysMenuPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface SysMenuPermissionMapper extends TobBaseMapper<SysMenuPermission> {

    /** 批量插入 */
    void batchInsert(List<SysMenuPermission> list);

    /** 批量查询多个菜单绑定的权限码（按 menuId 分组，用于管理端菜单树批量回填） */
    default Map<Long, List<String>> findCodesByMenuIdsGrouped(Set<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return Map.of();
        return lambdaSelect(w -> w.in(SysMenuPermission::getMenuId, menuIds))
                .stream()
                .collect(Collectors.groupingBy(
                        SysMenuPermission::getMenuId,
                        Collectors.mapping(SysMenuPermission::getPermissionCode, Collectors.toList())
                ));
    }

    /** 删除按钮所有权限关联（保存时全量覆盖） */
    default void deleteByMenuId(Long menuId) {
        lambdaDelete(w -> w.eq(SysMenuPermission::getMenuId, menuId));
    }

    /** 精准删除指定权限码（增量覆盖用） */
    default void deleteByMenuIdAndCodes(Long menuId, List<String> codes) {
        if (codes == null || codes.isEmpty()) return;
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenuPermission>()
                .eq(SysMenuPermission::getMenuId, menuId)
                .in(SysMenuPermission::getPermissionCode, codes));
    }

}
