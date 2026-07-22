-- 销售大区管理表
CREATE TABLE IF NOT EXISTS sys_sales_region
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code             VARCHAR(20)  NOT NULL COMMENT '销售大区编号',
    name             VARCHAR(50)  NOT NULL COMMENT '销售大区名称',
    service_enabled  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '下单服务启用 1=启用 0=停用',
    multi_day        TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '多天订购 1=开启 0=关闭',
    min_days         INT          NULL     COMMENT '最少订购天数（多天订购开启时有效）',
    biz_hours        VARCHAR(20)  NULL     COMMENT '营业时间（格式 HH:mm ~ HH:mm）',
    order_type       VARCHAR(10)  NOT NULL DEFAULT '按金额' COMMENT '起订类型：按金额/按重量',
    order_amount     VARCHAR(50)  NULL     COMMENT '起订金额或重量',
    arrival_days     INT          NOT NULL DEFAULT 1 COMMENT '到货日期（T+N）',
    price_approval   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '价格审批 1=开启 0=关闭',
    approval_threshold DECIMAL(5,1) NULL   COMMENT '审批阈值(%)',
    approvers        JSON         NULL     COMMENT '审批人列表 JSON',
    warehouse_count  INT          NOT NULL DEFAULT 0 COMMENT '已关联仓库数',
    std_freight      DECIMAL(10,2) NULL    DEFAULT 0 COMMENT '标准物流费',
    std_free_amount  DECIMAL(10,2) NULL    DEFAULT 0 COMMENT '免运费金额',
    new_freight      DECIMAL(10,2) NULL    DEFAULT 0 COMMENT '新客物流费',
    new_free_amount  DECIMAL(10,2) NULL    DEFAULT 0 COMMENT '新客免运费金额',
    merchant_no      VARCHAR(30)  NOT NULL COMMENT '商户号',
    merchant_name    VARCHAR(100) NULL     COMMENT '商户号名称',
    region_managers  JSON         NULL     COMMENT '区域经理列表 JSON',
    covered_cities   JSON         NULL     COMMENT '覆盖城市列表 JSON',
    created_by       VARCHAR(50)  NULL     COMMENT '创建人',
    updated_by       VARCHAR(50)  NULL     COMMENT '修改人',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售大区';
