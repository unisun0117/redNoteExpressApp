/**
 * 订单模块
 *
 * 职责: 处理销售订单、退货单、支付流水等订单相关业务
 *
 * 子域划分:
 * - sales: 销售单 (销售订单的创建、修改、取消等)
 * - refund: 退货单 (退货的申请、审核、处理)
 * - payment: 支付流水 (支付记录、退款记录、流水查询)
 * - query: 订单查询 (只读数据聚合，支持多维度查询)
 *
 * 消息事件:
 * - SalesOrderCreatedEvent: 销售单创建
 * - SalesOrderCancelledEvent: 销售单取消
 * - RefundOrderCreatedEvent: 退货单创建
 * - PaymentConfirmedEvent: 支付确认
 */
package cn.qdm.tob.modules.order;
