-- ============================================================
-- 迁移: vip_level / package_type / payment_method 改为用户私有
-- 执行前请确保旧数据已清理或无需保留
-- ============================================================

-- 1. vip_level 加列 + 改唯一索引
ALTER TABLE vip_level ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID' AFTER `benefits`;
ALTER TABLE vip_level DROP INDEX uk_level;
ALTER TABLE vip_level ADD UNIQUE KEY uk_user_level (`user_id`, `level`);

-- 2. package_type 加列
ALTER TABLE package_type ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID' AFTER `status`;
CREATE INDEX idx_user_id ON package_type (user_id);

-- 3. payment_method 加列
ALTER TABLE payment_method ADD COLUMN user_id BIGINT NOT NULL DEFAULT 0 COMMENT '所属用户ID' AFTER `status`;
CREATE INDEX idx_user_id ON payment_method (user_id);
