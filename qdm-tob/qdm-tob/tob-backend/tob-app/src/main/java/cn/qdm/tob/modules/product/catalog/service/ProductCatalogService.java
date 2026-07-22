package cn.qdm.tob.modules.product.catalog.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.product.catalog.ProductCatalogMapping;
import cn.qdm.tob.modules.product.catalog.api.internal.dto.ProductCatalogDTO;
import cn.qdm.tob.modules.product.catalog.domain.PrdProduct;
import cn.qdm.tob.modules.product.catalog.domain.ProductCatalog;
import cn.qdm.tob.modules.product.catalog.enums.ProductStatus;
import cn.qdm.tob.modules.product.catalog.mapper.PrdProductMapper;
import cn.qdm.tob.modules.product.catalog.mapper.ProductCatalogMapper;
import cn.qdm.tob.modules.product.catalog.vo.*;
import cn.qdm.tob.modules.system.warehouse.api.internal.WarehouseApi;
import cn.qdm.tob.modules.system.warehouse.api.internal.dto.WarehouseDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品资料 CRUD 服务
 */
@Service
@RequiredArgsConstructor
public class ProductCatalogService extends TobBaseService<ProductCatalogMapper, ProductCatalog> {

    private final ProductCatalogMapper productCatalogMapper;
    private final PrdProductMapper prdProductMapper;
    private final WarehouseApi warehouseApi;
    private final ProductCatalogMapping mapping;

    // ================================================================
    // 分页查询
    // ================================================================

    /**
     * 分页查询商品资料列表
     */
    public Page<ProductCatalogViewVO> page(Integer pageNum, Integer pageSize,
                                           String salesRegionCode, String productBarcode,
                                           String productName, String status) {
        LambdaQueryWrapper<ProductCatalog> wrapper = new LambdaQueryWrapper<>();
        if (salesRegionCode != null && !salesRegionCode.isBlank()) {
            wrapper.eq(ProductCatalog::getSalesRegionCode, salesRegionCode);
        }
        if (productBarcode != null && !productBarcode.isBlank()) {
            wrapper.like(ProductCatalog::getProductBarcode, productBarcode);
        }
        if (productName != null && !productName.isBlank()) {
            wrapper.like(ProductCatalog::getProductName, productName);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(ProductCatalog::getStatus, status);
        }
        wrapper.orderByDesc(ProductCatalog::getUpdatedAt);

        Page<ProductCatalog> entityPage = productCatalogMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        Page<ProductCatalogViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(mapping.toViewList(entityPage.getRecords()));
        return voPage;
    }

    // ================================================================
    // 详情
    // ================================================================

    /**
     * 按 ID 查询详情
     */
    public ProductCatalogViewVO getById(Long id) {
        ProductCatalog entity = productCatalogMapper.selectById(id);
        AssertUtils.notNull(entity, "商品资料不存在");
        return mapping.toView(entity);
    }

    // ================================================================
    // 新增
    // ================================================================

