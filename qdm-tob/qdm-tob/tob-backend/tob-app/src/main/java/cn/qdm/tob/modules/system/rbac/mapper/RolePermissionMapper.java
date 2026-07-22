package cn.qdm.tob.modules.system.rbac.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.rbac.domain.RolePermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends TobBaseMapper<RolePermission> {

    default List<RolePermission> findByRoleId(Long roleId) {
        return lambdaSelect(w -> w.eq(RolePermission::getRoleId, roleId));
    }

    default int deleteByRoleId(Long roleId) {
        return lambdaDelete(w -> w.eq(RolePermission::getRoleId, roleId));
    }
}
