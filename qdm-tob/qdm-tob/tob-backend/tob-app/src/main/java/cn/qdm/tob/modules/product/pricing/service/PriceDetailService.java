package cn.qdm.tob.modules.product.pricing.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.SalesRegionApi;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionDetailDTO;
import cn.qdm.tob.modules.product.catalog.domain.PrdProduct;
import cn.qdm.tob.modules.product.catalog.domain.ProductCatalog;
import cn.qdm.tob.modules.product.catalog.mapper.PrdProductMapper;
import cn.qdm.tob.modules.product.catalog.mapper.ProductCatalogMapper;
import cn.qdm.tob.modules.product.pricing.PriceDetailMapping;
import cn.qdm.tob.modules.product.pricing.api.internal.dto.PriceDetailDTO;
import cn.qdm.tob.modules.product.pricing.domain.ApprovalStatus;
import cn.qdm.tob.modules.product.pricing.domain.PrdPriceDetail;
import cn.qdm.tob.modules.product.pricing.domain.PrdPriceGroup;
import cn.qdm.tob.modules.product.pricing.mapper.PriceDetailMapper;
import cn.qdm.tob.modules.product.pricing.mapper.PriceGroupMapper;
import cn.qdm.tob.modules.product.pricing.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 价格组明细 CRUD 服务
 * <p>
 * 核心逻辑：
 * - 新增：条码反查商品名称，校验唯一性
 * - 编辑：计算价格变动比率，超阈值走审批
 * - 导入：匹配更新/新增，不触发审批
 */
@Service
@RequiredArgsConstructor
public class PriceDetailService extends TobBaseService<PriceDetailMapper, PrdPriceDetail> {

    private final PriceDetailMapper priceDetailMapper;
    private final PriceGroupMapper priceGroupMapper;
    private final PrdProductMapper prdProductMapper;
    private final ProductCatalogMapper productCatalogMapper;
    private final SalesRegionApi salesRegionApi;
    private final PriceDetailMapping mapping;

    /** 默认审批阈值 30%（当销售大区未配置时使用） */
    private static final BigDecimal DEFAULT_APPROVAL_THRESHOLD = new BigDecimal("30");

    // ================================================================
    // 分页查询
    // ================================================================

