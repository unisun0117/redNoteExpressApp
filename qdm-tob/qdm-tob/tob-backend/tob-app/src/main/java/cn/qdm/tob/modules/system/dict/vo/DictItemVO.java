package cn.qdm.tob.modules.system.dict.vo;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字典项视图（列表/详情）
 */
@Data
@Schema(description = "字典项视图")
public class DictItemVO {

    @Schema(description = "所属字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictCode;

    @Schema(description = "字典项数据值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;

    @Schema(description = "字典项显示文本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String label;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "ACTIVE")
    private RecordStatus status;
}
