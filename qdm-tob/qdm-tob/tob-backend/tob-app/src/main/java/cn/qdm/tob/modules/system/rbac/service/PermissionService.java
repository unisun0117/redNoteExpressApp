package cn.qdm.tob.modules.system.rbac.service;

import cn.qdm.tob.framework.exception.ErrorCode;
import cn.qdm.tob.framework.exception.TobServiceException;
import cn.qdm.tob.framework.model.RecordStatus;
import cn.qdm.tob.modules.system.rbac.domain.Permission;
import cn.qdm.tob.modules.system.rbac.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限 CRUD 服务（旧链路 sys_permission 表）
 *
 * @deprecated 权限校验已迁移至 {@code sys_menu_permission}（菜单按钮权限码），
 *             见 {@link cn.qdm.tob.modules.system.rbac.service.OperatorAuthorityLoader}。
 *             本类保留供后续按需启用。
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionMapper permissionMapper;

    public List<Permission> listAll() {
        return permissionMapper.selectList(null);
    }

    public Permission getById(Long id) {
        Permission p = permissionMapper.selectById(id);
        if (p == null) {
            throw new TobServiceException(ErrorCode.NOT_FOUND.code(), "权限不存在: " + id);
        }
        return p;
    }

    public Permission create(Permission permission) {
        if (permissionMapper.findByCode(permission.getCode()).isPresent()) {
            throw new TobServiceException(ErrorCode.BAD_REQUEST.code(), "权限码已存在: " + permission.getCode());
        }
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        if (permission.getStatus() == null) {
            permission.setStatus(RecordStatus.ACTIVE);
        }
        permissionMapper.insert(permission);
        return permission;
    }

    public Permission update(Long id, Permission patch) {
        Permission existing = getById(id);
        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        permissionMapper.updateById(existing);
        // 注意：sys_permission 表已不再参与权限校验（改走 sys_menu_permission），无需清缓存
        return existing;
    }

    public void delete(Long id) {
        getById(id);
        permissionMapper.deleteById(id);
        // 注意：sys_permission 表已不再参与权限校验（改走 sys_menu_permission），无需清缓存
    }
}
