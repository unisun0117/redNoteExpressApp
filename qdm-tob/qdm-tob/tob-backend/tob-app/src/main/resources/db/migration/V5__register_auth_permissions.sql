-- V5: 注册小程序认证端点到 sys_api_permission（遵循 RULES.md 权限注册）
-- 注：/api/mall/auth/** 在 SecurityConfiguration 中为 permitAll（公开），此处注册仅做契约留痕，
-- DynamicAuthorizationManager 仅对 /api/admin/** 做动态 RBAC 校验，不拦截 mall 公开端点。
-- 幂等：每条用 WHERE NOT EXISTS 防重复插入。

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/mall/auth/sms/request', 'mall:auth:sms:request', '小程序-发送短信验证码', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/mall/auth/sms/request');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/mall/auth/sms/verify', 'mall:auth:sms:verify', '小程序-短信验证码登录', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/mall/auth/sms/verify');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/mall/auth/register', 'mall:auth:register', '小程序-注册（姓名+手机号+验证码）', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/mall/auth/register');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/mall/auth/wechat/login', 'mall:auth:wechat:login', '小程序-微信一键登录', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/mall/auth/wechat/login');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/mall/auth/logout', 'mall:auth:logout', '小程序-退出登录', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/mall/auth/logout');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/mall/auth/token/refresh', 'mall:auth:token:refresh', '小程序-刷新Token', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/mall/auth/token/refresh');