    /**
     * 分页查询价格组明细列表
     * <p>
     * priceGroupName 过滤改为两步：先查价格组表按名称模糊匹配得到编码列表，再按编码过滤明细。
     * priceGroupName 展示通过批量联查 prd_price_group 填充。
     */
    public Page<PriceDetailViewVO> page(Integer pageNum, Integer pageSize,
                                        String salesRegionCode, String priceGroupName,
                                        String keyword) {
        LambdaQueryWrapper<PrdPriceDetail> wrapper = new LambdaQueryWrapper<>();
        if (salesRegionCode != null && !salesRegionCode.isBlank()) {
            wrapper.eq(PrdPriceDetail::getSalesRegionCode, salesRegionCode);
        }
        if (priceGroupName != null && !priceGroupName.isBlank()) {
            // 先按名称模糊匹配价格组 → 得到编码列表
            List<PrdPriceGroup> matchedGroups = priceGroupMapper.lambdaSelect(
                    w -> w.like(PrdPriceGroup::getPriceGroupName, priceGroupName));
            List<String> matchedCodes = matchedGroups.stream()
                    .map(PrdPriceGroup::getPriceGroupCode)
                    .distinct()
                    .collect(Collectors.toList());
            if (matchedCodes.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            wrapper.in(PrdPriceDetail::getPriceGroupCode, matchedCodes);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(PrdPriceDetail::getProductBarcode, keyword)
                              .or()
                              .like(PrdPriceDetail::getProductName, keyword));
        }
        wrapper.orderByDesc(PrdPriceDetail::getUpdatedAt);

        Page<PrdPriceDetail> entityPage = priceDetailMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        List<PriceDetailViewVO> voList = mapping.toViewList(entityPage.getRecords());
        fillPriceGroupNames(voList);
        Page<PriceDetailViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    // ================================================================
    // 详情
    // ================================================================

    /**
     * 按 ID 查询详情（priceGroupName 通过联查 prd_price_group 获取）
     */
    public PriceDetailViewVO getById(Long id) {
        PrdPriceDetail entity = priceDetailMapper.selectById(id);
        AssertUtils.notNull(entity, "价格明细不存在");
        PriceDetailViewVO vo = mapping.toView(entity);
        fillPriceGroupName(vo);
        return vo;
    }

    // ================================================================
    // 新增
    // ================================================================

    /**
     * 新增价格组明细
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(PriceDetailCreationVO vo) {
        // 1. 校验价格组存在
        PrdPriceGroup priceGroup = priceGroupMapper.lambdaSelectOne(
                w -> w.eq(PrdPriceGroup::getSalesRegionCode, vo.getSalesRegionCode())
                     .eq(PrdPriceGroup::getPriceGroupCode, vo.getPriceGroupCode()))
                .orElse(null);
        AssertUtils.notNull(priceGroup, "该销售大区下不存在此价格组");

        // 2. 校验该条码在当前销售大区的商品资料中已录入
        ProductCatalog catalog = productCatalogMapper.lambdaSelectOne(
                w -> w.eq(ProductCatalog::getSalesRegionCode, vo.getSalesRegionCode())
                     .eq(ProductCatalog::getProductBarcode, vo.getProductBarcode()))
                .orElse(null);
        AssertUtils.notNull(catalog, "该商品条码在商品资料中不存在，请检查");

        // 3. 校验联合主键唯一性
        boolean exists = priceDetailMapper.lambdaExists(w ->
                w.eq(PrdPriceDetail::getSalesRegionCode, vo.getSalesRegionCode())
                 .eq(PrdPriceDetail::getPriceGroupCode, vo.getPriceGroupCode())
                 .eq(PrdPriceDetail::getProductBarcode, vo.getProductBarcode()));
        AssertUtils.isFalse(exists, "该价格组下已存在相同商品条码的价格明细");

        // 4. 组装实体
        PrdPriceDetail entity = mapping.toEntity(vo);
        entity.setProductName(catalog.getProductName());
        entity.setApprovalStatus(ApprovalStatus.NONE);

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        priceDetailMapper.insert(entity);
    }

    // ================================================================
    // 条码反查
    // ================================================================

    /**
     * 按条码反查商品名称（查询 prd_product 表）
     */
    public PriceDetailViewVO lookupBarcode(String barcode) {
        PrdProduct product = prdProductMapper.lambdaSelectOne(
                w -> w.eq(PrdProduct::getBarcode, barcode))
                .orElse(null);
        AssertUtils.notNull(product, "该商品条码在商品资料中不存在，请检查");

        PriceDetailViewVO vo = new PriceDetailViewVO();
        vo.setProductBarcode(product.getBarcode());
        vo.setProductName(product.getName());
        return vo;
    }

    // ================================================================
    // 编辑（含价格变动审批逻辑）
    // ================================================================

    /**
     * 编辑价格组明细
     * <p>
     * 价格变动比率 = |新售价 - 原售价| / 原售价 × 100%
     * - 变动比率 ≤ 阈值 → 直接更新
     * - 变动比率 > 阈值 → 生成待审批记录
     */
    @Transactional(rollbackFor = Exception.class)
    public PriceDetailEditResultVO update(PriceDetailEditVO vo) {
        PrdPriceDetail existing = priceDetailMapper.selectById(vo.getId());
        AssertUtils.notNull(existing, "价格明细不存在");

        // 检查是否有正在审批中的记录
        AssertUtils.isTrue(existing.getApprovalStatus() != ApprovalStatus.PENDING,
                "该价格明细已有待审批的变更，请等待审批完成后再操作");

        // 联查商品资料表，刷新 productName
        if (existing.getProductBarcode() != null && !existing.getProductBarcode().isBlank()) {
            PrdProduct product = prdProductMapper.lambdaSelectOne(
                    w -> w.eq(PrdProduct::getBarcode, existing.getProductBarcode()))
                    .orElse(null);
            if (product != null) {
                existing.setProductName(product.getName());
            }
        }

        BigDecimal oldPrice = existing.getPrice();
        BigDecimal newPrice = vo.getPrice();

        // 计算变动比率
        BigDecimal changeRatio = calculateChangeRatio(oldPrice, newPrice);

        // 获取销售大区的审批配置
        BigDecimal threshold = getApprovalThreshold(existing.getSalesRegionCode());

        PriceDetailEditResultVO result = new PriceDetailEditResultVO();
        result.setChangeRatio(changeRatio);

        if (changeRatio.compareTo(threshold) > 0) {
            // 超阈值 → 生成待审批记录，原售价保持不变
            existing.setApprovalStatus(ApprovalStatus.PENDING);
            existing.setPendingPrice(newPrice);
            existing.setChangeReason(vo.getChangeReason());
            if (vo.getUpdatedBy() != null) {
                existing.setUpdatedBy(vo.getUpdatedBy());
            }
            existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
            priceDetailMapper.updateById(existing);

            result.setApprovalRequired(true);
            result.setMessage("价格变动比率 " + changeRatio.setScale(2, RoundingMode.HALF_UP)
                    + "% 超过阈值 " + threshold + "%，已生成待审批记录，审批完成后生效");
        } else {
            // 未超阈值 → 直接更新售价
            existing.setPrice(newPrice);
            existing.setChangeReason(vo.getChangeReason());
            existing.setApprovalStatus(ApprovalStatus.NONE);
            existing.setPendingPrice(null);
            if (vo.getUpdatedBy() != null) {
                existing.setUpdatedBy(vo.getUpdatedBy());
            }
            existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
            priceDetailMapper.updateById(existing);

            result.setApprovalRequired(false);
            result.setMessage("价格更新成功");
        }

        return result;
    }

    // ================================================================
    // 导出
    // ================================================================

    /**
     * 导出价格组明细（按筛选条件，上限 10,000 条）
     */
    public List<PriceDetailExportVO> exportList(String salesRegionCode, String priceGroupName,
                                                 String keyword) {
        LambdaQueryWrapper<PrdPriceDetail> wrapper = new LambdaQueryWrapper<>();
        if (salesRegionCode != null && !salesRegionCode.isBlank()) {
            wrapper.eq(PrdPriceDetail::getSalesRegionCode, salesRegionCode);
        }
        if (priceGroupName != null && !priceGroupName.isBlank()) {
            List<PrdPriceGroup> matchedGroups = priceGroupMapper.lambdaSelect(
                    w -> w.like(PrdPriceGroup::getPriceGroupName, priceGroupName));
            List<String> matchedCodes = matchedGroups.stream()
                    .map(PrdPriceGroup::getPriceGroupCode)
                    .distinct()
                    .collect(Collectors.toList());
            if (matchedCodes.isEmpty()) {
                return List.of();
            }
            wrapper.in(PrdPriceDetail::getPriceGroupCode, matchedCodes);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(PrdPriceDetail::getProductBarcode, keyword)
                              .or()
                              .like(PrdPriceDetail::getProductName, keyword));
        }
        wrapper.orderByDesc(PrdPriceDetail::getUpdatedAt);

        List<PrdPriceDetail> entities = priceDetailMapper.selectList(
                new Page<>(1, 10000), wrapper);
        List<PriceDetailExportVO> exportList = mapping.toExportList(entities);
        fillPriceGroupNamesForExport(exportList);
        return exportList;
    }

