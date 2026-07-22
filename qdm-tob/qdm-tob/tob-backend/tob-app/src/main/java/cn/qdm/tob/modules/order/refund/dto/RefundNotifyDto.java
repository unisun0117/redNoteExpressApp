package cn.qdm.tob.modules.order.refund.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 退款结果回调通知中的业务数据（resource.ciphertext 解密后的明文）。
 *
 * <p>注意：此 DTO 用于解析解密后的回调业务数据，并非微信回调的原始 JSON 结构。</p>
 */
@Data
public class RefundNotifyDto {

    @SerializedName("mchid")
    private String mchid;

    @SerializedName("out_trade_no")
    private String outTradeNo;

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("out_refund_no")
    private String outRefundNo;

    @SerializedName("refund_id")
    private String refundId;

    @SerializedName("refund_status")
    private String refundStatus;

    @SerializedName("success_time")
    private String successTime;

    @SerializedName("user_received_account")
    private String userReceivedAccount;

    @SerializedName("amount")
    private RefundNotifyAmount amount;

    @Data
    public static class RefundNotifyAmount {
        @SerializedName("total")
        private Long total;

        @SerializedName("refund")
        private Long refund;

        @SerializedName("payer_total")
        private Long payerTotal;

        @SerializedName("payer_refund")
        private Long payerRefund;
    }
}
