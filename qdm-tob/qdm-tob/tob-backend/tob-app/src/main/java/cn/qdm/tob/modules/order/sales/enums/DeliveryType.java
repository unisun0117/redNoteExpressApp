package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送方式枚举
 */
@Getter
@AllArgsConstructor
public enum DeliveryType implements Describable {

    /** 物流配送 */
    LOGISTICS("物流配送");

    private final String description;
}
