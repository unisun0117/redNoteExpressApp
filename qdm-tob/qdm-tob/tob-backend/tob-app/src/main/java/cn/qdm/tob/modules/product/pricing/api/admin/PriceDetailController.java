package cn.qdm.tob.modules.product.pricing.api.admin;

import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.excel.annotation.Importable;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.infrastructure.security.Permissions;
import cn.qdm.tob.infrastructure.security.RequirePermission;
import cn.qdm.tob.modules.product.pricing.service.PriceDetailService;
import cn.qdm.tob.modules.product.pricing.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格组明细管理 API
 */
@Tag(name = "价格组明细管理")
@RestController
@RequestMapping("/api/admin/product/price-detail")
@RequiredArgsConstructor
public class PriceDetailController {

    private final PriceDetailService priceDetailService;

    // ================================================================
    // 查询
    // ================================================================

    @GetMapping("/list")
    @Operation(summary = "分页查询价格组明细列表")
    @RequirePermission(Permissions.PRICE_DETAIL_LIST)
    public ResponseResult<Page<PriceDetailViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode,
            @Parameter(description = "价格组名称（模糊）") @RequestParam(name = "priceGroupName", required = false) String priceGroupName,
            @Parameter(description = "商品条码/名称（模糊）") @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseResult.success(
                priceDetailService.page(pageNum, pageSize, salesRegionCode, priceGroupName, keyword));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询价格组明细详情")
    @RequirePermission(Permissions.PRICE_DETAIL_DETAIL)
    public ResponseResult<PriceDetailViewVO> get(
            @Parameter(description = "明细ID", required = true) @RequestParam(name = "id") Long id) {
        return ResponseResult.success(priceDetailService.getById(id));
    }

    // ================================================================
    // 新增 / 编辑
    // ================================================================

    @PostMapping
    @Operation(summary = "新增价格组明细")
    @RequirePermission(Permissions.PRICE_DETAIL_CREATE)
    public ResponseResult<?> create(@Valid @RequestBody PriceDetailCreationVO vo) {
        priceDetailService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping
    @Operation(summary = "编辑价格组明细（含价格变动审批判断）")
    @RequirePermission(Permissions.PRICE_DETAIL_UPDATE)
    public ResponseResult<PriceDetailEditResultVO> update(@Valid @RequestBody PriceDetailEditVO vo) {
        return ResponseResult.success(priceDetailService.update(vo));
    }

    // ================================================================
    // 辅助查询
    // ================================================================

    @GetMapping("/lookup-barcode")
    @Operation(summary = "条码反查商品名称")
    @RequirePermission(Permissions.PRICE_DETAIL_LOOKUP)
    public ResponseResult<PriceDetailViewVO> lookupBarcode(
            @Parameter(description = "商品条码", required = true) @RequestParam(name = "barcode") String barcode) {
        return ResponseResult.success(priceDetailService.lookupBarcode(barcode));
    }

    // ================================================================
    // 导出 / 导入
    // ================================================================

    @GetMapping("/export")
    @Operation(summary = "导出价格组明细（请求头加 Action: export）")
    @Exportable(name = "价格组明细", templateType = PriceDetailExportVO.class, dataPath = "data")
    @RequirePermission(Permissions.PRICE_DETAIL_EXPORT)
    public ResponseResult<List<PriceDetailExportVO>> exportList(
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode,
            @Parameter(description = "价格组名称（模糊）") @RequestParam(name = "priceGroupName", required = false) String priceGroupName,
            @Parameter(description = "商品条码/名称（模糊）") @RequestParam(name = "keyword", required = false) String keyword) {
        return ResponseResult.success(
                priceDetailService.exportList(salesRegionCode, priceGroupName, keyword));
    }

    @PostMapping("/import")
    @Operation(summary = "导入价格组明细（按销售大区+价格组编码+商品条码匹配，不触发审批）")
    @Importable(enableValidation = true)
    @RequirePermission(Permissions.PRICE_DETAIL_IMPORT)
    public ResponseResult<PriceDetailImportResultVO> importDetails(
            @Parameter(hidden = true) List<PriceDetailExportVO> importList) {
        return ResponseResult.success(priceDetailService.importDetails(importList));
    }
}
