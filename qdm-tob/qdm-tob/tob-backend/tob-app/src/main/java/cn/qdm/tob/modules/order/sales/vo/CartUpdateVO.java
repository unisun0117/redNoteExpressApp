package cn.qdm.tob.modules.order.sales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "修改购物车商品")
public class CartUpdateVO {
    @Schema(description = "购物车记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "数量（选填）")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    private BigDecimal quantity;

    @Schema(description = "是否勾选（0/1，选填）")
    private Integer selected;
}
