package cn.qdm.tob.modules.system.rbac.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.rbac.domain.SysOperatorRole;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface SysOperatorRoleMapper extends TobBaseMapper<SysOperatorRole> {

    default List<SysOperatorRole> findByOperatorId(Long operatorId) {
        return lambdaSelect(w -> w.eq(SysOperatorRole::getOperatorId, operatorId));
    }

    default int deleteByOperatorId(Long operatorId) {
        return lambdaDelete(w -> w.eq(SysOperatorRole::getOperatorId, operatorId));
    }

    default void deleteByRoleId(Long roleId) {
        lambdaDelete(w -> w.eq(SysOperatorRole::getRoleId, roleId));
    }

    /** 查用户的所有权限码（一条 SQL：operator → role → menu → permission） */
    Set<String> findPermissionsByOperatorId(@Param("operatorId") Long operatorId, @Param("status") String status);

    /** 查用户的所有 menuId（一条 SQL：operator → role_menu，不按 group 过滤） */
    Set<Long> findMenuIdsByOperatorId(@Param("operatorId") Long operatorId, @Param("status") String status);

    /** 反查某角色下的所有用户 ID（用于缓存失效时精准清理） */
    default List<Long> findOperatorIdsByRoleId(Long roleId) {
        return lambdaSelect(w -> w.eq(SysOperatorRole::getRoleId, roleId))
                .stream()
                .map(SysOperatorRole::getOperatorId)
                .toList();
    }

    /** 批量插入 */
    void batchInsert(@Param("list") List<SysOperatorRole> list);

    /** 精准删除指定角色绑定（增量覆盖用） */
    default void deleteByOperatorIdAndRoleIds(Long operatorId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysOperatorRole>()
                .eq(SysOperatorRole::getOperatorId, operatorId)
                .in(SysOperatorRole::getRoleId, roleIds));
    }
}
