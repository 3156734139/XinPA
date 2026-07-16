-- 陪玩星助手 数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS xinpa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xinpa;

-- ==================== 用户与权限 ====================

-- 陪玩用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录用户名',
    password        VARCHAR(128) NOT NULL COMMENT 'BCrypt加密密码',
    nickname        VARCHAR(64)  DEFAULT NULL COMMENT '昵称/艺名',
    avatar          VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    member_type     TINYINT      NOT NULL DEFAULT 0 COMMENT '会员类型: 0免费 1付费',
    member_expire   DATETIME     DEFAULT NULL COMMENT '会员到期时间',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='陪玩用户表';

-- 管理员表
CREATE TABLE IF NOT EXISTS sys_admin (
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
) ENGINE=InnoDB COMMENT='管理员表';

-- 全平台公告
CREATE TABLE IF NOT EXISTS sys_announcement (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(256) NOT NULL COMMENT '公告标题',
    content     TEXT         NOT NULL COMMENT '公告内容',
    admin_id    BIGINT       NOT NULL COMMENT '发布管理员ID',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '0下线 1发布',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='全平台公告';

-- AI调用日志
CREATE TABLE IF NOT EXISTS ai_call_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT '调用用户ID',
    scene       VARCHAR(64)  NOT NULL COMMENT '场景: intro/chat/risk/activity等',
    prompt      TEXT         COMMENT '请求内容摘要',
    response    TEXT         COMMENT '响应内容摘要',
    tokens      INT          DEFAULT 0 COMMENT '消耗token数',
    success     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否成功',
    error_msg   VARCHAR(512) DEFAULT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='AI调用日志';

-- ==================== 人设主页 ====================

-- 个人人设主页
CREATE TABLE IF NOT EXISTS user_profile (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    template_type   TINYINT      NOT NULL DEFAULT 1 COMMENT '模板: 1游戏陪玩 2声优树洞 3线下陪伴',
    intro           TEXT         COMMENT '主页简介',
    opening_line    VARCHAR(512) DEFAULT NULL COMMENT '私聊开场白',
    order_status    TINYINT      NOT NULL DEFAULT 1 COMMENT '接单状态: 1在线 2休息 3通宵 4仅熟客',
    games           JSON         COMMENT '游戏标签JSON数组',
    ranks           JSON         COMMENT '段位标签',
    positions       JSON         COMMENT '擅长位置',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='个人人设主页';

-- 价目套餐
CREATE TABLE IF NOT EXISTS price_package (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    name            VARCHAR(128) NOT NULL COMMENT '套餐名称',
    package_type    TINYINT      NOT NULL COMMENT '类型: 1小时单 2包夜 3教学 4包月 5线下',
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
) ENGINE=InnoDB COMMENT='价目套餐';

-- 素材库
CREATE TABLE IF NOT EXISTS material (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    name            VARCHAR(256) NOT NULL COMMENT '素材名称',
    material_type   TINYINT      NOT NULL COMMENT '类型: 1截图 2语音 3短视频',
    file_url        VARCHAR(512) NOT NULL COMMENT '文件URL',
    file_size       BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
    watermark       TINYINT      DEFAULT 0 COMMENT '是否添加防盗水印',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='素材库';

-- ==================== 客户管理 ====================

-- 客户档案
CREATE TABLE IF NOT EXISTS customer (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '所属陪玩用户ID',
    nickname        VARCHAR(128) NOT NULL COMMENT '客户昵称',
    contact         VARCHAR(128) DEFAULT NULL COMMENT '微信/QQ等联系方式',
    source          VARCHAR(64)  DEFAULT NULL COMMENT '来源渠道',
    spend_level     TINYINT      DEFAULT 1 COMMENT '消费水平: 1低 2中 3高',
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
) ENGINE=InnoDB COMMENT='客户档案';

-- 优惠券
CREATE TABLE IF NOT EXISTS coupon (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '所属陪玩用户ID',
    customer_id     BIGINT       DEFAULT NULL COMMENT '定向客户ID，NULL为通用',
    name            VARCHAR(128) NOT NULL COMMENT '券名称',
    coupon_type     TINYINT      NOT NULL COMMENT '类型: 1时长折扣 2免费体验',
    discount_value  DECIMAL(5,2) DEFAULT NULL COMMENT '折扣值(0.8=8折)',
    free_hours      DECIMAL(5,2) DEFAULT NULL COMMENT '免费时长(小时)',
    min_amount      DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费门槛',
    expire_time     DATETIME     DEFAULT NULL COMMENT '过期时间',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0已用 1未用 2过期',
    used_order_id   BIGINT       DEFAULT NULL COMMENT '使用的订单ID',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_customer (user_id, customer_id)
) ENGINE=InnoDB COMMENT='优惠券';

-- 回访提醒
CREATE TABLE IF NOT EXISTS follow_up_reminder (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '陪玩用户ID',
    customer_id     BIGINT       NOT NULL COMMENT '客户ID',
    order_id        BIGINT       DEFAULT NULL COMMENT '关联订单ID',
    remind_type     TINYINT      NOT NULL COMMENT '类型: 1下单3天 2下单7天 3生日 4版本更新',
    remind_time     DATETIME     NOT NULL COMMENT '提醒时间',
    ai_greeting     TEXT         COMMENT 'AI生成问候话术',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理 2已忽略',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_remind (user_id, remind_time)
) ENGINE=InnoDB COMMENT='回访提醒';

-- 快捷文案库
CREATE TABLE IF NOT EXISTS copywriting (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    category        VARCHAR(64)  NOT NULL COMMENT '分类: 开场/安抚/砍价/活动等',
    title           VARCHAR(128) NOT NULL COMMENT '标题',
    content         TEXT         NOT NULL COMMENT '文案内容',
    is_ai_generated TINYINT      DEFAULT 0 COMMENT '是否AI生成',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_category (user_id, category)
) ENGINE=InnoDB COMMENT='快捷文案库';

-- ==================== 订单管理 ====================

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '陪玩用户ID',
    customer_id     BIGINT       DEFAULT NULL COMMENT '客户ID',
    order_no        VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单编号',
    order_source    TINYINT      NOT NULL COMMENT '来源: 1平台派单 2微信QQ私域 3线下预约',
    package_id      BIGINT       DEFAULT NULL COMMENT '关联套餐ID',
    title           VARCHAR(256) NOT NULL COMMENT '订单标题',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1待接单 2进行中 3待结算 4已完结 5售后退款',
    unit_price      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '单价',
    planned_hours   DECIMAL(8,2) DEFAULT NULL COMMENT '计划时长(小时)',
    actual_minutes  INT          DEFAULT 0 COMMENT '实际计时(分钟)',
    extra_minutes   INT          DEFAULT 0 COMMENT '手动补时(分钟)',
    total_amount    DECIMAL(10,2) DEFAULT 0 COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    final_amount    DECIMAL(10,2) DEFAULT 0 COMMENT '实付金额',
    is_overnight    TINYINT      DEFAULT 0 COMMENT '是否通宵单',
    is_offline      TINYINT      DEFAULT 0 COMMENT '是否线下单',
    coupon_id       BIGINT       DEFAULT NULL COMMENT '使用的优惠券',
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
) ENGINE=InnoDB COMMENT='订单表';

-- 订单计时记录
CREATE TABLE IF NOT EXISTS order_timer_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id        BIGINT       NOT NULL COMMENT '订单ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    action          TINYINT      NOT NULL COMMENT '动作: 1开始 2暂停 3继续 4结束 5补时',
    minutes         INT          DEFAULT 0 COMMENT '本次计时分钟数',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB COMMENT='订单计时记录';

-- 预约日历
CREATE TABLE IF NOT EXISTS appointment (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    order_id        BIGINT       DEFAULT NULL COMMENT '关联订单ID',
    customer_id     BIGINT       DEFAULT NULL COMMENT '客户ID',
    title           VARCHAR(256) NOT NULL COMMENT '预约标题',
    start_time      DATETIME     NOT NULL COMMENT '开始时间',
    end_time        DATETIME     NOT NULL COMMENT '结束时间',
    is_overnight    TINYINT      DEFAULT 0 COMMENT '通宵标记',
    is_offline      TINYINT      DEFAULT 0 COMMENT '线下标记',
    color           VARCHAR(16)  DEFAULT '#409EFF' COMMENT '日历颜色',
    remark          VARCHAR(512) DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_time (user_id, start_time, end_time)
) ENGINE=InnoDB COMMENT='预约日历';

-- ==================== 财务管理 ====================

-- 财务流水
CREATE TABLE IF NOT EXISTS finance_record (
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
) ENGINE=InnoDB COMMENT='财务流水';

-- 用户财务设置
CREATE TABLE IF NOT EXISTS user_finance_setting (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    monthly_target  DECIMAL(12,2) DEFAULT 0 COMMENT '月度收入目标',
    withdraw_fee_rate DECIMAL(5,4) DEFAULT 0.006 COMMENT '提现手续费率',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='用户财务设置';

-- ==================== 日程工具 ====================

-- 快捷短语
CREATE TABLE IF NOT EXISTS quick_phrase (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    phrase          VARCHAR(512) NOT NULL COMMENT '短语内容',
    sort_order      INT          DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='快捷短语';

-- 待办看板
CREATE TABLE IF NOT EXISTS todo_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    title           VARCHAR(256) NOT NULL COMMENT '待办标题',
    todo_type       TINYINT      NOT NULL COMMENT '类型: 1素材更新 2平台签到 3自定义',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待办 1完成',
    due_date        DATE         DEFAULT NULL COMMENT '截止日期',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB COMMENT='待办看板';

-- ==================== 初始化数据 ====================

-- 默认超级管理员 (密码: admin123)
INSERT INTO sys_admin (username, password, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 'SUPER_ADMIN', 1);

-- 演示陪玩用户 (密码: user123)
INSERT INTO sys_user (username, password, nickname, member_type, status) VALUES
('demo', '$2a$10$8.UnVuG9HHgfftdD04OK0.zJx/qx3nYpBWn0BGB8oIvKOSSpjRqW.', '演示陪玩', 0, 1);

INSERT INTO user_profile (user_id, template_type, intro, order_status) VALUES
(1, 1, '专业游戏陪玩，多赛季王者，声音甜美~', 1);

INSERT INTO user_finance_setting (user_id, monthly_target) VALUES (1, 10000.00);

-- ==================== 2.0 新增: 游戏配置 ====================

CREATE TABLE IF NOT EXISTS user_game_config (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    game_name       VARCHAR(128) NOT NULL COMMENT '游戏名称',
    intro           TEXT         COMMENT '游戏介绍',
    opening_line    VARCHAR(512) DEFAULT NULL COMMENT '开场白',
    tags            VARCHAR(512) DEFAULT NULL COMMENT '标签逗号分隔',
    rank_info       VARCHAR(256) DEFAULT NULL COMMENT '段位',
    position_info   VARCHAR(256) DEFAULT NULL COMMENT '擅长位置',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='用户游戏配置';
