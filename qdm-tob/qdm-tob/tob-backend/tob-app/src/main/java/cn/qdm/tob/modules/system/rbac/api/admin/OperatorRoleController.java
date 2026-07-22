package cn.qdm.tob.modules.system.rbac.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.rbac.service.OperatorRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 管理员角色分配 API
 */
@RestController
@RequestMapping("/api/admin/operator")
@RequiredArgsConstructor
public class OperatorRoleController {

    private final OperatorRoleService operatorRoleService;

    /**
     * 查询管理员已绑定的角色 id
     */
    @GetMapping("/roles")
    public ResponseResult<List<Long>> getRoles(@RequestParam Long operatorId) {
        return ResponseResult.success(operatorRoleService.getRoleIds(operatorId));
    }

    /**
     * 覆盖式设置管理员的角色（Service 层 @Caching 自动清缓存）
     */
    @PutMapping("/roles")
    public ResponseResult<?> setRoles(@RequestParam Long operatorId,
                                      @RequestBody Set<Long> roleIds) {
        operatorRoleService.setRoles(operatorId, roleIds);
        return ResponseResult.success();
    }
}
