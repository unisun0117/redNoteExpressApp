package cn.qdm.tob.modules.customer.operation.announcement;

import cn.qdm.tob.modules.customer.operation.announcement.domain.OprAnnouncement;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementCreationVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementEditVO;
import cn.qdm.tob.modules.customer.operation.announcement.vo.AnnouncementViewVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 公告模块 MapStruct 对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnnouncementMapping {

    /** CreationVO → 实体（regionName / 审计字段在 Service 与自动填充处理器中补齐） */
    OprAnnouncement toEntity(AnnouncementCreationVO vo);

    /** EditVO → 实体（增量更新到目标实体，null 值跳过不覆盖） */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AnnouncementEditVO vo, @MappingTarget OprAnnouncement entity);

    /** 实体 → ViewVO */
    AnnouncementViewVO toViewVO(OprAnnouncement entity);

    /** 批量实体 → ViewVO */
    List<AnnouncementViewVO> toViewList(List<OprAnnouncement> entities);
}
