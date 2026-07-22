package cn.qdm.tob.modules.system.rbac.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.rbac.domain.ApiPermission;
import cn.qdm.tob.modules.system.rbac.service.ApiPermissionManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API 权限映射管理（配置哪个接口需要哪个权限码）
 *
 * @deprecated 当前版本使用基于菜单按钮的静态权限码校验，非动态 URL 匹配。
 *             保留供后续按需启用动态 URL 授权。
 */
@Deprecated
@RestController
@RequestMapping("/api/admin/api-permissions")
@RequiredArgsConstructor
public class ApiPermissionController {

    private final ApiPermissionManageService apiPermissionManageService;

    @GetMapping
    public ResponseResult<List<ApiPermission>> list() {
        return ResponseResult.success(apiPermissionManageService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseResult<ApiPermission> get(@PathVariable Long id) {
        return ResponseResult.success(apiPermissionManageService.getById(id));
    }

    @PostMapping
    public ResponseResult<ApiPermission> create(@RequestBody ApiPermission apiPermission) {
        return ResponseResult.success(apiPermissionManageService.create(apiPermission));
    }

    @PutMapping("/{id}")
    public ResponseResult<ApiPermission> update(@PathVariable Long id,
                                                @RequestBody ApiPermission patch) {
        return ResponseResult.success(apiPermissionManageService.update(id, patch));
    }

    @DeleteMapping("/{id}")
    public ResponseResult<?> delete(@PathVariable Long id) {
        apiPermissionManageService.delete(id);
        return ResponseResult.success();
    }
}
