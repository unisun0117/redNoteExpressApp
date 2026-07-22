package cn.qdm.tob.modules.system.dict.api.internal.dto;

import lombok.Data;

/**
 * 字典项 API 响应（跨模块调用）。
 * 只暴露外部模块需要的字段，内部 VO 字段变更不影响调用方。
 */
@Data
public class DictItemDTO {
    /** 数据值 */
    private String value;
    /** 显示文本 */
    private String label;
    /** 排序号 */
    private Integer sort;
}
