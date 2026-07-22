-- V9: 小程序隐私政策管理 —— 隐私文档表 + 授权记录表
--
-- ⚠️ 留档说明：本项目当前 QA 库并未运行 Flyway（详见 V8 头注）。本文件作为「表结构变更留档」，
--    与线上手工执行保持一致。CREATE TABLE IF NOT EXISTS 保证可安全重放。
--
-- sys_privacy_doc        隐私文档（隐私政策 / 摘要 / 用户协议 等），状态机：未发布 → 已发布 → 已下架
-- sys_privacy_auth_record 小程序端授权记录（相机 / 相册 / 隐私政策同意 等）

-- ==========================================
-- 1. 隐私文档表
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_privacy_doc
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    doc_type        VARCHAR(32)   NOT NULL COMMENT '文档类型：PRIVACY_POLICY/PRIVACY_SUMMARY/USER_RULES/INFO_COLLECTION/THIRD_PARTY_SHARING/USER_AGREEMENT',
    version         VARCHAR(50)   NOT NULL COMMENT '版本号',
    h5_url          VARCHAR(500)  NOT NULL COMMENT 'H5 展示链接',
    file_url        VARCHAR(500)  NULL     COMMENT '附件文件链接（可选）',
    remark          VARCHAR(200)  NULL     COMMENT '备注',
    rich_content    LONGTEXT      NULL     COMMENT '富文本内容',
    status          VARCHAR(20)   NOT NULL DEFAULT 'UNPUBLISHED' COMMENT '状态：UNPUBLISHED=未发布 PUBLISHED=已发布 WITHDRAWN=已下架',
    created_by      VARCHAR(64)   NULL     COMMENT '创建人',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(64)   NULL     COMMENT '修改人',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    INDEX idx_doc_type (doc_type),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '隐私文档';

-- ==========================================
-- 2. 隐私授权记录表
-- ==========================================
CREATE TABLE IF NOT EXISTS sys_privacy_auth_record
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    openid     VARCHAR(64) NOT NULL COMMENT '小程序用户 openid',
    phone      VARCHAR(20) NULL     COMMENT '手机号',
    auth_type  VARCHAR(32) NOT NULL COMMENT '授权类型：CAMERA/ALBUM/PRIVACY_POLICY/PRIVACY_SUMMARY/USER_RULES/USER_AGREEMENT/INFO_DOWNLOAD',
    version    VARCHAR(50) NULL     COMMENT '授权时对应的文档版本号',
    auth_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    INDEX idx_openid (openid),
    INDEX idx_auth_type (auth_type),
    INDEX idx_auth_time (auth_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '隐私授权记录';
