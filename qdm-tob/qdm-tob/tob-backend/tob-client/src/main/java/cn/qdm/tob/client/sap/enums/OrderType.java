package cn.qdm.tob.client.sap.enums;

import cn.qdm.tob.framework.Describable;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 订单类型
 */
@Getter
public enum OrderType implements Describable {

    SALES(1, "订货单"),
    RETURN(2, "退货单"),
    URGENT(3, "紧急加单"),
    DIFF(4, "差补单"),
    OFFLINE(5, "线下尾货单"),
    MATERIAL(6, "物料订货单"),
    MATERIAL_RETURN(7, "物料退货单"),
    MATERIAL_DIFF(8, "物料差补单"),
    MONTH_CUSTOMER_ORDER(13, "月结单"),
    MONTH_CUSTOMER_RETURN(14, "月结退货单");

    private final Integer value;
    private final String description;

    OrderType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    @Override
    public String toString() {
        return value.toString();
    }
}
