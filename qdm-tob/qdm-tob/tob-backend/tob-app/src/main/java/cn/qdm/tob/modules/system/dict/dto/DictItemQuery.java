package cn.qdm.tob.modules.system.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典项列表查询参数
 */
@Data
@Schema(description = "字典项列表查询参数")
public class DictItemQuery {

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "搜索关键词（模糊匹配 value/label）")
    private String keyword;

    @Schema(description = "状态筛选（ACTIVE / INACTIVE），不传则返回全部")
    private String status;
}
