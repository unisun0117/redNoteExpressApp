package cn.qdm.tob.modules.system.rbac.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.rbac.domain.Permission;
import cn.qdm.tob.modules.system.rbac.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理 API（旧链路 sys_permission 表）
 *
 * @deprecated 权限校验已迁移至菜单按钮权限码，保留供后续按需启用。
 */
@Deprecated
@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseResult<List<Permission>> list() {
        return ResponseResult.success(permissionService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<Permission> get(@PathVariable Long id) {
        return ResponseResult.success(permissionService.getById(id));
    }

    @PostMapping
    public ResponseResult<Permission> create(@RequestBody Permission permission) {
        return ResponseResult.success(permissionService.create(permission));
    }

    @PutMapping("/{id}")
    public ResponseResult<Permission> update(@PathVariable Long id,
                                             @RequestBody Permission patch) {
        return ResponseResult.success(permissionService.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<?> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseResult.success();
    }
}
