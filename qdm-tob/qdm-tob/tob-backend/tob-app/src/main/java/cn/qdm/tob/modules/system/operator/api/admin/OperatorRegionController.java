package cn.qdm.tob.modules.system.operator.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.system.operator.service.OperatorRegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 运营人员-销售大区数据权限绑定 API。
 * <p>提供查询用户已绑定大区和覆盖式保存绑定的功能，数据权限配置弹窗使用。</p>
 */
@Tag(name = "运营人员-销售大区数据权限")
@RestController
@RequestMapping("/api/admin/operator")
@RequiredArgsConstructor
public class OperatorRegionController {

    private final OperatorRegionService operatorRegionService;

    /**
     * 查询运营人员已绑定的销售大区 ID 列表。
     *
     * @param operatorId 运营人员 ID
     * @return 已绑定的大区 ID 列表，无绑定时返回空列表
     */
    @GetMapping("/regions")
    @Operation(summary = "查询运营人员已绑定的销售大区")
    public ResponseResult<List<String>> getRegions(
            @Parameter(description = "运营人员ID", required = true) @RequestParam Long operatorId) {
        return ResponseResult.success(operatorRegionService.getRegionCodes(operatorId));
    }

    /**
     * 覆盖式设置运营人员的销售大区绑定。
     * <p>通过 region_code 关联，不做有效性校验。</p>
     *
     * @param operatorId  运营人员 ID
     * @param regionCodes 销售大区编号集合，传空集合则清空所有绑定
     */
    @PutMapping("/regions")
    @Operation(summary = "覆盖式设置运营人员的销售大区绑定（通过 region_code）")
    public ResponseResult<?> setRegions(
            @Parameter(description = "运营人员ID", required = true) @RequestParam Long operatorId,
            @Parameter(description = "销售大区编号集合", required = true) @RequestBody Set<String> regionCodes) {
        operatorRegionService.setRegions(operatorId, regionCodes);
        return ResponseResult.success();
    }
}
