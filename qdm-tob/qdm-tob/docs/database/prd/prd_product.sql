-- 商品资料主表（优化版 - product_id 使用自增）
CREATE TABLE prd_product
(
    -- 主键：使用自增 BIGINT，性能最优
    product_id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID（自增主键）',
    -- 业务编码：独立商品编号，用于对外展示和外部系统对接
    barcode          VARCHAR(32)     NOT NULL COMMENT '商品编码（SAP业务系统编号）',
    `name`           VARCHAR(128)    NOT NULL COMMENT '商品名称',
    -- 分类ID：与 prd_category.id 类型保持一致（VARCHAR(6)）
    category_id      VARCHAR(6)      NOT NULL COMMENT '小类编号（关联 prd_category.id）',
    spu              VARCHAR(40)     NULL COMMENT '商品SPU编码',
    spec             VARCHAR(50)     NULL COMMENT '规格',
    origin_place     VARCHAR(50)     NULL COMMENT '产地',
    brand            VARCHAR(50)     NULL COMMENT '品牌',
    -- 单位字段
    unit1            VARCHAR(10)     NOT NULL COMMENT '订购单位',
    unit2            VARCHAR(10)     NOT NULL COMMENT '结算单位',
    unit_weight      DECIMAL(10, 3)  NULL COMMENT '单位重量（kg）',
    quality_days     SMALLINT UNSIGNED NULL COMMENT '保质天数',
    season_factor    VARCHAR(20)     NULL COMMENT '季节因子',
    storage_req      VARCHAR(200)    NULL COMMENT '储存要求',
    remark           VARCHAR(500)    NULL COMMENT '备注',
    -- 状态：与分类表 status 风格统一
    `status`         ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '商品状态：ACTIVE-启用 INACTIVE-停用 DELETED-删除',
    -- 审计字段
    created_by       VARCHAR(50)     NULL COMMENT '创建人',
    updated_by       VARCHAR(50)     NULL COMMENT '修改人',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    -- 主键
    PRIMARY KEY (product_id),
    -- 唯一索引：业务编码必须唯一
    UNIQUE INDEX uk_barcode (barcode),
    -- 普通索引：分类ID和状态是高频查询条件
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品资料主表';

-- ============================================
-- 商品种子数据
-- ============================================

-- 蔬菜类（叶菜/根茎/茄果）
INSERT INTO prd_product (barcode, `name`, category_id, spec, origin_place, brand, unit1, unit2, unit_weight, quality_days, season_factor, storage_req, remark, status, created_by, updated_by) VALUES
('PRD000001', '有机大白菜',       '010001', '500g/棵',     '山东寿光', '寿光有机',   '斤', '斤', 0.500, 7,  '秋冬', '0-4℃冷藏',     '有机认证，精品包装',                'ACTIVE', 'admin', 'admin'),
('PRD000002', '精品土豆',         '010002', '2.5kg/袋',    '甘肃定西', '定西土豆',   '袋', '袋', 2.500, 30, '全年', '阴凉干燥处',     '淀粉含量高，适合炖煮',               'ACTIVE', 'admin', 'admin'),
('PRD000003', '樱桃番茄',         '010003', '250g/盒',     '云南昆明', '春城优选',   '盒', '盒', 0.250, 5,  '春夏', '0-4℃冷藏',     '温室种植，酸甜可口',                 'ACTIVE', 'admin', 'admin'),
('PRD000004', '有机菠菜',         '010001', '300g/袋',     '北京大兴', '京郊有机',   '袋', '袋', 0.300, 3,  '春秋', '0-4℃冷藏',     '当日采摘，叶片嫩绿',                 'ACTIVE', 'admin', 'admin'),
('PRD000005', '铁棍山药',         '010002', '1kg/捆',      '河南焦作', '怀山堂',     '捆', '捆', 1.000, 60, '秋冬', '阴凉通风处',     '铁棍山药，肉质细腻',                'ACTIVE', 'admin', 'admin'),

-- 水果类
('PRD000006', '丹东99草莓',       '020001', '300g/盒',     '辽宁丹东', '丹东红颜',   '盒', '盒', 0.300, 3,  '冬春', '0-4℃冷藏',     '奶油草莓，甜度高',                   'ACTIVE', 'admin', 'admin'),
('PRD000007', '赣南脐橙',         '020002', '5kg/箱',      '江西赣州', '赣南优选',   '箱', '箱', 5.000, 30, '冬春', '常温阴凉处',     '脐橙大果，直径≥75mm',               'ACTIVE', 'admin', 'admin'),
('PRD000008', '智利车厘子',       '020001', '2.5kg/箱',    '智利',      '进口甄选',   '箱', '箱', 2.500, 7,  '冬季', '0-4℃冷藏',     'JJ级，果径28-30mm',                  'ACTIVE', 'admin', 'admin'),
('PRD000009', '海南芒果',         '020003', '2.5kg/箱',    '海南三亚', '热带风情',   '箱', '箱', 2.500, 7,  '春夏', '常温催熟',       '贵妃芒，单果200g+',                  'ACTIVE', 'admin', 'admin'),

-- 肉类
('PRD000010', '猪五花肉',         '030001', '500g/袋',     '河南南阳', '牧原食品',   '袋', '斤', 0.500, 7,  '全年', '-18℃冷冻',     '肥瘦相间，适合红烧',                'ACTIVE', 'admin', 'admin'),
('PRD000011', '鸡胸肉',           '030002', '1kg/袋',      '山东临沂', '新希望六和', '袋', '斤', 1.000, 180,'全年', '-18℃冷冻',     '去皮去骨，低脂高蛋白',              'ACTIVE', 'admin', 'admin'),
('PRD000012', '澳洲肥牛卷',       '030003', '250g/盒',     '澳大利亚', '恒都牛肉',   '盒', '盒', 0.250, 180,'秋冬', '-18℃冷冻',     '火锅专用，肉质鲜嫩',                'ACTIVE', 'admin', 'admin'),

-- 水产类
('PRD000013', '鲜活鲈鱼',         '040001', '500-750g/条', '广东珠海', '珠海渔丰',   '条', '斤', 0.600, 2,  '全年', '活鲜运输',       '当日捕捞，保证鲜活',                'ACTIVE', 'admin', 'admin'),
('PRD000014', '厄瓜多尔白虾',     '040002', '1.5kg/盒',    '厄瓜多尔', '大洋世家',   '盒', '盒', 1.500, 180,'全年', '-18℃冷冻',     '30/40规格，去虾线',                  'ACTIVE', 'admin', 'admin'),
('PRD000015', '鲍鱼',             '040003', '10头/袋',     '福建连江', '连江鲍业',   '袋', '袋', 0.500, 180,'全年', '-18℃冷冻',     '8-10头规格，个大肉厚',               'ACTIVE', 'admin', 'admin'),

-- 乳制品
('PRD000016', '巴氏鲜牛奶',       '050001', '950ml/瓶',    '北京延庆', '三元乳业',   '瓶', '瓶', 0.950, 7,  '全年', '2-6℃冷藏',     '巴氏杀菌，72小时鲜',                'ACTIVE', 'admin', 'admin'),
('PRD000017', '原味酸奶',         '050002', '100g×8杯/组', '内蒙古',    '伊利',       '组', '组', 0.800, 21, '全年', '2-6℃冷藏',     '益生菌发酵，口感醇厚',              'ACTIVE', 'admin', 'admin'),

-- 调味品
('PRD000018', '特级生抽',         '060001', '500ml/瓶',    '广东佛山', '海天味业',   '瓶', '瓶', 0.600, 365,'全年', '常温避光',       '酿造酱油，氨基酸态氮≥0.8g/100ml',   'ACTIVE', 'admin', 'admin'),
('PRD000019', '料酒',             '060002', '500ml/瓶',    '浙江绍兴', '古越龙山',   '瓶', '瓶', 0.550, 365,'全年', '常温避光',       '黄酒基料，去腥增香',                'ACTIVE', 'admin', 'admin'),
('PRD000020', '食用盐（精制）',   '060003', '400g/袋',     '山东潍坊', '鲁盐集团',   '袋', '袋', 0.400, 730,'全年', '常温干燥处',     '精制碘盐，符合国标GB 2721',         'ACTIVE', 'admin', 'admin'),

-- 停用商品（示例）
('PRD000021', '速冻水饺',         '070001', '500g/袋',     '河南郑州', '思念食品',   '袋', '袋', 0.500, 180,'全年', '-18℃冷冻',     '猪肉大葱馅，因销量低已停用',        'INACTIVE', 'admin', 'admin');
