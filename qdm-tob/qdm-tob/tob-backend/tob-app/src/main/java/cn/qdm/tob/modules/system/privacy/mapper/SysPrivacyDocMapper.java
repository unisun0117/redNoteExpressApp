package cn.qdm.tob.modules.system.privacy.mapper;

import cn.qdm.tob.infrastructure.base.TobBaseMapper;
import cn.qdm.tob.modules.system.privacy.domain.SysPrivacyDoc;
import cn.qdm.tob.modules.system.privacy.enums.DocStatus;
import cn.qdm.tob.modules.system.privacy.enums.DocType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SysPrivacyDocMapper extends TobBaseMapper<SysPrivacyDoc> {

    /** 按文档类型查询（创建时间倒序） */
    default List<SysPrivacyDoc> findByDocType(DocType docType) {
        return lambdaSelect(w -> w
                .eq(SysPrivacyDoc::getDocType, docType)
                .orderByDesc(SysPrivacyDoc::getCreatedAt));
    }

    /** 按状态查询（创建时间倒序） */
    default List<SysPrivacyDoc> findByStatus(DocStatus status) {
        return lambdaSelect(w -> w
                .eq(SysPrivacyDoc::getStatus, status)
                .orderByDesc(SysPrivacyDoc::getCreatedAt));
    }

    /** 查询全部已发布文档（创建时间倒序） */
    default List<SysPrivacyDoc> findPublished() {
        return findByStatus(DocStatus.PUBLISHED);
    }

    /** 按文档类型查询已发布文档（创建时间倒序，可能多版本） */
    default List<SysPrivacyDoc> findPublishedByDocType(DocType docType) {
        return lambdaSelect(w -> w
                .eq(SysPrivacyDoc::getDocType, docType)
                .eq(SysPrivacyDoc::getStatus, DocStatus.PUBLISHED)
                .orderByDesc(SysPrivacyDoc::getCreatedAt));
    }

    /** 按文档类型查询「最新」已发布文档 */
    default Optional<SysPrivacyDoc> findLatestPublishedByDocType(DocType docType) {
        return findPublishedByDocType(docType).stream().findFirst();
    }

    /** 按文档类型 + 版本号查询已发布文档 */
    default Optional<SysPrivacyDoc> findPublishedByVersion(DocType docType, String version) {
        return lambdaSelect(w -> w
                .eq(SysPrivacyDoc::getDocType, docType)
                .eq(SysPrivacyDoc::getVersion, version)
                .eq(SysPrivacyDoc::getStatus, DocStatus.PUBLISHED)
                .orderByDesc(SysPrivacyDoc::getCreatedAt))
                .stream().findFirst();
    }
}
