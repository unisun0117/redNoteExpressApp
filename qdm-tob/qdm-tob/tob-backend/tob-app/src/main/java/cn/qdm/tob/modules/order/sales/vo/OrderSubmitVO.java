package cn.qdm.tob.modules.order.sales.vo;

import cn.qdm.tob.modules.order.sales.enums.PayMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "提交订单")
public class OrderSubmitVO {
    @Schema(description = "收货地址ID（客户档案ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @Schema(description = "支付方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付方式不能为空")
    private PayMethod payMethod;

    @Schema(description = "期望到货日期")
    private LocalDate arrivalDate;

    @Schema(description = "配送备注")
    @Size(max = 200, message = "备注不能超过200字")
    private String deliveryRemark;

    @Schema(description = "幂等键（客户端生成，防重复提交）")
    private String idempotentKey;
}
