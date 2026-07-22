package cn.qdm.tob.modules.product.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑价格组明细入参
 * <p>
 * 仅售价和变动原因可修改，其他字段只读
 */
@Data
@Schema(description = "编辑价格组明细")
public class PriceDetailEditVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "明细ID不能为空")
    private Long id;

    @Schema(description = "售价（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    @DecimalMax(value = "99999999.99", message = "售价超出上限")
    private BigDecimal price;

    @Schema(description = "变动原因", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
    @NotBlank(message = "变动原因不能为空")
    private String changeReason;

    @Schema(description = "更新人")
    private String updatedBy;
}
