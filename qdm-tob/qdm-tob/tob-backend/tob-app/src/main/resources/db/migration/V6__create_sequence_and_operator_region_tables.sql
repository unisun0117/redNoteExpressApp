-- 通用序列号表 + 运营人员推荐码 + 运营人员-销售大区关联表
CREATE TABLE IF NOT EXISTS sys_sequence
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    seq_key     VARCHAR(64) NOT NULL COMMENT '序列标识',
    current_val BIGINT      NOT NULL DEFAULT 0 COMMENT '当前值',
    step        INT         NOT NULL DEFAULT 1 COMMENT '步长',
    formatter   VARCHAR(64) NULL     COMMENT '格式化模板，如 %04d',
    description VARCHAR(128) NULL    COMMENT '描述',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME    NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_seq_key (seq_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用序列号';

-- 初始化用户推荐码序列
INSERT INTO sys_sequence (seq_key, current_val, step, formatter, description, created_at)
VALUES ('USER_RECOMMEND_CODE', 0, 1, '%04d', '用户推荐码（4位自增）', NOW());

-- sys_operator 新增推荐码字段（4位字符，系统自动生成，隐性字段）
ALTER TABLE sys_operator
    ADD COLUMN recommend_code VARCHAR(4) NULL COMMENT '推荐码（4位自增）' AFTER last_login_at,
    ADD UNIQUE INDEX uk_recommend_code (recommend_code);

-- 运营人员-销售大区数据权限关联表
CREATE TABLE IF NOT EXISTS sys_operator_region
(
    operator_id  BIGINT      NOT NULL COMMENT '运营人员 ID',
    region_code  VARCHAR(20) NOT NULL COMMENT '销售大区编号（sys_sales_region.code）',
    created_by   VARCHAR(64) COMMENT '创建人',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    PRIMARY KEY (operator_id, region_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营人员-销售大区数据权限关联';
