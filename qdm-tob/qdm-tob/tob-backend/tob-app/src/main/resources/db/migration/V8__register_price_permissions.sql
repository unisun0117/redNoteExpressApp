-- V8: 注册商品价格管理 API 权限映射到 sys_api_permission
-- 幂等：每条用 WHERE NOT EXISTS 防重复插入
-- permission_code 前缀统一为 prd: 与 Permissions 枚举一致

-- 价格组
INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/price-group/list', 'prd:price-group:list', '价格组-分页查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/price-group/list');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/product/price-group', 'prd:price-group:create', '价格组-新增', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/product/price-group');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'PUT', '/api/admin/product/price-group', 'prd:price-group:update', '价格组-编辑', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='PUT' AND `url_pattern`='/api/admin/product/price-group');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/price-group/options', 'prd:price-group:options', '价格组-下拉选项', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/price-group/options');

-- 价格组明细
INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/price-detail/list', 'prd:price-detail:list', '价格组明细-分页查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/price-detail/list');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/price-detail/detail', 'prd:price-detail:detail', '价格组明细-详情查询', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/price-detail/detail');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/product/price-detail', 'prd:price-detail:create', '价格组明细-新增', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/product/price-detail');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'PUT', '/api/admin/product/price-detail', 'prd:price-detail:update', '价格组明细-编辑', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='PUT' AND `url_pattern`='/api/admin/product/price-detail');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/price-detail/lookup-barcode', 'prd:price-detail:lookup', '价格组明细-条码反查', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/price-detail/lookup-barcode');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'GET', '/api/admin/product/price-detail/export', 'prd:price-detail:export', '价格组明细-导出', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='GET' AND `url_pattern`='/api/admin/product/price-detail/export');

INSERT INTO `sys_api_permission` (`http_method`, `url_pattern`, `permission_code`, `description`, `status`)
SELECT 'POST', '/api/admin/product/price-detail/import', 'prd:price-detail:import', '价格组明细-导入', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM `sys_api_permission` WHERE `http_method`='POST' AND `url_pattern`='/api/admin/product/price-detail/import');
