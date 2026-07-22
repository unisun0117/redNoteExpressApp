package cn.qdm.tob.modules.system.dict;

import cn.qdm.tob.modules.system.dict.api.internal.dto.DictItemDTO;
import cn.qdm.tob.modules.system.dict.api.internal.dto.DictItemDetailDTO;
import cn.qdm.tob.modules.system.dict.domain.SysDict;
import cn.qdm.tob.modules.system.dict.domain.SysDictItem;
import cn.qdm.tob.modules.system.dict.dto.DictItemBatchSaveDTO;
import cn.qdm.tob.modules.system.dict.dto.DictItemSaveDTO;
import cn.qdm.tob.modules.system.dict.dto.DictSaveDTO;
import cn.qdm.tob.modules.system.dict.vo.DictItemVO;
import cn.qdm.tob.modules.system.dict.vo.DictVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 字典模块对象映射器
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DictMapping {

    /** DictSaveDTO → SysDict 实体 */
    SysDict toEntity(DictSaveDTO vo);

    /** SysDict 实体 → DictVO */
    DictVO toSummary(SysDict entity);

    /** DictItemSaveDTO → SysDictItem 实体 */
    SysDictItem toItemEntity(DictItemSaveDTO vo);

    /** DictItemBatchSaveDTO → SysDictItem 实体（不含 dictCode，由 Service 设置） */
    SysDictItem toItemEntity(DictItemBatchSaveDTO vo);

    /** 批量 DictItemBatchSaveDTO → SysDictItem 实体 */
    List<SysDictItem> toItemEntities(List<DictItemBatchSaveDTO> list);

    /** SysDictItem 实体 → DictItemVO */
    DictItemVO toItemSummary(SysDictItem entity);

    /** 批量 SysDictItem 实体 → DictItemVO */
    List<DictItemVO> toItemSummaryList(List<SysDictItem> entities);

    /** SysDictItem 实体 → DictItemDTO（跨模块 API） */
    DictItemDTO toItemDTO(SysDictItem entity);

    /** 批量 SysDictItem 实体 → DictItemDTO */
    List<DictItemDTO> toItemDTOList(List<SysDictItem> entities);

    /** SysDictItem 实体 → DictItemDetailDTO（跨模块 API，含状态） */
    DictItemDetailDTO toItemDetailDTO(SysDictItem entity);

}
