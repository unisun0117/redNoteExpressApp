package cn.qdm.tob.modules.customer.cst.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分配审核人入参VO
 */
@Data
@Schema(description = "分配审核人")
public class AssignAuditorVO {

    @Schema(description = "审核人ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "审核人ID不能为空")
    private Long auditorId;

    @Schema(description = "审核人姓名")
    private String auditorName;

    @Schema(description = "审核人类型：SALESMAN/MANAGER")
    private String auditorType;
}
