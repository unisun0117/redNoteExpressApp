-- V11: 创建销售订单模块表
-- 包含：ord_sales_order（销售订单主表）、ord_order_diff（差补单表）、
--        ord_order_item（订单商品明细表）、ord_cart_item（购物车表）

-- ==========================================
-- 1. 销售订单主表
-- ==========================================
CREATE TABLE IF NOT EXISTS ord_sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_no VARCHAR(20) NOT NULL COMMENT '订单编号（唯一）',
    order_status ENUM('PENDING_PAYMENT','PENDING_OUTBOUND',
        'OUTBOUND_IN_PROGRESS','COMPLETED','CANCELLED')
        NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态：待支付/待出库/出库中/已完成/已取消',

    -- 关联ID（逻辑外键）
    customer_id BIGINT NOT NULL COMMENT '客户档案ID',
    sales_region_id BIGINT NOT NULL COMMENT '销售大区ID',
    salesman_id BIGINT NULL COMMENT '销售员ID',
    submit_user_id BIGINT NOT NULL COMMENT '下单人（小程序用户）ID',

    -- 快照字段
    customer_code VARCHAR(30) NOT NULL COMMENT '客户编码（快照）',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称（快照）',
    customer_type VARCHAR(20) NULL COMMENT '客户类型（快照）',
    submit_user_name VARCHAR(50) NOT NULL COMMENT '下单人姓名（快照）',
    submit_user_phone VARCHAR(20) NOT NULL COMMENT '下单手机号（快照）',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名（快照）',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话（快照）',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收货地址（快照）',

    -- 配送信息
    delivery_type ENUM('LOGISTICS') NOT NULL DEFAULT 'LOGISTICS' COMMENT '配送方式',
    delivery_start_time DATETIME NULL COMMENT '配送时间起',
    delivery_end_time DATETIME NULL COMMENT '配送时间止',
    delivery_remark VARCHAR(200) NULL COMMENT '配送备注',

    -- 时间节点
    order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    arrival_date DATE NULL COMMENT '期望到货日期',
    cancel_time DATETIME NULL COMMENT '取消时间',

    -- 金额（DECIMAL(12,2)）
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额（=商品总金额+运费）',
    goods_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额（Σ各行商品总金额）',
    promotion_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '促销金额（整单）',
    coupon_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠券金额（整单）',
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '折扣金额（整单）',
    goods_paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '商品实付金额（=商品总金额-促销-优惠券-折扣）',
    freight_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实付总金额（=商品实付金额+运费）',

    -- 结算/支付
    settle_customer_code VARCHAR(30) NULL COMMENT '结算客户编码',
    pay_method ENUM('WECHAT','CREDIT','BALANCE') NULL COMMENT '支付方式：微信/账期/余额',
    pay_transaction_id VARCHAR(64) NULL COMMENT '支付流水号（微信/余额支付回调回写）',
    pay_time DATETIME NULL COMMENT '支付时间',
    merchant_id VARCHAR(32) NULL COMMENT '商户号（微信回调回写）',
    merchant_name VARCHAR(100) NULL COMMENT '商户号名称（微信回调回写）',

    -- SAP 推送（订单级）
    sap_push_status ENUM('PENDING','PUSHED','FAILED')
        NOT NULL DEFAULT 'PENDING' COMMENT 'SAP推送状态：待推送/已推送/推送失败',
    sap_push_time DATETIME NULL COMMENT '推送SAP时间',
    sap_picking_time DATETIME NULL COMMENT 'SAP分拣出库单回执时间（回写各明细actual_qty）',

    -- 退货/物流
    return_status ENUM('NONE','RETURNED') NOT NULL DEFAULT 'NONE' COMMENT '退货状态：未退货/已退货',
    logistics_no VARCHAR(50) NULL COMMENT '物流单号（出库后回写，查询TMS获取司机/轨迹/时间节点）',

    -- 审计字段
    created_by VARCHAR(64) NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_order_time (order_time),
    INDEX idx_customer_id (customer_id),
    INDEX idx_sales_region_id (sales_region_id),
    INDEX idx_salesman_id (salesman_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

-- ==========================================
-- 2. 差补单表（1:1 扩展原订单）
-- ==========================================
CREATE TABLE IF NOT EXISTS ord_order_diff (
    order_id BIGINT NOT NULL PRIMARY KEY COMMENT '原订单ID（即ord_sales_order.id，一一对应）',
    order_no VARCHAR(20) NOT NULL COMMENT '原订单编号（冗余，方便展示）',
    diff_status ENUM('PENDING','PENDING_PAYMENT','COMPLETED')
        NOT NULL DEFAULT 'PENDING' COMMENT '差补状态：待处理/待支付/已完成',
    diff_type ENUM('REFUND','SUPPLEMENT') NOT NULL COMMENT '差补类型：退款/补款',
    diff_amount DECIMAL(12,2) NOT NULL COMMENT '差补总金额（Σ各明细diff_qty×应退/应补单价）',

    -- 补款支付（仅 diff_type=SUPPLEMENT 且 原订单 pay_method=WECHAT 时使用）
    pay_transaction_id VARCHAR(64) NULL COMMENT '补款支付流水号',
    pay_time DATETIME NULL COMMENT '补款支付时间',

    -- SAP 推送（差补单也需要推送 SAP）
    sap_push_status ENUM('PENDING','PUSHED','FAILED')
        NOT NULL DEFAULT 'PENDING' COMMENT 'SAP推送状态：待推送/已推送/推送失败',
    sap_push_time DATETIME NULL COMMENT '推送SAP时间',
    sap_picking_time DATETIME NULL COMMENT 'SAP回执时间',

    reason VARCHAR(500) NULL COMMENT '差补原因',
    created_by VARCHAR(64) NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='差补单表';

-- ==========================================
-- 3. 订单商品明细表（普通+差补共用）
-- ==========================================
CREATE TABLE IF NOT EXISTS ord_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID（关联ord_sales_order.id）',
    warehouse_id BIGINT NULL COMMENT '配送仓库ID（行级指定，不同商品可从不同仓库出货）',
    sku_id VARCHAR(32) NULL COMMENT '商品SKU ID',

    -- 商品快照
    barcode VARCHAR(50) NOT NULL COMMENT '商品条码（快照）',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
    unit VARCHAR(20) NULL COMMENT '结算单位（快照，如千克/箱）',

    -- 数量三要素
    quantity DECIMAL(10,2) NOT NULL COMMENT '订购数量',
    actual_qty DECIMAL(10,2) NULL COMMENT '实际出库数量（SAP回写后更新本行）',
    diff_qty DECIMAL(10,2) NULL COMMENT '差异数量（=actual_qty-quantity，≠0触发差补）',

    -- 价格
    unit_price DECIMAL(12,2) NOT NULL COMMENT '商品单价（快照，下单时销售价）',
    goods_total DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '行级商品总金额（=quantity×unit_price）',

    -- 优惠分摊（整单按比例拆分到行，尾差归金额最大行）
    promotion_share DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '促销分摊金额',
    coupon_share DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠券分摊金额',
    discount_share DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '折扣分摊金额',
    goods_paid DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '行级实付金额（=goods_total-promotion-coupon-discount）',

    -- 附加信息
    extra_service VARCHAR(200) NULL COMMENT '附加服务（如加急、保温包装）',

    -- 审计字段
    created_by VARCHAR(64) NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';

-- ==========================================
-- 4. 购物车表
-- ==========================================
CREATE TABLE IF NOT EXISTS ord_cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '小程序用户ID',
    barcode VARCHAR(50) NOT NULL COMMENT '商品条码',
    goods_name VARCHAR(100) NULL COMMENT '商品名称（冗余，列表展示用）',
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '数量',
    selected TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否勾选（0=否 1=是）',
    created_by VARCHAR(64) NULL COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(64) NULL COMMENT '修改人',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_user_barcode (user_id, barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
