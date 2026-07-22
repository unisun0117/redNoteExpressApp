package cn.qdm.tob.modules.order.refund.api.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 微信支付退款申请请求 DTO。
 *
 * <p>仅接收商户订单号和退款单号。退款金额、微信支付订单号等关键字段由后端从本地订单记录查询，
 * 遵循「铁律三：零信任」原则——不信任前端传值。</p>
 */
@Data
@Schema(description = "退款申请请求")
public class RefundRequest {

    @NotBlank(message = "商户订单号不能为空")
    @Schema(description = "原支付订单的商户订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202508010001")
    private String outTradeNo;

    @NotBlank(message = "商户退款单号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_\\-|*]{1,64}$", message = "商户退款单号格式不正确")
    @Schema(description = "商户退款单号，商户系统内部唯一，1-64位", requiredMode = Schema.RequiredMode.REQUIRED, example = "REF202508010001")
    private String outRefundNo;

    @Schema(description = "退款原因，长度不超过80个字节", example = "商品退货")
    private String reason;

    @DecimalMin(value = "0.01", message = "退款金额必须大于0.01")
    @Schema(description = "退款金额（元），为空则全额退款。仅用于前端指定部分退款金额时传入", example = "1.00")
    private BigDecimal refundAmountYuan;

    @Schema(description = "退款结果回调 URL，非必填，不传则使用配置中的默认地址")
    private String notifyUrl;
}