    /**
     * 新增商品资料
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(ProductCatalogCreationVO vo) {
        // 1. 校验联合主键唯一性
        boolean exists = productCatalogMapper.lambdaExists(w ->
                w.eq(ProductCatalog::getSalesRegionCode, vo.getSalesRegionCode())
                 .eq(ProductCatalog::getProductBarcode, vo.getProductBarcode()));
        AssertUtils.isFalse(exists, "该销售大区下已存在相同商品条码，请检查");

        // 2. 条码反查商品名称（查询 prd_product 表）
        PrdProduct product = prdProductMapper.lambdaSelectOne(
                w -> w.eq(PrdProduct::getBarcode, vo.getProductBarcode()))
                .orElse(null);
        AssertUtils.notNull(product, "未找到该商品条码");

        // 3. 校验订购约束逻辑关系
        validateOrderConstraints(vo.getOrderBaseQty(), vo.getOrderMinQty(), vo.getOrderMaxQty());

        // 4. 组装实体
        ProductCatalog entity = mapping.toEntity(vo);
        entity.setProductName(product.getName());
        entity.setSalesRegionName(vo.getSalesRegionName());

        // 查询仓库名称（通过 internal API 跨模块调用）
        if (vo.getWarehouseCode() != null && !vo.getWarehouseCode().isBlank()) {
            WarehouseDTO warehouse = warehouseApi.getByCode(vo.getWarehouseCode());
            if (warehouse != null) {
                entity.setWarehouseName(warehouse.getName());
            }
        }

        // 5. 默认值
        if (entity.getStatus() == null ) {
            entity.setStatus(ProductStatus.LISTED);
        }
        // JSON 类型字段不接受空字符串，null 化处理
        if (entity.getCarouselImages() != null && entity.getCarouselImages().isBlank()) {
            entity.setCarouselImages(null);
        }
        if (entity.getMainImage() != null && entity.getMainImage().isBlank()) {
            entity.setMainImage(null);
        }
        if (entity.getProductDetail() != null && entity.getProductDetail().isBlank()) {
            entity.setProductDetail(null);
        }
        entity.setDailyAvailable(vo.getDailyStock());   // 今日可用数量 = 每日可用库存
        entity.setDailySold(BigDecimal.ZERO);            // 今日已售数量 = 0

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        productCatalogMapper.insert(entity);
    }

    // ================================================================
    // 编辑
    // ================================================================

    /**
     * 编辑商品资料（仅允许修改非锁定字段）
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductCatalogEditVO vo) {
        ProductCatalog existing = productCatalogMapper.selectById(id);
        AssertUtils.notNull(existing, "商品资料不存在");

        // 校验订购约束
        if (vo.getOrderBaseQty() != null && vo.getOrderMinQty() != null && vo.getOrderMaxQty() != null) {
            validateOrderConstraints(vo.getOrderBaseQty(), vo.getOrderMinQty(), vo.getOrderMaxQty());
        }

        // 仅更新允许修改的字段
        if (vo.getWarehouseCode() != null) {
            existing.setWarehouseCode(vo.getWarehouseCode());
            // 查询仓库名称（通过 internal API 跨模块调用）
            WarehouseDTO warehouse = warehouseApi.getByCode(vo.getWarehouseCode());
            if (warehouse != null) {
                existing.setWarehouseName(warehouse.getName());
            }
        }
        if (vo.getStatus() != null) {
            existing.setStatus(vo.getStatus());
        }
        if (vo.getMiniappName() != null) {
            existing.setMiniappName(vo.getMiniappName());
        }
        if (vo.getMainImage() != null) {
            existing.setMainImage(vo.getMainImage().isBlank() ? null : vo.getMainImage());
        }
        if (vo.getCarouselImages() != null) {
            existing.setCarouselImages(vo.getCarouselImages().isBlank() ? null : vo.getCarouselImages());
        }
        if (vo.getProductDetail() != null) {
            existing.setProductDetail(vo.getProductDetail().isBlank() ? null : vo.getProductDetail());
        }
        if (vo.getOrderBaseQty() != null) {
            existing.setOrderBaseQty(vo.getOrderBaseQty());
        }
        if (vo.getOrderMinQty() != null) {
            existing.setOrderMinQty(vo.getOrderMinQty());
        }
        if (vo.getOrderMaxQty() != null) {
            existing.setOrderMaxQty(vo.getOrderMaxQty());
        }
        if (vo.getUpdatedBy() != null) {
            existing.setUpdatedBy(vo.getUpdatedBy());
        }
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));

        productCatalogMapper.updateById(existing);
    }

    // ================================================================
    // 库存调整
    // ================================================================

    /**
     * 调整库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long id, StockAdjustVO vo) {
        ProductCatalog existing = productCatalogMapper.selectById(id);
        AssertUtils.notNull(existing, "商品资料不存在");

        BigDecimal newDailyStock = vo.getNewDailyStock();
        BigDecimal newDailyAvailable = newDailyStock.subtract(existing.getDailySold());

        AssertUtils.isTrue(newDailyAvailable.compareTo(BigDecimal.ZERO) >= 0,
                "今日可用数量不能为负数，当前已售数量为 " + existing.getDailySold());

        existing.setDailyStock(newDailyStock);
        existing.setDailyAvailable(newDailyAvailable);
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));

        productCatalogMapper.updateById(existing);
    }

    // ================================================================
    // 条码反查 / 大区下拉 / 仓库下拉
    // ================================================================

    /**
     * 按条码反查商品名称（查询 prd_product 表）
     */
    public SkuLookupVO lookupBarcode(String barcode) {
        PrdProduct product = prdProductMapper.lambdaSelectOne(
                w -> w.eq(PrdProduct::getBarcode, barcode))
                .orElse(null);
        AssertUtils.notNull(product, "未找到该商品条码");

        SkuLookupVO vo = new SkuLookupVO();
        vo.setBarcode(product.getBarcode());
        vo.setProductName(product.getName());
        vo.setSpec(product.getSpec());
        vo.setUnit(product.getUnit1());
        return vo;
    }

