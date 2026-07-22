-- V8: 注册「公告管理」「小程序隐私政策」两个页面菜单到 sys_menu，并绑定到 role#1
--
-- ⚠️ 留档说明：本项目当前 QA 库并未运行 Flyway（qdm_tob 库中不存在 flyway_schema_history 表，
--    且 db/migration 下存在重复版本号 V5/V6，Flyway 正常启动会报错）。sys_menu 的菜单数据实际是
--    直接写入数据库 / 通过后台菜单管理界面维护的。本文件仅作为「菜单变更留档」，与线上手工执行保持一致。
--    若后续正式启用 Flyway，需先解决重复版本号问题，本脚本已做幂等（WHERE NOT EXISTS）可安全重放。
--
-- 幂等：每条用 WHERE NOT EXISTS 防重复插入。

-- 公告管理 → 运营管理(id=300, code=OPERATION) 下，sort=4
INSERT INTO `sys_menu` (`id`, `code`, `parent_id`, `name`, `type`, `menu_group`, `path`, `sort`, `status`)
SELECT 303, 'ANNOUNCEMENT', 300, '公告管理', 'PAGE', 'WEB', '/operation/announcement', 4, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'ANNOUNCEMENT');

-- 小程序隐私政策 → 系统管理(id=500, code=SYSTEM) 下，sort=5
INSERT INTO `sys_menu` (`id`, `code`, `parent_id`, `name`, `type`, `menu_group`, `path`, `sort`, `status`)
SELECT 505, 'PRIVACY_POLICY', 500, '小程序隐私政策', 'PAGE', 'WEB', '/system/privacy-policy', 5, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `code` = 'PRIVACY_POLICY');

-- 绑定到 role#1（有角色的用户 getUserTree 仅返回已授权菜单，不绑则看不到）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 303
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 303);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 505
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 505);
