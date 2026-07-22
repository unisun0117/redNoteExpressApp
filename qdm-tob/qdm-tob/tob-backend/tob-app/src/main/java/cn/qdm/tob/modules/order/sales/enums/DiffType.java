package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 差补类型枚举
 */
@Getter
@AllArgsConstructor
public enum DiffType implements Describable {

    /** 退款 */
    REFUND("退款"),
    /** 补款 */
    SUPPLEMENT("补款");

    private final String description;
}
