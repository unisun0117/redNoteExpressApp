package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退货状态枚举
 */
@Getter
@AllArgsConstructor
public enum ReturnStatus implements Describable {

    /** 未退货 */
    NONE("未退货"),
    /** 已退货 */
    RETURNED("已退货");

    private final String description;
}
