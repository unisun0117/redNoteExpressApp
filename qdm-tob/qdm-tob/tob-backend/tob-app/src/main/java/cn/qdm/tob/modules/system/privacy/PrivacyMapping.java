package cn.qdm.tob.modules.system.privacy;

import cn.qdm.tob.modules.system.privacy.domain.SysPrivacyAuthRecord;
import cn.qdm.tob.modules.system.privacy.domain.SysPrivacyDoc;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordExportVO;
import cn.qdm.tob.modules.system.privacy.vo.AuthRecordViewVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocCreationVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocEditVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocExportVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocSummaryVO;
import cn.qdm.tob.modules.system.privacy.vo.PrivacyDocViewVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 隐私模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PrivacyMapping {

    // ==================== 隐私文档 ====================

    /** CreationVO → 实体（id/状态/审计字段由 Service 或框架填充） */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SysPrivacyDoc toEntity(PrivacyDocCreationVO vo);

    /** EditVO → 原地更新实体（忽略 null 字段，不改 docType/状态/审计字段） */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "docType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(PrivacyDocEditVO vo, @MappingTarget SysPrivacyDoc entity);

    /** 实体 → 详情 VO */
    PrivacyDocViewVO toViewVO(SysPrivacyDoc entity);

    List<PrivacyDocViewVO> toViewVOList(List<SysPrivacyDoc> entities);

    /** 实体 → 列表 VO */
    PrivacyDocSummaryVO toSummaryVO(SysPrivacyDoc entity);

    List<PrivacyDocSummaryVO> toSummaryVOList(List<SysPrivacyDoc> entities);

    /** 实体 → 导出 VO */
    PrivacyDocExportVO toDocExportVO(SysPrivacyDoc entity);

    List<PrivacyDocExportVO> toDocExportVOList(List<SysPrivacyDoc> entities);

    // ==================== 授权记录 ====================

    /** 实体 → 出参 VO */
    AuthRecordViewVO toAuthRecordViewVO(SysPrivacyAuthRecord entity);

    List<AuthRecordViewVO> toAuthRecordViewVOList(List<SysPrivacyAuthRecord> entities);

    /** 实体 → 导出 VO */
    AuthRecordExportVO toAuthRecordExportVO(SysPrivacyAuthRecord entity);

    List<AuthRecordExportVO> toAuthRecordExportVOList(List<SysPrivacyAuthRecord> entities);
}
