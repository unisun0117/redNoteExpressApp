package cn.qdm.tob.modules.system.rbac.mapper;

import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.rbac.domain.ApiPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApiPermissionMapper extends TobBaseMapper<ApiPermission> {

    /** 按 HTTP 方法查询所有启用的映射（用于动态授权匹配） */
    default List<ApiPermission> findActiveByMethod(String httpMethod) {
        return lambdaSelect(w -> w
                .eq(ApiPermission::getHttpMethod, httpMethod)
                .eq(ApiPermission::getStatus, RecordStatus.ACTIVE));
    }
}
