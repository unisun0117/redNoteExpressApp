package cn.qdm.tob.modules.system.operator.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.modules.system.operator.dto.OperatorPageQuery;
import cn.qdm.tob.modules.system.operator.dto.OperatorSaveDTO;
import cn.qdm.tob.modules.system.operator.service.OperatorService;
import cn.qdm.tob.modules.system.operator.vo.OperatorVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运营人员管理 API
 */
@Tag(name = "运营人员管理")
@RestController
@RequestMapping("/api/admin/operator")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    @GetMapping("/list")
    @Operation(summary = "获取运营人员列表（分页搜索）")
    @RequirePermission(Permissions.USER_VIEW)
    public ResponseResult<IPage<OperatorVO>> list(@Valid OperatorPageQuery dto) {
        return ResponseResult.success(operatorService.listPage(dto));
    }

    @GetMapping("/view")
    @Operation(summary = "查看运营人员详情")
    @RequirePermission(Permissions.USER_VIEW)
    public ResponseResult<OperatorVO> view(@RequestParam Long id) {
        return ResponseResult.success(operatorService.get(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建运营人员")
    @RequirePermission(Permissions.USER_EDIT)
    public ResponseResult<?> create(@Valid @RequestBody OperatorSaveDTO dto) {
        operatorService.create(dto);
        return ResponseResult.success();
    }

    @PostMapping("/update")
    @Operation(summary = "更新运营人员")
    @RequirePermission(Permissions.USER_EDIT)
    public ResponseResult<?> update(@RequestParam Long id,
                                     @Valid @RequestBody OperatorSaveDTO dto) {
        operatorService.update(id, dto);
        return ResponseResult.success();
    }

    @PostMapping("/update/status")
    @Operation(summary = "启用/停用运营人员")
    @RequirePermission(Permissions.USER_EDIT)
    public ResponseResult<?> updateStatus(@RequestParam Long id,
                                           @RequestParam String status) {
        operatorService.updateStatus(id, status);
        return ResponseResult.success();
    }
}