    /**
     * 查询仓库列表（联动大区过滤，通过 internal API 跨模块调用）
     */
    public List<WarehouseSimpleVO> getWarehouses(String salesRegionCode) {
        return warehouseApi.listByRegion(salesRegionCode).stream()
                .map(wh -> WarehouseSimpleVO.builder()
                        .code(wh.getCode())
                        .name(wh.getName())
                        .region(wh.getRegion())
                        .build())
                .collect(Collectors.toList());
    }

    // ================================================================
    // 导出
    // ================================================================

    /**
     * 导出商品资料（排除图片和富文本字段）
     */
    public List<ProductCatalogExportVO> exportList(String salesRegionCode, String productBarcode,
                                                    String productName, String status) {
        LambdaQueryWrapper<ProductCatalog> wrapper = new LambdaQueryWrapper<>();
        if (salesRegionCode != null && !salesRegionCode.isBlank()) {
            wrapper.eq(ProductCatalog::getSalesRegionCode, salesRegionCode);
        }
        if (productBarcode != null && !productBarcode.isBlank()) {
            wrapper.like(ProductCatalog::getProductBarcode, productBarcode);
        }
        if (productName != null && !productName.isBlank()) {
            wrapper.like(ProductCatalog::getProductName, productName);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(ProductCatalog::getStatus, status);
        }
        wrapper.orderByDesc(ProductCatalog::getUpdatedAt);
        // 上限 10000 条
        List<ProductCatalog> entities = productCatalogMapper.selectList(
                new Page<>(1, 10000), wrapper);
        return mapping.toExportList(entities);
    }

    // ================================================================
    // 导入
    // ================================================================

