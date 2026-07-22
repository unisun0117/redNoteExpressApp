package cn.qdm.tob.modules.system.role.vo;

import cn.qdm.tob.framework.model.RecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "角色视图")
public class RoleViewVO {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private RecordStatus status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "修改人")
    private String updatedBy;
}
