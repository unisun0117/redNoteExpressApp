-- V13: 移除 prd_price_detail 表中的 price_group_name 冗余列
-- 该字段是 prd_price_group.price_group_name 的快照，改为应用层联查获取

ALTER TABLE prd_price_detail DROP COLUMN IF EXISTS price_group_name;