-- 业务员推荐码表
CREATE TABLE IF NOT EXISTS sys_salesman
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id       BIGINT       NOT NULL COMMENT '关联后台用户ID',
    referral_code VARCHAR(4)   NULL     COMMENT '推荐码（4位数字字母）',
    code_status   VARCHAR(10)  NOT NULL DEFAULT 'VALID' COMMENT '推荐码状态：VALID=有效 EMPTY=已置空 INVALID=已失效',
    created_by    VARCHAR(50)  NULL     COMMENT '创建人',
    updated_by    VARCHAR(50)  NULL     COMMENT '修改人',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_referral_code (referral_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务员推荐码';

-- 业务员月度绩效表
CREATE TABLE IF NOT EXISTS sys_salesman_performance
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    salesman_id    BIGINT       NOT NULL COMMENT '关联业务员ID（sys_salesman.id）',
    month          VARCHAR(7)   NOT NULL COMMENT '月度（YYYY-MM）',
    order_count    INT          NOT NULL DEFAULT 0 COMMENT '订单数',
    customer_count INT          NOT NULL DEFAULT 0 COMMENT '下单客户数',
    sales_amount   DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '销售额（元）',
    created_by     VARCHAR(50)  NULL     COMMENT '创建人',
    updated_by     VARCHAR(50)  NULL     COMMENT '修改人',
    salesman_name  VARCHAR(60)  NOT NULL DEFAULT '' COMMENT '销售员名称（冗余存储）',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_salesman_month (salesman_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务员月度绩效';
-- 仓库信息表
CREATE TABLE IF NOT EXISTS sys_warehouse
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code             VARCHAR(20)   NOT NULL COMMENT '仓库编码',
    name             VARCHAR(50)   NOT NULL COMMENT '仓库名称',
    region           VARCHAR(50)   NOT NULL COMMENT '销售大区编码',
    type             VARCHAR(20)   NOT NULL COMMENT '仓库性质',
    province         VARCHAR(20)   NULL     COMMENT '省',
    city             VARCHAR(20)   NULL     COMMENT '市',
    district         VARCHAR(20)   NULL     COMMENT '区',
    address          VARCHAR(200)  NULL     COMMENT '详细地址',
    lng              DECIMAL(10,6) NULL     COMMENT '经度',
    lat              DECIMAL(10,6) NULL     COMMENT '纬度',
    created_by       VARCHAR(50)   NULL     COMMENT '创建人',
    updated_by       VARCHAR(50)   NULL     COMMENT '修改人',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息';
-- 客户公司档案主表
CREATE TABLE IF NOT EXISTS cst_company_archive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    sap_customer_code VARCHAR(30) NULL COMMENT 'SAP客户编码',
    company_name VARCHAR(100) NOT NULL COMMENT '公司名称',
    license_no VARCHAR(50) NULL COMMENT '营业执照编号',
    door_photo VARCHAR(500) NULL COMMENT '门头照URL',
    license_photo VARCHAR(500) NULL COMMENT '营业执照照URL',
    storage_photos TEXT NULL COMMENT '收货存放位置照片',
    contact_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '收货人联系电话',
    province VARCHAR(50) NOT NULL COMMENT '所在省份',
    city VARCHAR(50) NOT NULL COMMENT '所在城市',
    district VARCHAR(50) NOT NULL COMMENT '所在区县',
    address VARCHAR(500) NOT NULL COMMENT '详细收货地址',
    longitude DECIMAL(10,7) NULL COMMENT '经度',
    latitude DECIMAL(10,7) NULL COMMENT '纬度',
    receive_time_start VARCHAR(10) DEFAULT '00:00',
    receive_time_end VARCHAR(10) DEFAULT '08:00',
    receive_requirement TEXT NULL COMMENT '收货要求',
    sales_region_id BIGINT NULL,
    sales_region_name VARCHAR(50) NULL,
    salesman_id BIGINT NULL,
    salesman_name VARCHAR(50) NULL,
    referral_code VARCHAR(10) NULL,
    audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态',
    price_group VARCHAR(50) NULL,
    settle_company VARCHAR(100) NULL,
    business_type VARCHAR(50) NULL,
    settle_type VARCHAR(20) NULL COMMENT '结算类型：CASH/PERIOD',
    internal_remark TEXT NULL,
    auditor_id BIGINT NULL,
    auditor_name VARCHAR(50) NULL,
    auditor_type VARCHAR(20) NULL,
    audit_reject_reason TEXT NULL,
    audit_time DATETIME NULL,
    submit_user_id BIGINT NULL,
    submit_user_name VARCHAR(50) NULL,
    last_order_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_audit_status (audit_status),
    INDEX idx_company_name (company_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户公司档案';
-- 审核历史流转表
CREATE TABLE IF NOT EXISTS cst_archive_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    archive_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    operator_id BIGINT NOT NULL,
    operator_type VARCHAR(20) NOT NULL,
    operator_name VARCHAR(50) NULL,
    remark TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_archive_id (archive_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='档案审核历史';
-- 地址-小程序用户绑定表
CREATE TABLE IF NOT EXISTS cst_archive_user_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    archive_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) NULL,
    user_mobile VARCHAR(20) NULL,
    member_role VARCHAR(20) DEFAULT 'MEMBER',
    invite_code VARCHAR(20) NULL,
    invite_code_created_at DATETIME NULL,
    binding_status VARCHAR(20) DEFAULT 'BOUND',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_archive_user (archive_id, user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='档案用户绑定';
