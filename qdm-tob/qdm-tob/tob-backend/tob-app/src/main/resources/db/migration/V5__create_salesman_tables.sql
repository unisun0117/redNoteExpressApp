-- 业务员推荐码表
CREATE TABLE IF NOT EXISTS sys_salesman
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id       BIGINT       NOT NULL COMMENT '关联后台用户ID',
    referral_code VARCHAR(4)   NULL     COMMENT '推荐码（4位数字字母）',
    code_status   VARCHAR(10)  NOT NULL DEFAULT 'VALID' COMMENT '推荐码状态：VALID=有效 EMPTY=已置空 INVALID=已失效',
    created_by    VARCHAR(64)  NULL     COMMENT '创建人',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by    VARCHAR(64)  NULL     COMMENT '修改人',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_referral_code (referral_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务员推荐码';

-- 业务员月度绩效表
CREATE TABLE IF NOT EXISTS sys_salesman_performance
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    salesman_id    BIGINT       NOT NULL COMMENT '关联业务员ID（sys_salesman.id）',
    salesman_name  VARCHAR(60)  NOT NULL DEFAULT '' COMMENT '销售员名称（冗余存储，避免跨表查询）',
    month          VARCHAR(7)   NOT NULL COMMENT '月度（YYYY-MM）',
    order_count    INT          NOT NULL DEFAULT 0 COMMENT '订单数',
    customer_count INT          NOT NULL DEFAULT 0 COMMENT '下单客户数',
    sales_amount   DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '销售额（元）',
    created_by     VARCHAR(64)  NULL     COMMENT '创建人',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by     VARCHAR(64)  NULL     COMMENT '修改人',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_salesman_month (salesman_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务员月度绩效';
