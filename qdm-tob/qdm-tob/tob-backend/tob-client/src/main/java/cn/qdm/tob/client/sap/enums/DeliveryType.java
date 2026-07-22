package cn.qdm.tob.client.sap.enums;

import cn.qdm.tob.framework.Describable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 配送方式
 */
@Getter
public enum DeliveryType implements Describable {

    SELF_DELIVERY("A", "自提"),
    LOGISTICS("B", "送货上门");

    private final String value;
    private final String description;

    DeliveryType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
