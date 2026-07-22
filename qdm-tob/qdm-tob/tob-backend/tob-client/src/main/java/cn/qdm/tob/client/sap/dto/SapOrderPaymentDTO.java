package cn.qdm.tob.client.sap.dto;

import cn.qdm.tob.client.sap.enums.PaymentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhaoxiaoyun
 */
@Data
public class SapOrderPaymentDTO {
    public SapOrderPaymentDTO() { }

    public SapOrderPaymentDTO(PaymentType type, BigDecimal amount) {
        this.payType = type;
        this.payAmt = amount;
    }

    /**
     * 支付方式
     * ZY16 自研平台-微信（B2B）
     * ZY17 自研平台-支付宝（B2B）
     */
    @JsonProperty("pay_type")
    private PaymentType payType;

    /**
     * 支付金额
     */
    @JsonProperty("pay_amt")
    private BigDecimal payAmt;
}
