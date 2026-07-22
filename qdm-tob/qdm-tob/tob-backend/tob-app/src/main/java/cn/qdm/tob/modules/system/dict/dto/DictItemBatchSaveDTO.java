package cn.qdm.tob.modules.system.dict.dto;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典项批量新增请求（不含 dictCode，由外层传入）
 */
@Data
@Schema(description = "字典项批量新增请求")
public class DictItemBatchSaveDTO {

    @Schema(description = "字典项数据值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典项数据值不能为空")
    private String value;

    @Schema(description = "字典项显示文本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典项显示文本不能为空")
    private String label;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "ACTIVE")
    private RecordStatus status;
}
