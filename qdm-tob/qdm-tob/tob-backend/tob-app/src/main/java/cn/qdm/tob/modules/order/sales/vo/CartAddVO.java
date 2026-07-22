package cn.qdm.tob.modules.order.sales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "加入购物车")
public class CartAddVO {
    @Schema(description = "商品条码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品条码不能为空")
    private String barcode;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    private BigDecimal quantity;
}
