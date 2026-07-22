-- ============================================================
-- 陪玩星助手 数据库初始化脚本
-- MySQL 8.0+
-- 用法: mysql -h 127.0.0.1 -P 3307 -u root -proot < init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS xinpa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xinpa;

-- ==================== 用户与权限 ====================

CREATE TABLE sys_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    password        VARCHAR(128) DEFAULT NULL COMMENT 'BCrypt加密密码（选填）',
    nickname        VARCHAR(64)  DEFAULT NULL COMMENT '昵称/艺名',
    avatar          VARCHAR(512) DEFAULT NULL COMMENT '头像URL / objectKey',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    member_type     TINYINT      NOT NULL DEFAULT 0 COMMENT '会员类型: 0免费 1付费',
    member_expire   DATETIME     DEFAULT NULL COMMENT '会员到期时间',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='陪玩用户表';

CREATE TABLE sys_admin (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(64)  NOT NULL UNIQUE COMMENT '管理员用户名',
    password        VARCHAR(128) NOT NULL COMMENT 'BCrypt加密密码',
    real_name       VARCHAR(64)  DEFAULT NULL COMMENT '真实姓名',
    role            VARCHAR(32)  NOT NULL DEFAULT 'ADMIN' COMMENT '角色: SUPER_ADMIN/ADMIN',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
    last_login_time DATETIME     DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

CREATE TABLE sys_announcement (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(256) NOT NULL COMMENT '公告标题',
    content     TEXT         NOT NULL COMMENT '公告内容',
    admin_id    BIGINT       NOT NULL COMMENT '发布管理员ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0下线 1发布',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全平台公告';

CREATE TABLE price_package (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    name            VARCHAR(128) NOT NULL COMMENT '套餐名称',
    package_type    TINYINT      NOT NULL COMMENT '类型: 1小时单 2包天 3通宵 4包周 5包月',
    price           DECIMAL(10,2) NOT NULL COMMENT '单价(元)',
    unit            VARCHAR(32)  DEFAULT '小时' COMMENT '计价单位',
    description     VARCHAR(512) DEFAULT NULL COMMENT '描述',
    discount_rules  JSON         COMMENT '阶梯折扣规则 [{minHours,discount}]',
    sort_order      INT          DEFAULT 0,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价目套餐';

-- ==================== 素材库 ====================

CREATE TABLE material (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    name            VARCHAR(256) NOT NULL COMMENT '素材名称',
    material_type   TINYINT      NOT NULL COMMENT '类型: 1图片 2音频 3视频',
    file_url        VARCHAR(512) NOT NULL COMMENT '文件URL/objectKey',
    file_size       BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
    watermark       TINYINT      DEFAULT 0 COMMENT '是否添加防盗水印',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材库';

-- ==================== 客户管理 ====================

CREATE TABLE customer (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '所属陪玩用户ID',
    nickname        VARCHAR(128) NOT NULL COMMENT '客户昵称',
    contact         VARCHAR(128) DEFAULT NULL COMMENT '微信/QQ等联系方式',
    source          VARCHAR(64)  DEFAULT NULL COMMENT '来源渠道(旧字段)',
    source_id       BIGINT       DEFAULT NULL COMMENT '关联order_source.id',
    spend_level     TINYINT      DEFAULT 0 COMMENT '优惠等级: 0无 1VIP1 2VIP2 ...',
    game_preference JSON         COMMENT '游戏偏好',
    personality     VARCHAR(512) DEFAULT NULL COMMENT '性格/雷点备注',
    birthday        DATE         DEFAULT NULL COMMENT '生日',
    tags            JSON         COMMENT '标签: 新客/包月熟客/大客户等',
    is_blacklist    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否黑名单',
    blacklist_reason VARCHAR(512) DEFAULT NULL COMMENT '拉黑原因',
    last_order_time DATETIME     DEFAULT NULL COMMENT '最后下单时间',
    total_spend     DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费',
    order_count     INT          DEFAULT 0 COMMENT '下单次数',
    remark          TEXT         COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_blacklist (user_id, is_blacklist)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户档案';

-- ==================== 订单管理 ====================

CREATE TABLE `order` (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '陪玩用户ID',
    customer_id     BIGINT       DEFAULT NULL COMMENT '客户ID',
    order_no        VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单编号',
    order_source    BIGINT       DEFAULT NULL COMMENT '来源: 引用order_source.id',
    package_id      BIGINT       DEFAULT NULL COMMENT '关联套餐ID',
    package_name    VARCHAR(256) DEFAULT NULL COMMENT '套餐名称',
    title           VARCHAR(256) NOT NULL COMMENT '订单标题',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1待接单 2进行中 3待结算 4已完结 5售后退款',
    unit_price      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
    unit            VARCHAR(16)  DEFAULT '小时' COMMENT '计价单位（小时、次、晚等）',
    billed_minutes  INT          DEFAULT 0 COMMENT '计费时长(分钟)',
    actual_minutes  INT          DEFAULT 0 COMMENT '实际计时(分钟)',
    extra_minutes   INT          DEFAULT 0 COMMENT '手动补时(分钟)',
    total_amount    DECIMAL(10,2) DEFAULT 0 COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    final_amount    DECIMAL(10,2) DEFAULT 0 COMMENT '实付金额',
    payment_method  VARCHAR(32)  DEFAULT NULL COMMENT '支付方式(旧字段)',
    payment_method_id BIGINT     DEFAULT NULL COMMENT '关联payment_method.id',
    settle_ratio    DECIMAL(5,2)  DEFAULT 100.00 COMMENT '到手比例(100=100%)',
    is_overnight    TINYINT      DEFAULT 0 COMMENT '是否通宵单',
    is_offline      TINYINT      DEFAULT 0 COMMENT '是否线下单',
    start_time      DATETIME     DEFAULT NULL COMMENT '开始计时',
    end_time        DATETIME     DEFAULT NULL COMMENT '结束计时',
    settle_time     DATETIME     DEFAULT NULL COMMENT '结算时间',
    appointment_time DATETIME    DEFAULT NULL COMMENT '预约时间',
    remark          TEXT         COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (user_id, status),
    INDEX idx_appointment (user_id, appointment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE order_timer_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id    BIGINT       NOT NULL COMMENT '订单ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    action      TINYINT      NOT NULL COMMENT '操作: 1开始 2暂停 3结算',
    minutes     INT          DEFAULT 0 COMMENT '耗时(分钟)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单计时日志';

-- ==================== 财务管理 ====================

CREATE TABLE finance_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    order_id        BIGINT       DEFAULT NULL COMMENT '关联订单ID',
    record_type     TINYINT      NOT NULL COMMENT '类型: 1收入 2支出',
    category        VARCHAR(64)  NOT NULL COMMENT '分类',
    amount          DECIMAL(12,2) NOT NULL COMMENT '金额',
    payment_method  VARCHAR(32)  DEFAULT NULL COMMENT '支付方式: 平台/微信/支付宝/现金',
    platform_fee    DECIMAL(10,2) DEFAULT 0 COMMENT '平台抽成',
    record_date     DATE         NOT NULL COMMENT '记账日期',
    remark          VARCHAR(512) DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_type (user_id, record_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='财务流水';

CREATE TABLE user_finance_setting (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id               BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    monthly_target         DECIMAL(12,2) DEFAULT 0 COMMENT '月度收入目标',
    monthly_expense_target DECIMAL(12,2) DEFAULT 0 COMMENT '月度支出预算',
    withdraw_fee_rate      DECIMAL(5,4) DEFAULT 0.006 COMMENT '提现手续费率',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户财务设置';

-- ==================== 日程工具 ====================

CREATE TABLE todo_item (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    title       VARCHAR(256) NOT NULL COMMENT '待办标题',
    todo_type   TINYINT      NOT NULL COMMENT '类型: 1素材更新 2平台签到 3自定义',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待办 1完成',
    due_date    DATE         DEFAULT NULL COMMENT '截止日期',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办看板';

CREATE TABLE copywriting (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    category        VARCHAR(64)  NOT NULL COMMENT '分类',
    title           VARCHAR(256) DEFAULT NULL COMMENT '标题',
    content         TEXT         NOT NULL COMMENT '文案内容',
    is_ai_generated TINYINT      DEFAULT 0 COMMENT '是否AI生成',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文案库';

CREATE TABLE quick_phrase (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    phrase      VARCHAR(512) NOT NULL COMMENT '快捷语',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快捷语';

-- ==================== 系统配置 ====================

CREATE TABLE sys_config (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key  VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT        NOT NULL COMMENT '配置值',
    description VARCHAR(256) DEFAULT NULL COMMENT '描述',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置';

-- ==================== 字典表 ====================

CREATE TABLE order_source (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64) NOT NULL COMMENT '来源名称',
    sort_order  INT         DEFAULT 0,
    status      TINYINT     DEFAULT 1 COMMENT '0禁用 1启用',
    user_id     BIGINT      NOT NULL COMMENT '所属用户ID',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT     DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单来源字典';

CREATE TABLE payment_method (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64) NOT NULL COMMENT '支付方式名称',
    sort_order  INT         DEFAULT 0,
    status      TINYINT     DEFAULT 1 COMMENT '0禁用 1启用',
    user_id     BIGINT      NOT NULL COMMENT '所属用户ID',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT     DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付方式（用户私有）';

CREATE TABLE package_type (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(64) NOT NULL COMMENT '类型名称',
    sort_order  INT         NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    user_id     BIGINT      NOT NULL COMMENT '所属用户ID',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐类型（用户私有）';

CREATE TABLE vip_level (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `level`     INT          NOT NULL COMMENT '等级数字 1-6',
    name        VARCHAR(64)  NOT NULL COMMENT '等级名称 如 VIP1',
    threshold   DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '消费门槛(元)',
    discount    INT          NOT NULL DEFAULT 100 COMMENT '折扣 如98=98折',
    benefits    VARCHAR(500) DEFAULT NULL COMMENT '等级福利描述',
    user_id     BIGINT       NOT NULL COMMENT '所属用户ID',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    status      TINYINT      DEFAULT 1 COMMENT '0禁用 1启用',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_user_level (`user_id`, `level`),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP等级配置（用户私有）';

-- ==================== 初始化数据 ====================

-- 默认超级管理员 (密码: admin123)
INSERT IGNORE INTO sys_admin (username, password, real_name, role, status) VALUES
('admin', '$2a$10$1up6pBzXc0fKSQ3MddzMve9ptQ8dJNVNpCVbnuRp4/SkWbDNj.fBq', '超级管理员', 'SUPER_ADMIN', 1);

-- 演示陪玩用户 (密码: user123, 手机号: 13800138000)
INSERT IGNORE INTO sys_user (password, nickname, phone, member_type, status) VALUES
('$2a$10$SK6EByab0ZyUDsvzbLnHGuly45/6LCygYpwF3MO.q9r8AxwPU8SJC', '演示陪玩', '13800138000', 0, 1);

INSERT IGNORE INTO user_finance_setting (user_id, monthly_target, monthly_expense_target) VALUES (1, 10000.00, 0.00);

-- 支付方式、套餐类型、VIP等级现在由用户注册时自动初始化（参见 SysUserServiceImpl.initNewUser）
-- 不再需要全局默认值