    // ================================================================
    // 导入
    // ================================================================

    /**
     * 导入价格组明细
     * <p>
     * 匹配键：销售大区 + 价格组编码 + 商品条码
     * - 匹配到已有记录 → 更新售价（不触发审批，变动原因记录为"批量导入"）
     * - 未匹配到 → 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public PriceDetailImportResultVO importDetails(List<PriceDetailExportVO> importList) {
        PriceDetailImportResultVO result = new PriceDetailImportResultVO();

        for (int i = 0; i < importList.size(); i++) {
            PriceDetailExportVO row = importList.get(i);
            int rowNum = i + 2; // Excel 行号（跳过表头）
            try {
                // 校验价格组存在
                PrdPriceGroup priceGroup = priceGroupMapper.lambdaSelectOne(
                        w -> w.eq(PrdPriceGroup::getSalesRegionCode, row.getSalesRegionCode())
                             .eq(PrdPriceGroup::getPriceGroupCode, row.getPriceGroupCode()))
                        .orElse(null);
                AssertUtils.notNull(priceGroup, "价格组不存在：销售大区="
                        + row.getSalesRegionCode() + " 编码=" + row.getPriceGroupCode());

                // 条码反查商品名称（查询 prd_product 表）
                String productName = row.getProductName();
                if (productName == null || productName.isBlank()) {
                    PrdProduct product = prdProductMapper.lambdaSelectOne(
                            w -> w.eq(PrdProduct::getBarcode, row.getProductBarcode()))
                            .orElse(null);
                    if (product != null) {
                        productName = product.getName();
                    }
                }

                // 匹配已有记录
                PrdPriceDetail existing = priceDetailMapper.lambdaSelectOne(
                        w -> w.eq(PrdPriceDetail::getSalesRegionCode, row.getSalesRegionCode())
                             .eq(PrdPriceDetail::getPriceGroupCode, row.getPriceGroupCode())
                             .eq(PrdPriceDetail::getProductBarcode, row.getProductBarcode()))
                        .orElse(null);

                LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

                if (existing != null) {
                    // 更新模式（不触发审批）
                    existing.setPrice(row.getPrice());
                    existing.setChangeReason("批量导入");
                    existing.setApprovalStatus(ApprovalStatus.NONE);
                    existing.setPendingPrice(null);
                    existing.setUpdatedAt(now);
                    if (row.getUpdatedBy() != null) {
                        existing.setUpdatedBy(row.getUpdatedBy());
                    }
                    priceDetailMapper.updateById(existing);
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                } else {
                    // 新增模式
                    PrdPriceDetail entity = new PrdPriceDetail();
                    entity.setSalesRegionCode(row.getSalesRegionCode());
                    entity.setPriceGroupCode(row.getPriceGroupCode());
                    entity.setProductBarcode(row.getProductBarcode());
                    entity.setProductName(productName != null ? productName : "");
                    entity.setPrice(row.getPrice());
                    entity.setChangeReason("批量导入");
                    entity.setApprovalStatus(ApprovalStatus.NONE);
                    entity.setCreatedAt(now);
                    entity.setUpdatedAt(now);
                    if (row.getUpdatedBy() != null) {
                        entity.setCreatedBy(row.getUpdatedBy());
                        entity.setUpdatedBy(row.getUpdatedBy());
                    }
                    priceDetailMapper.insert(entity);
                    result.setCreatedCount(result.getCreatedCount() + 1);
                }
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }
        return result;
    }

    // ================================================================
    // 工具方法
    // ================================================================

    /**
     * 批量填充 priceGroupName 到 ViewVO 列表（从 prd_price_group 联查）
     */
    private void fillPriceGroupNames(List<PriceDetailViewVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        Map<String, String> nameMap = buildGroupNameMap(voList);
        for (PriceDetailViewVO vo : voList) {
            String key = buildGroupKey(vo.getSalesRegionCode(), vo.getPriceGroupCode());
            vo.setPriceGroupName(nameMap.getOrDefault(key, ""));
        }
    }

