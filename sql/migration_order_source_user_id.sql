-- ============================================================
-- 迁移: order_source 改为用户私有（去掉全局来源概念）
-- 此脚本将已有全局来源按实际引用情况复制给每个用户，
-- 并更新 order / customer 中的引用指向新副本。
-- ============================================================

-- 1. 加列（允许 NULL，便于迁移过程中兼容旧数据）
ALTER TABLE order_source
    ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '所属用户ID' AFTER `status`;

-- 2. 从 order 表查出每个用户实际引用过的来源，复制一份归该用户
INSERT INTO order_source (name, sort_order, status, user_id, created_at)
SELECT DISTINCT os.name, os.sort_order, os.status, o.user_id, NOW()
FROM order_source os
INNER JOIN `order` o ON o.order_source = os.id
WHERE os.deleted = 0 AND o.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM order_source dup
    WHERE dup.user_id = o.user_id AND dup.name = os.name AND dup.deleted = 0
  );

-- 3. 从 customer 表补漏（有些用户有客户但可能没有引用该来源的订单）
INSERT INTO order_source (name, sort_order, status, user_id, created_at)
SELECT DISTINCT os.name, os.sort_order, os.status, c.user_id, NOW()
FROM order_source os
INNER JOIN customer c ON c.source_id = os.id
WHERE os.deleted = 0 AND c.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM order_source dup
    WHERE dup.user_id = c.user_id AND dup.name = os.name AND dup.deleted = 0
  );

-- 4. 更新 order 引用指向新的副本
UPDATE `order` o
INNER JOIN order_source old_os ON o.order_source = old_os.id AND old_os.deleted = 0
INNER JOIN order_source new_os ON new_os.name = old_os.name
    AND new_os.user_id = o.user_id AND new_os.deleted = 0
SET o.order_source = new_os.id
WHERE o.deleted = 0 AND old_os.user_id IS NULL;

-- 5. 更新 customer 引用指向新的副本
UPDATE customer c
INNER JOIN order_source old_os ON c.source_id = old_os.id AND old_os.deleted = 0
INNER JOIN order_source new_os ON new_os.name = old_os.name
    AND new_os.user_id = c.user_id AND new_os.deleted = 0
SET c.source_id = new_os.id
WHERE c.deleted = 0 AND old_os.user_id IS NULL;

-- 6. 清理全局旧记录
DELETE FROM order_source WHERE user_id IS NULL;

-- 7. 加 NOT NULL 约束 + 索引
ALTER TABLE order_source MODIFY user_id BIGINT NOT NULL COMMENT '所属用户ID';
CREATE INDEX idx_user_id ON order_source (user_id);
