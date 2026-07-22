-- V8: 商品价格管理基础表
-- 创建 prd_price_group（价格组表）、prd_price_detail（价格组明细表）

-- ==========================================
-- 1. 价格组表
--    主键 = 销售大区 + 价格组编码（联合唯一）
-- ==========================================
CREATE TABLE IF NOT EXISTS prd_price_group
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    sales_region_code VARCHAR(60)  NOT NULL COMMENT '销售大区编号',
    sales_region_name VARCHAR(100) NOT NULL COMMENT '销售大区名称（冗余）',
    price_group_code  VARCHAR(60)  NOT NULL COMMENT '价格组编码',
    price_group_name  VARCHAR(100) NOT NULL COMMENT '价格组名称',
    description       VARCHAR(500) NULL     COMMENT '描述说明',
    created_by        VARCHAR(64)  NULL     COMMENT '创建人',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by        VARCHAR(64)  NULL     COMMENT '修改人',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_region_group (sales_region_code, price_group_code),
    INDEX idx_sales_region_code (sales_region_code),
    INDEX idx_price_group_name (price_group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格组（按销售大区维度）';

-- ==========================================
-- 2. 价格组明细表
--    主键 = 销售大区 + 价格组编码 + 商品条码（联合唯一）
-- ==========================================
CREATE TABLE IF NOT EXISTS prd_price_detail
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    sales_region_code VARCHAR(60)   NOT NULL COMMENT '销售大区编号',
    price_group_code  VARCHAR(60)   NOT NULL COMMENT '价格组编码',
    product_barcode   VARCHAR(60)   NOT NULL COMMENT '商品条码',
    product_name      VARCHAR(200)  NOT NULL COMMENT '商品名称（条码反查）',
    price             DECIMAL(10,2) NOT NULL COMMENT '售价（元）',
    change_reason     VARCHAR(50)   NULL     COMMENT '变动原因',
    approval_status   VARCHAR(20)   NOT NULL DEFAULT 'NONE' COMMENT '审批状态：NONE=无需审批 PENDING=审批中 APPROVED=审批通过 REJECTED=审批驳回',
    pending_price     DECIMAL(10,2) NULL     COMMENT '待审批的新价格',
    created_by        VARCHAR(64)   NULL     COMMENT '创建人',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by        VARCHAR(64)   NULL     COMMENT '修改人',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_region_group_barcode (sales_region_code, price_group_code, product_barcode),
    INDEX idx_sales_region_code (sales_region_code),
    INDEX idx_price_group_code (price_group_code),
    INDEX idx_product_barcode (product_barcode),
    INDEX idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格组明细（按价格组+商品条码维度）';
