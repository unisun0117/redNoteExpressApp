package cn.qdm.tob.modules.system.dict.vo;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典视图（列表/详情）
 */
@Data
@Schema(description = "字典视图")
public class DictVO {

    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "字典描述", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "ACTIVE")
    private RecordStatus status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime updatedAt;
}
