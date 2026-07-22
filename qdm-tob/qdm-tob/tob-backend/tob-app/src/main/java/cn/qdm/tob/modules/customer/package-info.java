/**
 * 客户模块
 *
 * 职责: 处理客户、客户审核、销售员、仓库、运营区等客户运营相关业务
 *
 * 子域划分:
 * - customer: 客户管理 (客户基本信息、客户状态)
 * - audit: 客户审核 (审核流程、审核记录)
 * - salesman: 销售员 (销售员信息、销售员绩效)
 * - operation: 运营管理 (仓库、运营区、配送范围)
 *
 * 消息事件:
 * - MerchantCreatedEvent: 客户创建
 * - MerchantApprovedEvent: 客户审批通过
 * - MerchantRejectedEvent: 客户审批拒绝
 */
package cn.qdm.tob.modules.customer;
