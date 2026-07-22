package cn.qdm.tob.modules.order.pay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 小程序支付下单响应 DTO。
 * <p>
 * 包含小程序端调用 wx.requestPayment 所需的全部参数。
 * </p>
 */
@Data
@Builder
@Schema(description = "小程序调起支付参数")
public class CreateOrderResponse {

    @Schema(description = "小程序 AppID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;

    @Schema(description = "时间戳（秒级，10位数字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timeStamp;

    @Schema(description = "随机字符串，不长于32位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nonceStr;

    @Schema(description = "订单详情扩展字符串，格式 prepay_id=xxx", requiredMode = Schema.RequiredMode.REQUIRED)
    private String packageStr;

    @Schema(description = "签名类型，固定为 RSA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String signType;

    @Schema(description = "签名值，使用商户私钥对签名串进行 SHA256 with RSA 签名并 Base64 编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String paySign;
}
