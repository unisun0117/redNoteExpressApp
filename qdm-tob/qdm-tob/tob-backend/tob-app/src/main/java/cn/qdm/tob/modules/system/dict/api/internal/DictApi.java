package cn.qdm.tob.modules.system.dict.api.internal;

import cn.qdm.tob.framework.description.DescriptionProvider;
import cn.qdm.tob.modules.system.dict.api.internal.dto.DictItemDTO;
import cn.qdm.tob.modules.system.dict.api.internal.dto.DictItemDetailDTO;

import java.util.List;

/**
 * 字典查询 API（供其他模块调用）。
 * <p>
 * 继承 {@link DescriptionProvider}，使框架层（Jackson 序列化、Excel 转换）能通过 SPI 机制自动发现。
 * 其他业务模块应通过此接口获取字典项，不可直接引用 dict 模块内部的 service / domain / mapper。
 */
public interface DictApi extends DescriptionProvider {

    /**
     * 按字典编码获取所有启用状态的字典项。
     *
     * @param dictCode 字典编码（如 ORDER_TYPE）
     * @return 字典项列表，按排序号升序；字典不存在时返回空列表
     */
    List<DictItemDTO> listItems(String dictCode);

    /**
     * 按字典编码和数据值获取单个字典项。
     *
     * @param dictCode 字典编码
     * @param value    数据值
     * @return 字典项（含状态），查不到返回 null
     */
    DictItemDetailDTO getItem(String dictCode, String value);

    /**
     * 按字典编码和状态获取字典项列表。
     *
     * @param dictCode 字典编码
     * @param status   状态（ACTIVE / INACTIVE）
     * @return 字典项列表，按排序号升序
     */
    List<DictItemDTO> listItemsByStatus(String dictCode, String status);
}
