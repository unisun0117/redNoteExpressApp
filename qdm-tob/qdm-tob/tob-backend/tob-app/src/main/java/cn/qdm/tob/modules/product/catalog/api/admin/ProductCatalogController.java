package cn.qdm.tob.modules.product.catalog.api.admin;

import cn.qdm.tob.framework.excel.annotation.Exportable;
import cn.qdm.tob.framework.excel.annotation.Importable;
import cn.qdm.tob.framework.model.ResponseResult;
import cn.qdm.tob.modules.product.catalog.service.ProductCatalogService;
import cn.qdm.tob.modules.product.catalog.vo.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品资料管理 API
 */
@RestController
@RequestMapping("/api/admin/product/catalog")
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    // ================================================================
    // 查询
    // ================================================================

    @GetMapping("/list")
    @Operation(summary = "分页查询商品资料列表")
    public ResponseResult<Page<ProductCatalogViewVO>> list(
            @Parameter(description = "页码") @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode,
            @Parameter(description = "商品条码（模糊）") @RequestParam(name = "productBarcode", required = false) String productBarcode,
            @Parameter(description = "商品名称（模糊）") @RequestParam(name = "productName", required = false) String productName,
            @Parameter(description = "状态") @RequestParam(name = "status", required = false) String status) {
        return ResponseResult.success(
                productCatalogService.page(pageNum, pageSize, salesRegionCode, productBarcode, productName, status));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询商品资料详情")
    public ResponseResult<ProductCatalogViewVO> get(
            @Parameter(description = "商品资料ID", required = true) @RequestParam(name = "id") Long id) {
        return ResponseResult.success(productCatalogService.getById(id));
    }

    // ================================================================
    // 新增 / 编辑
    // ================================================================

    @PostMapping
    @Operation(summary = "新增商品资料")
    public ResponseResult<?> create(@Valid @RequestBody ProductCatalogCreationVO vo) {
        productCatalogService.create(vo);
        return ResponseResult.success();
    }

    @PutMapping
    @Operation(summary = "编辑商品资料")
    public ResponseResult<?> update(
            @Parameter(description = "商品资料ID", required = true) @RequestParam(name = "id") Long id,
            @Valid @RequestBody ProductCatalogEditVO vo) {
        productCatalogService.update(id, vo);
        return ResponseResult.success();
    }

    // ================================================================
    // 库存调整
    // ================================================================

    @PutMapping("/adjust-stock")
    @Operation(summary = "调整今日可用库存")
    public ResponseResult<?> adjustStock(
            @Parameter(description = "商品资料ID", required = true) @RequestParam(name = "id") Long id,
            @Valid @RequestBody StockAdjustVO vo) {
        productCatalogService.adjustStock(id, vo);
        return ResponseResult.success();
    }

    // ================================================================
    // 辅助查询
    // ================================================================

    @GetMapping("/lookup-barcode")
    @Operation(summary = "条码反查商品名称（查询 prd_product 表）")
    public ResponseResult<SkuLookupVO> lookupBarcode(
            @Parameter(description = "商品条码", required = true) @RequestParam(name = "barcode") String barcode) {
        return ResponseResult.success(productCatalogService.lookupBarcode(barcode));
    }

    @GetMapping("/warehouses")
    @Operation(summary = "查询仓库列表（按大区过滤）")
    public ResponseResult<List<WarehouseSimpleVO>> getWarehouses(
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode) {
        return ResponseResult.success(productCatalogService.getWarehouses(salesRegionCode));
    }

    // ================================================================
    // 导出 / 导入
    // ================================================================

    @GetMapping("/export")
    @Operation(summary = "导出商品资料（排除图片和富文本，请求头加 Action: export）")
    @Exportable(name = "商品资料", templateType = ProductCatalogExportVO.class, dataPath = "data")
    public ResponseResult<List<ProductCatalogExportVO>> exportList(
            @Parameter(description = "销售大区编号") @RequestParam(name = "salesRegionCode", required = false) String salesRegionCode,
            @Parameter(description = "商品条码（模糊）") @RequestParam(name = "productBarcode", required = false) String productBarcode,
            @Parameter(description = "商品名称（模糊）") @RequestParam(name = "productName", required = false) String productName,
            @Parameter(description = "状态") @RequestParam(name = "status", required = false) String status) {
        return ResponseResult.success(
                productCatalogService.exportList(salesRegionCode, productBarcode, productName, status));
    }

    @PostMapping("/import")
    @Operation(summary = "导入商品资料（按销售大区+条码匹配更新/新增）")
    @Importable(enableValidation = true)
    public ResponseResult<ImportResultVO> importCatalog(
            @Parameter(hidden = true) List<ProductCatalogExportVO> importList) {
        return ResponseResult.success(productCatalogService.importCatalog(importList));
    }
}
