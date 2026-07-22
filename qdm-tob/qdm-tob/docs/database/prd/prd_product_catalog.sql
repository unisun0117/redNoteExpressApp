-- auto-generated definition
create table prd_product_catalog
(
    id                bigint auto_increment comment '主键'
        primary key,
    sales_region_code varchar(20)                              not null comment '销售大区编号',
    sales_region_name varchar(50)                              not null comment '销售大区名称（冗余）',
    product_barcode   varchar(50)                              not null comment '商品条码',
    product_name      varchar(200)                             not null comment '商品名称（条码反查）',
    warehouse_code    varchar(50)                              null comment '仓库编码（SAP）',
    warehouse_name    varchar(100)                             null comment '仓库名称（冗余）',
    status            varchar(20)    default 'LISTED'          not null comment '状态：LISTED=已上架 UNLISTED=已下架',
    miniapp_name      varchar(100)                             null comment '小程序名称（商品别名）',
    main_image        varchar(500)                             null comment '商品主图 URL',
    carousel_images   json                                     null comment '轮播图 URL 列表（最多9张）',
    product_detail    text                                     null comment '商品详情（富文本）',
    order_base_qty    decimal(10, 2)                           not null comment '订购基数',
    order_min_qty     decimal(10, 2)                           not null comment '订购下限',
    order_max_qty     decimal(10, 2)                           not null comment '订购上限',
    daily_stock       decimal(10, 2)                           not null comment '每日可用库存',
    daily_available   decimal(10, 2)                           not null comment '今日可用数量（=库存-已售）',
    daily_sold        decimal(10, 2) default 0.00              not null comment '今日已售数量（订单同步）',
    created_by        varchar(50)                              null comment '创建人',
    updated_by        varchar(50)                              null comment '修改人',
    created_at        datetime       default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at        datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    constraint uk_region_barcode
        unique (sales_region_code, product_barcode)
)
    comment '商品资料（按销售大区+商品条码维度）' charset = utf8mb4;

create index idx_product_barcode
    on prd_product_catalog (product_barcode);

create index idx_sales_region_code
    on prd_product_catalog (sales_region_code);

