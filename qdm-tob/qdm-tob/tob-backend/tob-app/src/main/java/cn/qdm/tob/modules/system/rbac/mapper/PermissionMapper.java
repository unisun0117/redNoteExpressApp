package cn.qdm.tob.modules.system.rbac.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.rbac.domain.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PermissionMapper extends TobBaseMapper<Permission> {

    default Optional<Permission> findByCode(String code) {
        return lambdaSelectOne(w -> w.eq(Permission::getCode, code));
    }
}
