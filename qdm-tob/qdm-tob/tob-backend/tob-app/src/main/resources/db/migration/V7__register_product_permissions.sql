-- V7: 注册商品资料管理 API 权限映射到 sys_api_permission
-- DynamicAuthorizationManager 仅对 /api/admin/** 做动态 RBAC 校验
-- 幂等：每条用 WHERE NOT EXISTS 防重复插入

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/catalog/list', 'product:catalog:list', '商品资料-分页查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/catalog/list');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/catalog/detail', 'product:catalog:detail', '商品资料-详情查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/catalog/detail');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/product/catalog', 'product:catalog:create', '商品资料-新增', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/product/catalog');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'PUT', '/api/admin/product/catalog', 'product:catalog:update', '商品资料-编辑', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='PUT' AND `url_pattern`='/api/admin/product/catalog');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'PUT', '/api/admin/product/catalog/adjust-stock', 'product:catalog:adjust-stock', '商品资料-库存调整', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='PUT' AND `url_pattern`='/api/admin/product/catalog/adjust-stock');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/catalog/export', 'product:catalog:export', '商品资料-导出', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/catalog/export');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/product/catalog/import', 'product:catalog:import', '商品资料-导入', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/product/catalog/import');
