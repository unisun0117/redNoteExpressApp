package cn.qdm.tob.modules.order.sales.vo;

import cn.qdm.tob.modules.order.pay.dto.CreateOrderResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "订单提交结果")
public class OrderSubmitViewVO {
    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;

    @Schema(description = "微信支付参数（仅微信支付成功时返回，失败时为 null）")
    private CreateOrderResponse payParams;
}
