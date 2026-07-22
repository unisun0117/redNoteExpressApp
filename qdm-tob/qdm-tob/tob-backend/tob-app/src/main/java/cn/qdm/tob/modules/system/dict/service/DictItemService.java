package cn.qdm.tob.modules.system.dict.service;

import cn.qdm.tob.modules.system.dict.dto.DictItemBatchSaveDTO;
import cn.qdm.tob.modules.system.dict.dto.DictItemQuery;
import cn.qdm.tob.modules.system.dict.dto.DictItemSaveDTO;
import cn.qdm.tob.modules.system.dict.vo.DictItemVO;

import java.util.List;

/**
 * 字典项服务接口
 */
public interface DictItemService {

    /** 按字典编码查询启用项（下拉框用），走缓存 */
    List<DictItemVO> listActiveItemVOs(String code);

    /** 按条件筛选字典项（管理后台列表），支持关键词和状态过滤 */
    List<DictItemVO> listItemVOs(DictItemQuery query);

    /** 更新字典项 */
    void updateByDictCodeAndValue(DictItemSaveDTO vo);

    /** 批量新增字典项 */
    void batchCreateItems(String dictCode, List<DictItemBatchSaveDTO> items);

    /** 删除单个字典项 */
    void deleteByDictCodeAndValue(String dictCode, String value);

    /** 按字典编码删除全部字典项（模块内调用） */
    void deleteByDictCode(String dictCode);
}
