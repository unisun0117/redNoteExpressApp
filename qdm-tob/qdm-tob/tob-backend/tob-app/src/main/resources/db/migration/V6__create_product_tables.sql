-- V6: 商品管理基础表
-- 创建 prd_sku（商品条码表）、sys_warehouse（仓库档案表）、prd_product_catalog（商品资料表）

-- ==========================================
-- 1. 商品条码表（底层标准条码基础表）
--    商品名称为条码反查的数据来源
-- ==========================================
-- auto-generated definition
create table prd_sku
(
    barcode         varchar(24)                                           not null comment '商品条码'
        primary key,
    name            varchar(80)                                           not null comment '条码名称',
    spu             varchar(40)                                           not null comment '商品SPU编码',
    unit            varchar(10)                                           not null comment '条码单位',
    spec            varchar(20)                                           null comment '条码规格',
    tran_value      decimal(12, 3)                                        not null comment '转换比例',
    settlement_flag tinyint(1)                  default 0                 not null comment '是否结算单位（1=是）',
    weight_flag     tinyint(1)                  default 0                 null comment '是否称重商品(1-称重 0-非称重)',
    unit_weight     decimal(12, 3)                                        null comment '单位重量（千克）',
    status          enum ('ACTIVE', 'INACTIVE') default 'ACTIVE'          null comment '状态',
    memo            varchar(128)                                          null comment '备注',
    updated_at      timestamp                   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '基础sku表';

create index idx_spu
    on prd_sku (spu);
-- ==========================================
-- 2. 仓库档案表
--    仓库下拉数据源，联动销售大区过滤
-- ==========================================
create table sys_warehouse
(
    id            bigint auto_increment comment '主键'
        primary key,
    code          varchar(20)                        not null comment '仓库编码',
    name          varchar(50)                        not null comment '仓库名称',
    region        varchar(50)                        not null comment '销售大区编码',
    type          varchar(20)                        not null comment '仓库性质',
    province      varchar(20)                        null comment '省',
    city          varchar(20)                        null comment '市',
    district      varchar(20)                        null comment '区',
    address       varchar(200)                       null comment '详细地址',
    lng           decimal(10, 6)                     null comment '经度',
    lat           decimal(10, 6)                     null comment '纬度',
    created_by    varchar(64)                        null comment '创建人',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_by    varchar(64)                        null comment '修改人',
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    province_name varchar(30)                        null,
    city_name     varchar(30)                        null,
    district_name varchar(30)                        null,
    constraint uk_code
        unique (code)
)
    comment '仓库信息' charset = utf8mb4;
-- ==========================================
-- 3. 商品资料表
--    主键 = 销售大区 + 商品条码（联合唯一）
-- ==========================================
CREATE TABLE IF NOT EXISTS prd_product_catalog
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    sales_region_code VARCHAR(20)   NOT NULL COMMENT '销售大区编号',
    sales_region_name VARCHAR(50)   NOT NULL COMMENT '销售大区名称（冗余）',
    product_barcode   VARCHAR(50)   NOT NULL COMMENT '商品条码',
    product_name      VARCHAR(200)  NOT NULL COMMENT '商品名称（条码反查）',
    warehouse_code    VARCHAR(50)   NULL     COMMENT '仓库编码（SAP）',
    warehouse_name    VARCHAR(100)  NULL     COMMENT '仓库名称（冗余）',
    status            VARCHAR(20)   NOT NULL DEFAULT 'LISTED' COMMENT '状态：LISTED=已上架 UNLISTED=已下架',
    miniapp_name      VARCHAR(100)  NULL     COMMENT '小程序名称（商品别名）',
    main_image        VARCHAR(500)  NULL     COMMENT '商品主图 URL',
    carousel_images   JSON          NULL     COMMENT '轮播图 URL 列表（最多9张）',
    product_detail    TEXT          NULL     COMMENT '商品详情（富文本）',
    order_base_qty    DECIMAL(10,2) NOT NULL COMMENT '订购基数',
    order_min_qty     DECIMAL(10,2) NOT NULL COMMENT '订购下限',
    order_max_qty     DECIMAL(10,2) NOT NULL COMMENT '订购上限',
    daily_stock       DECIMAL(10,2) NOT NULL COMMENT '每日可用库存',
    daily_available   DECIMAL(10,2) NOT NULL COMMENT '今日可用数量（=库存-已售）',
    daily_sold        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '今日已售数量（订单同步）',
    created_by        VARCHAR(64)   NULL     COMMENT '创建人',
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by        VARCHAR(64)   NULL     COMMENT '修改人',
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_region_barcode (sales_region_code, product_barcode),
    INDEX idx_sales_region_code (sales_region_code),
    INDEX idx_product_barcode (product_barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品资料（按销售大区+商品条码维度）';
