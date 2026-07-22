-- V10: 公告管理表 + API 权限映射注册
--
-- ⚠️ 留档说明：与 V8 一致，本项目当前 QA 库并未运行 Flyway（db/migration 下存在重复版本号，
--    Flyway 正常启动会报错），线上表结构/权限数据通过手工执行维护。本脚本做幂等处理可安全重放。
--    公告菜单（sys_menu code=ANNOUNCEMENT, id=303）已在 V8__seed_announcement_privacy_menu.sql 注册。

-- ================================================================
-- 1. 公告表
-- ================================================================
CREATE TABLE IF NOT EXISTS sys_announcement
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    region_code     VARCHAR(50)  NOT NULL COMMENT '销售大区编号',
    region_name     VARCHAR(100) NOT NULL COMMENT '销售大区名称（冗余，创建时按编号解析）',
    content         TEXT         NOT NULL COMMENT '公告内容',
    enabled         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '启用状态 1=启用 0=停用',
    created_by      VARCHAR(64)  NULL     COMMENT '创建人',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      VARCHAR(64)  NULL     COMMENT '修改人',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (id),
    INDEX idx_region_code (region_code),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告管理';

-- ================================================================
-- 2. 注册「公告管理」API 权限映射到 sys_api_permission
--    幂等：每条用 WHERE NOT EXISTS 防重复插入
-- ================================================================
INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/announcements/list', 'ops:announcement:view', '公告-分页查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/announcements/list');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/announcements', 'ops:announcement:edit', '公告-新增', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/announcements');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'PUT', '/api/admin/announcements', 'ops:announcement:edit', '公告-编辑', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='PUT' AND `url_pattern`='/api/admin/announcements');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'PUT', '/api/admin/announcements/toggle', 'ops:announcement:edit', '公告-启用停用切换', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='PUT' AND `url_pattern`='/api/admin/announcements/toggle');
