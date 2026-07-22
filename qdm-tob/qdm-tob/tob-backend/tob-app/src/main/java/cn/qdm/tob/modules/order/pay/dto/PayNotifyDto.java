package cn.qdm.tob.modules.order.pay.dto;

import lombok.Data;

/**
 * 微信支付成功回调通知中的业务数据（resource.ciphertext 解密后的明文）。
 * <p>
 * 注意：此 DTO 用于解析解密后的回调业务数据，并非微信回调的原始 JSON 结构。
 * </p>
 */
@Data
public class PayNotifyDto {

    /**
     * 商户下单时传入的公众账号 ID。
     */
    private String appid;

    /**
     * 商户号。
     */
    private String mchid;

    /**
     * 商户订单号。
     */
    private String outTradeNo;

    /**
     * 微信支付订单号。
     */
    private String transactionId;

    /**
     * 交易类型，如 JSAPI、NATIVE 等。
     */
    private String tradeType;

    /**
     * 交易状态：SUCCESS、REFUND、NOTPAY、CLOSED 等。
     */
    private String tradeState;

    /**
     * 交易状态描述。
     */
    private String tradeStateDesc;

    /**
     * 银行类型。
     */
    private String bankType;

    /**
     * 商户数据包（下单时传入的 attach 字段）。
     */
    private String attach;

    /**
     * 支付完成时间，RFC3339 格式。
     */
    private String successTime;

    /**
     * 支付者 openid。
     */
    private String openid;

    /**
     * 订单总金额，单位为分。
     */
    private Integer totalAmount;

    /**
     * 用户支付金额，单位为分。
     */
    private Integer payerTotalAmount;

    /**
     * 货币类型，固定为 CNY。
     */
    private String currency;
}