    /**
     * 填充单个 ViewVO 的 priceGroupName
     */
    private void fillPriceGroupName(PriceDetailViewVO vo) {
        fillPriceGroupNames(List.of(vo));
    }

    /**
     * 批量填充 priceGroupName 到 ExportVO 列表
     */
    private void fillPriceGroupNamesForExport(List<PriceDetailExportVO> exportList) {
        if (exportList == null || exportList.isEmpty()) {
            return;
        }
        Map<String, String> nameMap = buildGroupNameMapForExport(exportList);
        for (PriceDetailExportVO vo : exportList) {
            String key = buildGroupKey(vo.getSalesRegionCode(), vo.getPriceGroupCode());
            vo.setPriceGroupName(nameMap.getOrDefault(key, ""));
        }
    }

    /**
     * 从 prd_price_group 批量查询构建 (salesRegionCode::priceGroupCode) → priceGroupName 映射
     */
    private Map<String, String> buildGroupNameMap(List<PriceDetailViewVO> voList) {
        Set<String> groupCodes = new HashSet<>();
        for (PriceDetailViewVO vo : voList) {
            if (vo.getPriceGroupCode() != null && !vo.getPriceGroupCode().isBlank()) {
                groupCodes.add(vo.getPriceGroupCode());
            }
        }
        if (groupCodes.isEmpty()) {
            return Map.of();
        }
        List<PrdPriceGroup> groups = priceGroupMapper.lambdaSelect(
                w -> w.in(PrdPriceGroup::getPriceGroupCode, groupCodes));
        Map<String, String> map = new HashMap<>();
        for (PrdPriceGroup g : groups) {
            map.put(buildGroupKey(g.getSalesRegionCode(), g.getPriceGroupCode()), g.getPriceGroupName());
        }
        return map;
    }

