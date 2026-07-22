package cn.qdm.tob.modules.product.catalog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存调整入参 VO
 */
@Data
@Schema(description = "库存调整")
public class StockAdjustVO {

    @Schema(description = "新的今日可用库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "今日可用库存不能为空")
    @DecimalMin(value = "0", message = "今日可用库存不能小于0")
    private BigDecimal newDailyStock;
}
