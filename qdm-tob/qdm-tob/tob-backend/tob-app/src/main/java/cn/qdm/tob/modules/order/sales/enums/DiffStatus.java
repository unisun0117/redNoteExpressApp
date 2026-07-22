package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 差补状态枚举
 */
@Getter
@AllArgsConstructor
public enum DiffStatus implements Describable {

    /** 待处理 */
    PENDING("待处理"),
    /** 待支付 */
    PENDING_PAYMENT("待支付"),
    /** 已完成 */
    COMPLETED("已完成");

    private final String description;
}