    private Map<String, String> buildGroupNameMapForExport(List<PriceDetailExportVO> exportList) {
        Set<String> groupCodes = new HashSet<>();
        for (PriceDetailExportVO vo : exportList) {
            if (vo.getPriceGroupCode() != null && !vo.getPriceGroupCode().isBlank()) {
                groupCodes.add(vo.getPriceGroupCode());
            }
        }
        if (groupCodes.isEmpty()) {
            return Map.of();
        }
        List<PrdPriceGroup> groups = priceGroupMapper.lambdaSelect(
                w -> w.in(PrdPriceGroup::getPriceGroupCode, groupCodes));
        Map<String, String> map = new HashMap<>();
        for (PrdPriceGroup g : groups) {
            map.put(buildGroupKey(g.getSalesRegionCode(), g.getPriceGroupCode()), g.getPriceGroupName());
        }
        return map;
    }

    private static String buildGroupKey(String salesRegionCode, String priceGroupCode) {
        return (salesRegionCode != null ? salesRegionCode : "") + "::" + (priceGroupCode != null ? priceGroupCode : "");
    }

    /**
     * 计算价格变动比率：|新售价 - 原售价| / 原售价 × 100%
     */
    private BigDecimal calculateChangeRatio(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            // 原价为0视为100%变动
            return new BigDecimal("100");
        }
        return newPrice.subtract(oldPrice).abs()
                .divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取销售大区的审批阈值
     * <p>
     * 未配置时返回默认阈值 30%
     */
    private BigDecimal getApprovalThreshold(String salesRegionCode) {
        SalesRegionDetailDTO region = salesRegionApi.getDetailByCode(salesRegionCode);
        if (region == null || !Boolean.TRUE.equals(region.getPriceApproval())) {
            // 大区未开启审批，返回一个很大的阈值表示不需要审批
            return new BigDecimal("999999");
        }
        if (region.getApprovalThreshold() == null) {
            return DEFAULT_APPROVAL_THRESHOLD;
        }
        return region.getApprovalThreshold();
    }

    public List<PriceDetailDTO> listByBarcodes(String priceGroupCode, List<String> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) {
            return List.of();
        }
        List<PrdPriceDetail> entities = priceDetailMapper.selectList(
                new LambdaQueryWrapper<PrdPriceDetail>()
                        .eq(PrdPriceDetail::getPriceGroupCode, priceGroupCode)
                        .in(PrdPriceDetail::getProductBarcode, barcodes));
        return entities.stream().map(e -> {
            PriceDetailDTO dto = new PriceDetailDTO();
            dto.setProductBarcode(e.getProductBarcode());
            dto.setPriceGroupCode(e.getPriceGroupCode());
            dto.setPrice(e.getPrice());
            return dto;
        }).collect(Collectors.toList());
    }
}
