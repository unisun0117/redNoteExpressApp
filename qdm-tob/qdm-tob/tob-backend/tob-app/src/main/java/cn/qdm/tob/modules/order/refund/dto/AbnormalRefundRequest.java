package cn.qdm.tob.modules.order.refund.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 异常退款请求 DTO。
 *
 * <p>对应 <a href="https://pay.weixin.qq.com/doc/v3/merchant/4013071193">发起异常退款API</a>。
 * 退款状态为 ABNORMAL 时调用，可选择退款至用户银行卡或退款至商户银行账户。</p>
 */
@Data
@Schema(description = "异常退款请求")
public class AbnormalRefundRequest {

    @NotBlank(message = "微信支付退款单号不能为空")
    @Schema(description = "微信支付退款单号（申请退款接口返回的 refund_id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50000000382019052709732678859")
    private String refundId;

    @NotBlank(message = "商户退款单号不能为空")
    @Schema(description = "商户退款单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "REF202508010001")
    private String outRefundNo;

    @NotBlank(message = "异常退款处理方式不能为空")
    @Schema(description = "处理方式：USER_BANK_CARD-退款至用户银行卡，MERCHANT_BANK_CARD-退款至交易商户银行账户",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "USER_BANK_CARD")
    private String type;

    @Schema(description = "开户银行，退款至用户时必填。仅支持招行/交行/农行/建行/工行/中行/平安/浦发/中信/光大/民生/兴业/广发/邮储/宁波银行", example = "ICBC_DEBIT")
    private String bankType;

    @Schema(description = "收款银行卡号，退款至用户时必填，需使用微信支付公钥加密")
    private String bankAccount;

    @Schema(description = "收款用户姓名，退款至用户时必填，需使用微信支付公钥加密")
    private String realName;
}
