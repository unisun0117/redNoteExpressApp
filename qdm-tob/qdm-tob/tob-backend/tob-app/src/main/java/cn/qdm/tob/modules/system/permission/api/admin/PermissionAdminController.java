package cn.qdm.tob.modules.system.permission.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.web.EndpointScanner;
import cn.qdm.tob.infrastructure.web.domain.EndpointPermission;
import cn.qdm.tob.modules.system.permission.vo.PermissionDefinitionVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 权限定义 API（枚举值查询 + 端点扫描）
 */
@RestController
@RequestMapping("/api/admin/auth/permission")
@RequiredArgsConstructor
public class PermissionAdminController {

    private final EndpointScanner endpointScanner;

    @GetMapping("/list")
    @Operation(summary = "获取所有权限定义（枚举值及描述）")
    public ResponseResult<List<PermissionDefinitionVO>> list() {
        List<PermissionDefinitionVO> list = Arrays
                .stream(Permissions.values())
                .map(PermissionDefinitionVO::new)
                .toList();
        return ResponseResult.success(list);
    }

    @Profile("!prod")
    @GetMapping("/endpoints")
    @Operation(summary = "扫描所有接口，返回 URL pattern 及所需权限")
    public ResponseResult<List<EndpointPermission>> endpoints() {
        return ResponseResult.success(endpointScanner.scan());
    }
}
