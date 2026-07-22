package cn.qdm.tob.modules.order;

import org.springframework.modulith.ApplicationModule;

/**
 * 订单模块
 *
 * 职责: 处理销售订单、退货单、支付流水等订单相关业务
 *
 * 子域:
 * - sales: 销售单
 * - refund: 退货单
 * - payment: 支付流水
 * - query: 订单查询
 */
@ApplicationModule(displayName = "Order Module")
public class OrderModule {
}
