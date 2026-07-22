-- auto-generated definition
create table prd_price_group
(
    id                bigint auto_increment comment '主键'
        primary key,
    sales_region_code varchar(60)                        not null comment '销售大区编号',
    sales_region_name varchar(100)                       not null comment '销售大区名称（冗余）',
    price_group_code  varchar(60)                        not null comment '价格组编码',
    price_group_name  varchar(100)                       not null comment '价格组名称',
    description       varchar(500)                       null comment '描述说明',
    created_by        varchar(50)                        null comment '创建人',
    updated_by        varchar(50)                        null comment '更新人',
    created_at        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at        datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_region_group
        unique (sales_region_code, price_group_code)
)
    comment '价格组（按销售大区维度）' charset = utf8mb4;

create index idx_price_group_name
    on prd_price_group (price_group_name);

create index idx_sales_region_code
    on prd_price_group (sales_region_code);

