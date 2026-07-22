package cn.qdm.tob.modules.system.dict.api.internal.dto;

import lombok.Data;

/**
 * 字典项详情 DTO（跨模块单条查询）。
 * 返回 value/label/status，不返回 sort。
 */
@Data
public class DictItemDetailDTO {
    /** 数据值 */
    private String value;
    /** 显示文本 */
    private String label;
    /** 状态 */
    private String status;
}
