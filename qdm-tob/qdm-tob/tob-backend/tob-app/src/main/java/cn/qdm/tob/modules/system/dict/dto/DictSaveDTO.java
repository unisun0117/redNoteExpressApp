package cn.qdm.tob.modules.system.dict.dto;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 字典保存请求（创建/更新复用）
 */
@Data
@Schema(description = "字典保存请求")
public class DictSaveDTO {

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典编码不能为空")
    private String code;

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名称不能为空")
    private String name;

    @Schema(description = "字典描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "ACTIVE")
    private RecordStatus status;

    @Schema(description = "字典项（创建时必填，更新时可选）")
    private List<DictItemBatchSaveDTO> items;
}
