-- V11: 订单模块 — 客户账户管理（ord_customer_account + ord_account_transaction）
--
-- ⚠️ 留档说明：本项目当前 QA 库并未运行 Flyway（db/migration 下存在重复版本号，
--    Flyway 正常启动会报错），线上表结构通过手工执行维护。本脚本做幂等处理可安全重放。
--    客户信息（名称、营业执照等）不冗余存储，查询时从 cst_company_archive 实时获取。

-- ================================================================
-- 1. 客户账户表
-- ================================================================
CREATE TABLE IF NOT EXISTS ord_customer_account
(
    id                        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    customer_code             VARCHAR(50)   NOT NULL COMMENT '客户编码（关联 cst_company_archive.sap_customer_code）',
    balance                   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额（元）',
    account_type              VARCHAR(20)   NOT NULL DEFAULT 'PREPAID' COMMENT '账户类型：PREPAID=预付款 CREDIT=账期',
    credit_days               INT           NULL     COMMENT '账期天数（仅 CREDIT 类型有值）',
    next_reconciliation_date  DATE          NULL     COMMENT '下一对账日期',
    remark                    VARCHAR(500)  NULL     COMMENT '备注',
    created_at                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_customer_code (customer_code),
    INDEX idx_account_type (account_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户账户';

-- ================================================================
-- 2. 账户流水记录表
-- ================================================================
CREATE TABLE IF NOT EXISTS ord_account_transaction
(
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    transaction_no    VARCHAR(32)   NOT NULL COMMENT '流水号（YYYYMMDD + 4位序号）',
    account_id        BIGINT        NULL COMMENT '关联账户ID',
    account_type      VARCHAR(20)   NULL COMMENT '冗余账户类型（PREPAID / CREDIT）',
    transaction_type  VARCHAR(20)   NOT NULL COMMENT '流水类型：RECHARGE=充值 WITHDRAW=提现',
    amount            DECIMAL(10,2) NOT NULL COMMENT '金额（>0，单位：元）',
    balance_before    DECIMAL(10,2) NULL     COMMENT '交易前余额',
    balance_after     DECIMAL(10,2) NULL     COMMENT '交易后余额',
    status            VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS' COMMENT '交易状态：PROCESSING / SUCCESS / FAILED',
    operator_id       BIGINT        NULL     COMMENT '操作人ID',
    operator_name     VARCHAR(50)   NULL     COMMENT '操作人姓名',
    remark            VARCHAR(2000)  NULL     COMMENT '备注',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_transaction_no (transaction_no),
    INDEX idx_account_id (account_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户流水记录';

-- ================================================================
-- 3. 注册 API 权限映射到 sys_api_permission（幂等）
-- ================================================================
INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/order/account/list', 'ord:account:view', '客户账户-分页查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/order/account/list');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/order/account/customer', 'ord:account:view', '客户账户-查询客户信息', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/order/account/customer');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/order/account/transaction', 'ord:account:transaction:create', '客户账户-新增流水', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/order/account/transaction');
