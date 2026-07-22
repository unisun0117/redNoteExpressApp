package cn.qdm.tob.modules.customer.operation.announcement.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.customer.operation.announcement.AnnouncementMapping;
import cn.qdm.tob.modules.customer.operation.announcement.domain.OprAnnouncement;
import cn.qdm.tob.modules.customer.operation.announcement.mapper.OprAnnouncementMapper;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementCreationVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementEditVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementQueryVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementViewVO;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.SalesRegionApi;
import cn.qdm.tob.modules.customer.operation.salesregion.api.internal.dto.SalesRegionDetailDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 公告 CRUD 服务
 * <p>
 * 审计字段（创建/修改的时间与操作人）由 MyBatisMetaObjectHandler 自动填充，此处不手动 set。
 * 销售大区名称通过同模块内 {@link SalesRegionApi} 按编号解析后冗余存储，避免跨表 JOIN。
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService extends TobBaseService<OprAnnouncementMapper, OprAnnouncement> {

    private final AnnouncementMapping mapping;
    private final SalesRegionApi salesRegionApi;

    /**
     * 分页查询
     */
    public IPage<AnnouncementViewVO> list(AnnouncementQueryVO query) {
        LambdaQueryWrapper<OprAnnouncement> wrapper = new LambdaQueryWrapper<>();
        if (query.getRegionCode() != null && !query.getRegionCode().isBlank()) {
            wrapper.eq(OprAnnouncement::getRegionCode, query.getRegionCode());
        }
        if (query.getEnabled() != null) {
            wrapper.eq(OprAnnouncement::getEnabled, query.getEnabled());
        }
        wrapper.orderByDesc(OprAnnouncement::getUpdatedAt);

        Page<OprAnnouncement> entityPage = baseMapper.selectPage(
            new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        Page<AnnouncementViewVO> voPage = new Page<>(
            query.getPageNum(), query.getPageSize(), entityPage.getTotal());
        voPage.setRecords(mapping.toViewList(entityPage.getRecords()));
        return voPage;
    }

    /**
     * 新增
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(AnnouncementCreationVO vo) {
        // 校验销售大区存在并解析大区名称（同模块内部 API）
        SalesRegionDetailDTO region = salesRegionApi.getDetailByCode(vo.getRegionCode());
        AssertUtils.notNull(region, "销售大区不存在");

        OprAnnouncement entity = mapping.toEntity(vo);
        entity.setRegionName(region.getName());
        if (entity.getEnabled() == null) {
            entity.setEnabled(Boolean.TRUE);
        }
        baseMapper.insert(entity);
    }

    /**
     * 更新（销售大区不可修改，仅内容与启用状态）
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AnnouncementEditVO vo) {
        AssertUtils.notNull(baseMapper.selectById(id), "公告不存在");

        // 使用全新实体做增量更新，审计字段保持 null 以便自动填充刷新
        OprAnnouncement patch = new OprAnnouncement();
        mapping.updateEntity(vo, patch);
        patch.setId(id);
        baseMapper.updateById(patch);
    }

    /**
     * 小程序端 — 获取当前用户所属大区的已启用公告列表
     */
    public List<AnnouncementViewVO> getEnabledForMall(String regionCode) {
        LambdaQueryWrapper<OprAnnouncement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OprAnnouncement::getEnabled, Boolean.TRUE);
        if (regionCode != null && !regionCode.isBlank()) {
            wrapper.eq(OprAnnouncement::getRegionCode, regionCode);
        }
        wrapper.orderByDesc(OprAnnouncement::getUpdatedAt);
        return mapping.toViewList(baseMapper.selectList(wrapper));
    }

    /**
     * 启用/停用切换
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggle(Long id) {
        OprAnnouncement existing = baseMapper.selectById(id);
        AssertUtils.notNull(existing, "公告不存在");

        OprAnnouncement patch = new OprAnnouncement();
        patch.setId(id);
        patch.setEnabled(!Boolean.TRUE.equals(existing.getEnabled()));
        baseMapper.updateById(patch);
    }
}
