-- 仓库信息表
CREATE TABLE IF NOT EXISTS sys_warehouse
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code             VARCHAR(20)   NOT NULL COMMENT '仓库编码',
    name             VARCHAR(50)   NOT NULL COMMENT '仓库名称',
    region           VARCHAR(50)   NOT NULL COMMENT '销售大区编码',
    type             VARCHAR(20)   NOT NULL COMMENT '仓库性质',
    province         VARCHAR(20)   NULL     COMMENT '省编码',
    city             VARCHAR(20)   NULL     COMMENT '市编码',
    district         VARCHAR(20)   NULL     COMMENT '区编码',
    province_name    VARCHAR(30)   NULL     COMMENT '省名称',
    city_name        VARCHAR(30)   NULL     COMMENT '市名称',
    district_name    VARCHAR(30)   NULL     COMMENT '区名称',
    address          VARCHAR(200)  NULL     COMMENT '详细地址',
    lng              DECIMAL(10,6) NULL     COMMENT '经度',
    lat              DECIMAL(10,6) NULL     COMMENT '纬度',
    created_by       VARCHAR(64)   NULL     COMMENT '创建人',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by       VARCHAR(64)   NULL     COMMENT '修改人',
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库信息';
