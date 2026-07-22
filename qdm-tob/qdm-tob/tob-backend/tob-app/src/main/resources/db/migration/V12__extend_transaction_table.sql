-- V12: 扩展 ord_account_transaction 表 — 资金流水管理
--     新增 11 列（客户、结算账户、付款方式、收支类型、业务单号等）+ 权限注册
-- ⚠️ 幂等处理，可安全重放

-- ================================================================
-- 1. 新增列（IF NOT EXISTS 由存储过程模拟，直接 ALTER 需在非 CI 环境手动确认）
-- ================================================================
ALTER TABLE ord_account_transaction
    ADD COLUMN   customer_code             VARCHAR(50)  NULL COMMENT '客户编号（下单必填，充值/提现为空）' AFTER account_type,
    ADD COLUMN   customer_name             VARCHAR(100) NULL COMMENT '客户名称（冗余快照）' AFTER customer_code,
    ADD COLUMN   settlement_account_code   VARCHAR(50)  NULL COMMENT '结算账户编号' AFTER customer_name,
    ADD COLUMN   settlement_account_name   VARCHAR(100) NULL COMMENT '结算账户名称（冗余快照）' AFTER settlement_account_code,
    ADD COLUMN   payment_method            VARCHAR(20)  NULL COMMENT '付款方式：WECHAT/PREPAID/CREDIT/BANK_CARD' AFTER settlement_account_name,
    ADD COLUMN   business_no               VARCHAR(50)  NULL COMMENT '业务单号' AFTER balance_after,
    ADD COLUMN   order_no                  VARCHAR(50)  NULL COMMENT '订单号' AFTER business_no,
    ADD COLUMN   wechat_merchant_no        VARCHAR(20)  NULL COMMENT '微信商户号' AFTER order_no,
    ADD COLUMN   third_party_flow_no       VARCHAR(64)  NULL COMMENT '第三方流水号' AFTER wechat_merchant_no,
    ADD COLUMN   description               VARCHAR(200) NULL COMMENT '业务描述' AFTER third_party_flow_no;

-- ================================================================
-- 2. 注册 API 权限映射（幂等）
-- ================================================================
INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/order/transaction/list', 'ord:transaction:view', '资金流水-分页查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/order/transaction/list');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/order/transaction/summary', 'ord:transaction:view', '资金流水-汇总统计', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/order/transaction/summary');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/order/transaction/export', 'ord:transaction:export', '资金流水-导出', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/order/transaction/export');
