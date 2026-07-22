package cn.qdm.tob.client.sap.enums;

import cn.qdm.tob.framework.Describable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 销售渠道
 */
@Getter
public enum OrderChannel implements Describable {

    ONLINE("A", "B2B线上"),
    ONLINE_TAIL("B", "线上尾货"),
    OFFLINE_TAIL("C", "线下尾货");

    private final String value;
    private final String description;

    OrderChannel(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
