package cn.qdm.tob.modules.product.pricing.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.product.pricing.PriceGroupMapping;
import cn.qdm.tob.modules.product.pricing.domain.PrdPriceGroup;
import cn.qdm.tob.modules.product.pricing.mapper.PriceGroupMapper;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupCreationVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupEditVO;
import cn.qdm.tob.modules.product.pricing.vo.PriceGroupViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 价格组 CRUD 服务
 */
@Service
@RequiredArgsConstructor
public class PriceGroupService extends TobBaseService<PriceGroupMapper, PrdPriceGroup> {

    private final PriceGroupMapper priceGroupMapper;
    private final PriceGroupMapping mapping;

    // ================================================================
    // 分页查询
    // ================================================================

    /**
     * 分页查询价格组列表
     */
    public Page<PriceGroupViewVO> page(Integer pageNum, Integer pageSize,
                                       String salesRegionCode, String priceGroupName) {
        LambdaQueryWrapper<PrdPriceGroup> wrapper = new LambdaQueryWrapper<>();
        if (salesRegionCode != null && !salesRegionCode.isBlank()) {
            wrapper.eq(PrdPriceGroup::getSalesRegionCode, salesRegionCode);
        }
        if (priceGroupName != null && !priceGroupName.isBlank()) {
            wrapper.like(PrdPriceGroup::getPriceGroupName, priceGroupName);
        }
        wrapper.orderByDesc(PrdPriceGroup::getUpdatedAt);

        Page<PrdPriceGroup> entityPage = priceGroupMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        Page<PriceGroupViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(mapping.toViewList(entityPage.getRecords()));
        return voPage;
    }

    // ================================================================
    // 新增
    // ================================================================

    /**
     * 新增价格组
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(PriceGroupCreationVO vo) {
        // 校验「销售大区 + 价格组编码」唯一性
        boolean exists = priceGroupMapper.lambdaExists(w ->
                w.eq(PrdPriceGroup::getSalesRegionCode, vo.getSalesRegionCode())
                 .eq(PrdPriceGroup::getPriceGroupCode, vo.getPriceGroupCode()));
        AssertUtils.isFalse(exists, "该销售大区下已存在相同价格组编码");

        PrdPriceGroup entity = mapping.toEntity(vo);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setUpdatedBy(vo.getCreatedBy());

        priceGroupMapper.insert(entity);
    }

    // ================================================================
    // 编辑
    // ================================================================

    /**
     * 编辑价格组（仅名称和描述可修改）
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(PriceGroupEditVO vo) {
        PrdPriceGroup existing = priceGroupMapper.selectById(vo.getId());
        AssertUtils.notNull(existing, "价格组不存在");

        existing.setPriceGroupName(vo.getPriceGroupName());
        existing.setDescription(vo.getDescription());
        if (vo.getUpdatedBy() != null) {
            existing.setUpdatedBy(vo.getUpdatedBy());
        }
        existing.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));

        priceGroupMapper.updateById(existing);
    }

    // ================================================================
    // 下拉选项
    // ================================================================

    /**
     * 按销售大区查询价格组下拉选项
     */
    public List<PriceGroupViewVO> listOptions(String salesRegionCode) {
        LambdaQueryWrapper<PrdPriceGroup> wrapper = new LambdaQueryWrapper<>();
        if (salesRegionCode != null && !salesRegionCode.isBlank()) {
            wrapper.eq(PrdPriceGroup::getSalesRegionCode, salesRegionCode);
        }
        wrapper.orderByDesc(PrdPriceGroup::getUpdatedAt);
        List<PrdPriceGroup> entities = priceGroupMapper.selectList(wrapper);
        return mapping.toViewList(entities);
    }
}
