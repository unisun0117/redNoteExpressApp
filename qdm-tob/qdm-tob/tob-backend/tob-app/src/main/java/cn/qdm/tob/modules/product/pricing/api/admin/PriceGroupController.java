package cn.qdm.tob.modules.product.pricing.api.admin;

import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.product.pricing.service.PriceGroupService;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupCreationVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupEditVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupViewVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格组管理 API
 */
@Tag(name = "价格组管理")
@RestController
@RequestMapping("/api/admin/product/price-group")
@RequiredArgsConstructor
public class PriceGroupController {

    private final PriceGroupService priceGroupService;

    // ================================================================
    // 查询
    // ================================================================

    @GetMapping("/list")
    @Operation(summary = "分页查询价格组列表")
    @RequirePermission(Permissions.PRICE_GROUP_LIST)
    public ResponseResult<Page<PriceGroupViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode,
            @Parameter(description = "价格组名称（模糊）") @RequestParam(name = "priceGroupName", required = false) String priceGroupName) {
        return ResponseResult.success(
                priceGroupService.page(pageNum, pageSize, salesRegionCode, priceGroupName));
    }

    @GetMapping("/options")
    @Operation(summary = "按销售大区查询价格组下拉选项")
    @RequirePermission(Permissions.PRICE_GROUP_OPTIONS)
    public ResponseResult<List<PriceGroupViewVO>> options(
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode) {
        return ResponseResult.success(priceGroupService.listOptions(salesRegionCode));
    }

    // ================================================================
    // 新增 / 编辑
    // ================================================================

    @PostMapping
    @Operation(summary = "新增价格组")
    @RequirePermission(Permissions.PRICE_GROUP_CREATE)
    public ResponseResult<?> create(@Valid @RequestBody PriceGroupCreationVO vo) {
        priceGroupService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping
    @Operation(summary = "编辑价格组（仅名称和描述可修改）")
    @RequirePermission(Permissions.PRICE_GROUP_UPDATE)
    public ResponseResult<?> update(@Valid @RequestBody PriceGroupEditVO vo) {
        priceGroupService.update(vo);
        return ResponseResult.success();
    }
}
