-- ============================================================
-- 迁移：客户表新增 first_order_time 字段（用于陪伴天数计算）
-- ============================================================

ALTER TABLE customer ADD COLUMN first_order_time DATETIME DEFAULT NULL COMMENT '首次下单时间' AFTER last_order_time;

-- 为已有订单的客户回填首次下单时间
UPDATE customer c
SET first_order_time = (
    SELECT MIN(start_time) FROM `order` WHERE customer_id = c.id AND deleted = 0
)
WHERE EXISTS (SELECT 1 FROM `order` WHERE customer_id = c.id AND deleted = 0);
