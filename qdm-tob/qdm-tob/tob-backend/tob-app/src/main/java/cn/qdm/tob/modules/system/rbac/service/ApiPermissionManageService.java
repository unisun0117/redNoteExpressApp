package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.rbac.domain.ApiPermission;
import cn.qdm.tob.modules.system.rbac.mapper.ApiPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 权限映射 CRUD（后台管理用，与 ApiPermissionService 的查询逻辑分离）
 *
 * @deprecated 当前版本使用基于菜单按钮的静态权限码校验，非动态 URL 匹配。
 *             见 {@link cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader}。
 *             本类保留供后续按需启用动态 URL 授权。
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class ApiPermissionManageService {

    private final ApiPermissionMapper apiPermissionMapper;

    public List<ApiPermission> listAll() {
        return apiPermissionMapper.selectList(null);
    }

    public ApiPermission getById(Long id) {
        ApiPermission ap = apiPermissionMapper.selectById(id);
        if (ap == null) {
            throw new TobServiceException(ErrorCode.NOT_FOUND.code(), "API 权限映射不存在: " + id);
        }
        return ap;
    }

    public ApiPermission create(ApiPermission apiPermission) {
        apiPermission.setCreatedAt(LocalDateTime.now());
        apiPermission.setUpdatedAt(LocalDateTime.now());
        if (apiPermission.getStatus() == null) {
            apiPermission.setStatus(RecordStatus.ACTIVE);
        }
        apiPermissionMapper.insert(apiPermission);
        return apiPermission;
    }

    public ApiPermission update(Long id, ApiPermission patch) {
        ApiPermission existing = getById(id);
        if (patch.getHttpMethod() != null) existing.setHttpMethod(patch.getHttpMethod());
        if (patch.getUrlPattern() != null) existing.setUrlPattern(patch.getUrlPattern());
        if (patch.getPermissionCode() != null) existing.setPermissionCode(patch.getPermissionCode());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        apiPermissionMapper.updateById(existing);
        return existing;
    }

    public void delete(Long id) {
        getById(id);
        apiPermissionMapper.deleteById(id);
    }
}
