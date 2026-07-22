-- auto-generated definition
create table prd_price_detail
(
    id                bigint auto_increment comment '主键'
        primary key,
    sales_region_code varchar(60)                           not null comment '销售大区编号',
    price_group_code  varchar(60)                           not null comment '价格组编码',
    product_barcode   varchar(60)                           not null comment '商品条码',
    product_name      varchar(200)                          not null comment '商品名称（条码反查）',
    price             decimal(10, 2)                        not null comment '售价（元）',
    change_reason     varchar(50)                           null comment '变动原因',
    approval_status   varchar(20) default 'NONE'            not null comment '审批状态：NONE=无需审批 PENDING=审批中 APPROVED=审批通过 REJECTED=审批驳回',
    pending_price     decimal(10, 2)                        null comment '待审批的新价格',
    created_by        varchar(50)                           null comment '创建人',
    updated_by        varchar(50)                           null comment '更新人',
    created_at        datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at        datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_region_group_barcode
        unique (sales_region_code, price_group_code, product_barcode)
)
    comment '价格组明细（按价格组+商品条码维度）' charset = utf8mb4;

create index idx_approval_status
    on prd_price_detail (approval_status);

create index idx_price_group_code
    on prd_price_detail (price_group_code);

create index idx_product_barcode
    on prd_price_detail (product_barcode);

create index idx_sales_region_code
    on prd_price_detail (sales_region_code);

