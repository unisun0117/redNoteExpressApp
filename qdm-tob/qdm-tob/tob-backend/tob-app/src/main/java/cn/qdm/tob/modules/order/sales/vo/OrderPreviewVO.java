package cn.qdm.tob.modules.order.sales.vo;

import cn.qdm.tob.modules.order.sales.enums.PayMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "订单预览")
public class OrderPreviewVO {
    @Schema(description = "收货地址ID（客户档案ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @Schema(description = "支付方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付方式不能为空")
    private PayMethod payMethod;

    @Schema(description = "期望到货日期")
    private LocalDate arrivalDate;
}
