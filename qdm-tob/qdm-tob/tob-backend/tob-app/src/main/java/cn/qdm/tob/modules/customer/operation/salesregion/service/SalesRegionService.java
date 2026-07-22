package cn.qdm.tob.modules.customer.operation.salesregion.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.customer.operation.salesregion.SalesRegionMapping;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.SalesRegionApi;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionDetailDTO;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionSimpleDTO;
import cn.qdm.tob.modules.customer.operation.salesregion.domain.OprSalesRegion;
import cn.qdm.tob.modules.customer.operation.salesregion.mapper.SalesRegionMapper;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionCreationVO;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionEditVO;
import cn.qdm.tob.modules.customer.operation.salesregion.vo.SalesRegionViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * 销售大区 CRUD 服务
 */
@Service
@RequiredArgsConstructor
public class SalesRegionService extends TobBaseService<SalesRegionMapper, OprSalesRegion>
        implements SalesRegionApi {

    private final SalesRegionMapping mapping;

    /**
     * 分页查询
     */
    public Page<SalesRegionViewVO> page(Integer pageNum, Integer pageSize,
                                         String region, Boolean serviceEnabled) {
        LambdaQueryWrapper<OprSalesRegion> wrapper = new LambdaQueryWrapper<>();
        if (region != null && !region.isBlank()) {
            wrapper.eq(OprSalesRegion::getCode, region);
        }
        if (serviceEnabled != null) {
            wrapper.eq(OprSalesRegion::getServiceEnabled, serviceEnabled);
        }
        wrapper.orderByDesc(OprSalesRegion::getUpdatedAt);

        Page<OprSalesRegion> entityPage = baseMapper.selectPage(
            new Page<>(pageNum, pageSize), wrapper);

        Page<SalesRegionViewVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(mapping.toViewList(entityPage.getRecords()));
        return voPage;
    }

    /**
     * 按编号查询
     */
    public SalesRegionViewVO getByCode(String code) {
        OprSalesRegion entity = baseMapper.selectOne(
            new LambdaQueryWrapper<OprSalesRegion>().eq(OprSalesRegion::getCode, code));
        AssertUtils.notNull(entity, "销售大区不存在");
        return mapping.toView(entity);
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(SalesRegionCreationVO vo) {
        // 校验编号唯一性
        OprSalesRegion existing = baseMapper.selectOne(
            new LambdaQueryWrapper<OprSalesRegion>().eq(OprSalesRegion::getCode, vo.getCode()));
        AssertUtils.isNull(existing, "编号已存在");

        OprSalesRegion entity = mapping.toEntity(vo);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        entity.setWarehouseCount(0);
        entity.setMerchantName(vo.getMerchantName());
        entity.setCreatedBy(vo.getCreatedBy());
        entity.setUpdatedBy(vo.getCreatedBy());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        baseMapper.insert(entity);
    }

    /**
     * 更新
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(String code, SalesRegionEditVO vo) {
        OprSalesRegion existing = baseMapper.selectOne(
            new LambdaQueryWrapper<OprSalesRegion>().eq(OprSalesRegion::getCode, code));
        AssertUtils.notNull(existing, "销售大区不存在");

        OprSalesRegion entity = mapping.toEntity(vo);
        entity.setId(existing.getId());
        entity.setCode(code);
        entity.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        // 保留不可编辑字段
        entity.setWarehouseCount(existing.getWarehouseCount());
        entity.setCreatedAt(existing.getCreatedAt());
        entity.setCreatedBy(existing.getCreatedBy());
        // 透传前端传入字段
        if (vo.getMerchantName() != null) {
            entity.setMerchantName(vo.getMerchantName());
        }
        if (vo.getUpdatedBy() != null) {
            entity.setUpdatedBy(vo.getUpdatedBy());
        }
        baseMapper.updateById(entity);
    }

    /**
     * 删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String code) {
        OprSalesRegion existing = baseMapper.selectOne(
            new LambdaQueryWrapper<OprSalesRegion>().eq(OprSalesRegion::getCode, code));
        AssertUtils.notNull(existing, "销售大区不存在");
        baseMapper.deleteById(existing.getId());
    }

    @Override
    public List<SalesRegionSimpleDTO> listAll() {
        return baseMapper.selectList(null).stream()
                .map(r -> {
                    SalesRegionSimpleDTO dto = new SalesRegionSimpleDTO();
                    dto.setId(r.getId());
                    dto.setCode(r.getCode());
                    dto.setName(r.getName());
                    return dto;
                })
                .toList();
    }

    @Override
    public SalesRegionDetailDTO getDetailByCode(String code) {
        var entity = baseMapper.lambdaSelectOne(w -> w.eq(OprSalesRegion::getCode, code));
        return entity.map(mapping::toDto).orElse(null);
    }

    @Override
    public SalesRegionDetailDTO getDetailById(Long id) {
        var entity = baseMapper.selectById(id);
        return Objects.isNull(entity) ? null : mapping.toDto(entity);
    }
}
