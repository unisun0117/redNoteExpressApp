package cn.qdm.tob.modules.order.sales.enums;

import cn.qdm.tob.framework.Describable;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatus implements Describable {

    /** 待支付 */
    PENDING_PAYMENT("待支付"),
    /** 待出库 */
    PENDING_OUTBOUND("待出库"),
    /** 出库中 */
    OUTBOUND_IN_PROGRESS("出库中"),
    /** 已完成 */
    COMPLETED("已完成"),
    /** 已取消 */
    CANCELLED("已取消");

    private final String description;
}
