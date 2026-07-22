package cn.qdm.tob.modules.customer.operation.salesman.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加业务员入参
 */
@Data
@Schema(description = "添加业务员")
public class AddSalesmanVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择用户")
    private Long userId;

    @Schema(description = "创建人")
    private String createdBy;
}
