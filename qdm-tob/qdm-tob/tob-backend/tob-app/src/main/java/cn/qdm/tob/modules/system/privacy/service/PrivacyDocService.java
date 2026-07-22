package cn.qdm.tob.modules.system.privacy.service;

import cn.qdm.tob.framework.util.AssertUtils;
import cn.qdm.tob.infrastructure.base.TobBaseService;
import cn.qdm.tob.modules.system.privacy.PrivacyMapping;
import cn.qdm.tob.modules.system.privacy.domain.SysPrivacyDoc;
import cn.qdm.tob.modules.system.privacy.enums.DocStatus;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import cn.qdm.tob.modules.system.privacy.mapper.SysPrivacyDocMapper;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocCreationVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocEditVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocExportVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocQueryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocSummaryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocViewVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 隐私文档服务。
 * <p>
 * 状态机：UNPUBLISHED → PUBLISHED → WITHDRAWN（终态）。发布非隐私政策类型文档时，
 * 会自动下架同类型的历史已发布文档（保证同类型仅一份生效）；隐私政策类型支持多版本共存。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PrivacyDocService extends TobBaseService<SysPrivacyDocMapper, SysPrivacyDoc> {

    private final SysPrivacyDocMapper docMapper;
    private final PrivacyMapping mapping;
    private final PrivacyFileStorage fileStorage;

    // ==================== 管理端 ====================

    /** 分页查询列表（不含富文本） */
    public IPage<PrivacyDocSummaryVO> list(PrivacyDocQueryVO query) {
        Page<SysPrivacyDoc> entityPage = docMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), buildWrapper(query));

        Page<PrivacyDocSummaryVO> voPage = new Page<>(
                entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(mapping.toSummaryVOList(entityPage.getRecords()));
        return voPage;
    }

    /** 详情（含富文本） */
    public PrivacyDocViewVO getById(Long id) {
        SysPrivacyDoc entity = docMapper.selectById(id);
        AssertUtils.notNull(entity, "隐私文档不存在");
        return mapping.toViewVO(entity);
    }

    /** 新增，返回主键 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(PrivacyDocCreationVO vo, MultipartFile file) {
        SysPrivacyDoc entity = mapping.toEntity(vo);
        entity.setStatus(DocStatus.UNPUBLISHED);
        if (file != null && !file.isEmpty()) {
            entity.setFileUrl(fileStorage.store(file));
        }
        // 审计字段由 MyBatisMetaObjectHandler 自动填充
        docMapper.insert(entity);
        return entity.getId();
    }

    /** 编辑（不改 docType；已下架文档不可编辑） */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, PrivacyDocEditVO vo, MultipartFile file) {
        SysPrivacyDoc existing = docMapper.selectById(id);
        AssertUtils.notNull(existing, "隐私文档不存在");
        AssertUtils.notEquals(existing.getStatus(), DocStatus.WITHDRAWN, "已下架的文档不可编辑");

        mapping.updateEntity(vo, existing);
        if (file != null && !file.isEmpty()) {
            existing.setFileUrl(fileStorage.store(file));
        }
        existing.setUpdatedAt(null);
        existing.setUpdatedBy(null);
        docMapper.updateById(existing);
    }

    /** 发布（非隐私政策类型自动下架同类型历史已发布文档） */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        SysPrivacyDoc entity = docMapper.selectById(id);
        AssertUtils.notNull(entity, "隐私文档不存在");
        AssertUtils.equals(entity.getStatus(), DocStatus.UNPUBLISHED, "只有未发布的文档可以发布");

        if (entity.getDocType() != DocType.PRIVACY_POLICY) {
            List<SysPrivacyDoc> published = docMapper.findPublishedByDocType(entity.getDocType());
            for (SysPrivacyDoc old : published) {
                old.setStatus(DocStatus.WITHDRAWN);
                old.setUpdatedAt(null);
                old.setUpdatedBy(null);
                docMapper.updateById(old);
            }
        }

        entity.setStatus(DocStatus.PUBLISHED);
        entity.setUpdatedAt(null);
        entity.setUpdatedBy(null);
        docMapper.updateById(entity);
    }

    /** 下架（终态，仅已发布可下架） */
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long id) {
        SysPrivacyDoc entity = docMapper.selectById(id);
        AssertUtils.notNull(entity, "隐私文档不存在");
        AssertUtils.equals(entity.getStatus(), DocStatus.PUBLISHED, "只有已发布的文档可以下架");

        entity.setStatus(DocStatus.WITHDRAWN);
        entity.setUpdatedAt(null);
        entity.setUpdatedBy(null);
        docMapper.updateById(entity);
    }

    /** 导出（排除富文本，上限 10000 条） */
    public List<PrivacyDocExportVO> exportList(PrivacyDocQueryVO query) {
        List<SysPrivacyDoc> entities = docMapper.selectList(new Page<>(1, 10000), buildWrapper(query));
        return mapping.toDocExportVOList(entities);
    }

    /** 下载附件 */
    public FileDownload loadFile(Long id) {
        SysPrivacyDoc entity = docMapper.selectById(id);
        AssertUtils.notNull(entity, "隐私文档不存在");
        AssertUtils.notBlank(entity.getFileUrl(), "该文档无附件");

        Resource resource = fileStorage.loadAsResource(entity.getFileUrl());
        return new FileDownload(resource, fileStorage.originalName(entity.getFileUrl()));
    }

    // ==================== 小程序端 ====================

    /** 全部已发布文档（列表，不含富文本） */
    public List<PrivacyDocSummaryVO> getPublishedDocs() {
        return mapping.toSummaryVOList(docMapper.findPublished());
    }

    /** 按类型取当前生效（最新已发布）文档详情 */
    public PrivacyDocViewVO getByDocType(DocType docType) {
        SysPrivacyDoc entity = docMapper.findLatestPublishedByDocType(docType)
                .orElseThrow(AssertUtils.warn("该类型暂无已发布文档"));
        return mapping.toViewVO(entity);
    }

    /** 隐私政策历史版本列表（已发布，创建时间倒序） */
    public List<PrivacyDocSummaryVO> getVersionList() {
        return mapping.toSummaryVOList(docMapper.findPublishedByDocType(DocType.PRIVACY_POLICY));
    }

    /** 按版本号取隐私政策详情 */
    public PrivacyDocViewVO getByVersion(String version) {
        AssertUtils.notBlank(version, "版本号不能为空");
        SysPrivacyDoc entity = docMapper.findPublishedByVersion(DocType.PRIVACY_POLICY, version)
                .orElseThrow(AssertUtils.warn("该版本隐私政策不存在"));
        return mapping.toViewVO(entity);
    }

    // ==================== 私有 ====================

    private LambdaQueryWrapper<SysPrivacyDoc> buildWrapper(PrivacyDocQueryVO query) {
        LambdaQueryWrapper<SysPrivacyDoc> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getDocType())) {
            DocType docType = EnumUtils.getEnum(DocType.class, query.getDocType());
            AssertUtils.notNull(docType, "无效的文档类型: " + query.getDocType());
            wrapper.eq(SysPrivacyDoc::getDocType, docType);
        }
        if (StringUtils.isNotBlank(query.getVersion())) {
            wrapper.like(SysPrivacyDoc::getVersion, query.getVersion());
        }
        wrapper.orderByDesc(SysPrivacyDoc::getCreatedAt);
        return wrapper;
    }
}
