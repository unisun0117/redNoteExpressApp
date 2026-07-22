package cn.qdm.tob.client.sap.enums;

import cn.qdm.tob.framework.Describable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 支付类型
 */
@Getter
public enum PaymentType implements Describable {

    WECHAT_PAY("ZY16", "微信支付"),
    ALI_PAY("ZY17", "支付宝");

    private final String value;
    private final String description;

    PaymentType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