    /**
     * 导入商品资料（按「销售大区+条码」匹配更新/新增）
     * <p>
     * 入参 importList 由框架层 {@code @Importable} 拦截器自动解析 Excel 文件注入。
     */
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importCatalog(List<ProductCatalogExportVO> importList) {
        ImportResultVO result = new ImportResultVO();

        for (int i = 0; i < importList.size(); i++) {
            ProductCatalogExportVO row = importList.get(i);
            int rowNum = i + 2; // Excel 行号（跳过表头）
            try {
                // 必填校验已由 @Importable(enableValidation = true) + @NotBlank 覆盖
                // 此处做业务级匹配

                // 匹配已有记录
                ProductCatalog existing = productCatalogMapper.lambdaSelectOne(
                        w -> w.eq(ProductCatalog::getSalesRegionCode, row.getSalesRegionCode())
                             .eq(ProductCatalog::getProductBarcode, row.getProductBarcode()))
                        .orElse(null);

                if (existing != null) {
                    // 更新模式
                    applyImportUpdate(existing, row);
                    productCatalogMapper.updateById(existing);
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                } else {
                    // 新增模式
                    ProductCatalog entity = buildImportEntity(row);
                    productCatalogMapper.insert(entity);
                    result.setCreatedCount(result.getCreatedCount() + 1);
                }
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }
        return result;
    }

    private void applyImportUpdate(ProductCatalog entity, ProductCatalogExportVO row) {
        // warehouseCode / orderBaseQty / orderMinQty / orderMaxQty
        // 已由 @Importable(enableValidation = true) + @NotBlank / @NotNull 保证非空
        entity.setWarehouseCode(row.getWarehouseCode());
        entity.setOrderBaseQty(row.getOrderBaseQty());
        entity.setOrderMinQty(row.getOrderMinQty());
        entity.setOrderMaxQty(row.getOrderMaxQty());

        // 以下字段无校验注解，仅在用户填写时更新
        if (row.getStatus() != null) {
            entity.setStatus(row.getStatus());
        }
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
    }

    private ProductCatalog buildImportEntity(ProductCatalogExportVO row) {
        ProductCatalog entity = new ProductCatalog();
        entity.setSalesRegionCode(row.getSalesRegionCode());
        entity.setSalesRegionName(row.getSalesRegionName());
        entity.setProductBarcode(row.getProductBarcode());

        // 条码反查商品名称（查询 prd_product 表）
        prdProductMapper.lambdaSelectOne(
                w -> w.eq(PrdProduct::getBarcode, row.getProductBarcode()))
                .ifPresent(p -> entity.setProductName(p.getName()));
        if (isBlank(entity.getProductName())) {
            entity.setProductName(row.getProductName());
        }

        entity.setWarehouseCode(row.getWarehouseCode());
        entity.setWarehouseName(row.getWarehouseName());
        entity.setStatus(row.getStatus());
        entity.setMiniappName(row.getMiniappName());

        // 订购约束（@NotNull 校验保证非 null）
        entity.setOrderBaseQty(row.getOrderBaseQty());
        entity.setOrderMinQty(row.getOrderMinQty());
        entity.setOrderMaxQty(row.getOrderMaxQty());

        // 库存
        BigDecimal stock = row.getDailyStock() != null ? row.getDailyStock() : BigDecimal.ZERO;
        entity.setDailyStock(stock);
        entity.setDailyAvailable(stock);
        entity.setDailySold(BigDecimal.ZERO);

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return entity;
    }

    // ================================================================
    // 工具方法
    // ================================================================

    /**
     * 校验订购约束：基数 ≤ 下限 ≤ 上限
     */
    private void validateOrderConstraints(BigDecimal baseQty, BigDecimal minQty, BigDecimal maxQty) {
        AssertUtils.isTrue(baseQty.compareTo(minQty) <= 0,
                "订购基数(" + baseQty + ")不能大于订购下限(" + minQty + ")");
        AssertUtils.isTrue(minQty.compareTo(maxQty) <= 0,
                "订购下限(" + minQty + ")不能大于订购上限(" + maxQty + ")");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * 按条码列表查询商品+价格（INNER JOIN 价格表，仅返回有售价的商品）
     */
    public List<ProductCatalogDTO> listByBarcodesWithPrice(String priceGroupCode,
                                                           List<String> barcodes) {
        if (CollectionUtils.isEmpty(barcodes)) {
            return List.of();
        }
        return productCatalogMapper.listByBarcodesWithPrice(priceGroupCode, barcodes);
    }

    // ================================================================
    // 小程序商品列表
    // ================================================================

    /**
     * 小程序端商品列表（INNER JOIN 价格表，仅返回有售价的已上架商品）
     */
    public Page<ProductMallVO> listForMall(String priceGroupCode, int pageNum, int pageSize) {
        return productCatalogMapper.listForMall(new Page<>(pageNum, pageSize), priceGroupCode);
    }
}
