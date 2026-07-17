# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

陪玩星助手 (XinPA) — 陪玩个人助理 Web 版，前后端分离的全栈管理平台。帮助陪玩从业者管理订单、客户、财务、素材、预约等日常事务。

## 技术栈

- **后端**: Spring Boot 3.2.5 + Java 17 + Maven
  - Spring Security (JWT 无状态认证)
  - MyBatis-Plus 3.5.5 (ORM)
  - MySQL、Redis、RabbitMQ、MinIO
  - Lombok
- **前端**: Vue 3 + TypeScript + Vite 8
  - Element Plus (UI 组件库)
  - Pinia (状态管理)、Vue Router
  - ECharts + vue-echarts (图表)
  - Axios (HTTP 客户端)、Day.js

## 开发命令

### 后端

```bash
# 启动后端服务 (默认端口 8080, context-path: /api)
cd backend && mvn spring-boot:run

# 打包
cd backend && mvn clean package -DskipTests

# 运行所有测试
cd backend && mvn test

# 运行单个测试
cd backend && mvn test -Dtest=CustomerServiceImplTest

# 安装依赖 (初次使用)
cd backend && mvn dependency:resolve
```

### 前端

```bash
# 启动开发服务器 (默认端口 3000, 代理 /api -> localhost:8080)
cd frontend && npm run dev

# 构建生产版本
cd frontend && npm run build

# 预览构建产物
cd frontend && npm run preview

# 安装依赖
cd frontend && npm install
```

## 项目结构

### 后端 (`backend/`)

标准 Spring Boot 分层架构：

```
src/main/java/com/xinpa/
├── XinpaApplication.java          # 启动类 (@MapperScan + @EnableScheduling)
├── config/                         # 配置类
│   ├── CorsConfig.java             # 跨域配置
│   ├── DataInitRunner.java         # 启动数据初始化
│   ├── JwtProperties.java          # JWT 配置属性
│   ├── MinioProperties.java        # MinIO 配置属性
│   ├── MybatisPlusConfig.java      # MyBatis-Plus 分页插件
│   ├── RabbitConfig.java           # RabbitMQ 交换机/队列定义
│   ├── RedisConfig.java            # Redis 序列化配置
│   └── WebMvcConfig.java           # 静态资源映射 (上传文件)
├── security/                       # 认证安全层
│   ├── JwtUtils.java               # JWT 生成/解析/校验
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter, 解析 Bearer token
│   └── SecurityConfig.java         # Spring Security 安全规则
├── controller/                     # REST 控制器 (用户端)
│   ├── AuthController.java         # 用户登录/注册
│   ├── OrderController.java        # 订单 CRUD
│   ├── CustomerController.java     # 客户管理
│   ├── FinanceController.java      # 财务记账
│   ├── ProfileController.java      # 人设主页
│   ├── DashboardController.java    # 工作台
│   ├── AnnouncementController.java # 公告
│   ├── OrderSourceController.java  # 来源管理
│   ├── PaymentMethodController.java # 支付方式
│   ├── ToolsController.java        # 辅助工具
│   └── admin/                      # 管理员控制器
│       ├── AdminAuthController.java
│       ├── AdminStatsController.java
│       └── AdminSystemController.java
├── service/                        # 业务接口 + impl/ 实现
├── mapper/                         # MyBatis-Plus Mapper 接口
├── entity/                         # 数据库实体 (21个)
├── dto/                            # 数据传输对象
├── vo/                             # 视图对象
├── common/                         # 公共工具
│   ├── Result.java                 # 统一返回体 {code, message, data}
│   ├── PageResult.java             # 分页返回体
│   ├── BusinessException.java      # 业务异常
│   ├── GlobalExceptionHandler.java # 全局异常处理 (@RestControllerAdvice)
│   └── UserContext.java            # 当前用户上下文 (ThreadLocal)
├── handler/
│   └── MyMetaObjectHandler.java    # 自动填充 createTime/updateTime
├── mq/
│   ├── AppointmentConsumer.java    # 预约提醒 MQ 消费者
│   └── FollowUpConsumer.java      # 回访提醒 MQ 消费者
├── schedule/
│   └── AppointmentRemindTask.java  # 定时推送预约提醒
└── util/                           # 工具类
```

### 前端 (`frontend/`)

```
src/
├── main.ts                         # 入口
├── App.vue
├── style.css                       # 全局样式
├── router/index.ts                 # 路由定义 + 路由守卫
├── store/user.ts                   # Pinia 用户状态
├── utils/request.ts                # Axios 实例 + 拦截器
├── api/                            # 各模块 API 封装
│   ├── auth.ts, admin.ts, orders.ts, customers.ts,
│   ├── finance.ts, dashboard.ts, profile.ts,
│   ├── tools.ts, orderSource.ts, paymentMethod.ts
├── views/                          # 页面组件
│   ├── user/                       # 用户端页面
│   │   ├── Login.vue, Register.vue, Dashboard.vue
│   │   ├── orders/                 # 订单管理、详情、预约日历
│   │   ├── customers/              # 客户列表、详情
│   │   ├── finance/                # 财务记账、报表
│   │   ├── profile/                # 人设主页
│   │   ├── packages/               # 价目套餐
│   │   ├── materials/              # 素材库
│   │   ├── settings/               # 个人中心
│   │   └── tools/                  # 待办事项
│   ├── admin/                      # 管理员页面
│   │   ├── Login.vue
│   │   ├── dashboard/              # 平台总览、数据统计
│   │   └── system/                 # 管理/公告/来源/支付/配置
│   └── showcase/                   # 陪玩展示主页
├── layouts/                        # 布局 (UserLayout, AdminLayout)
├── components/                     # 通用组件
└── types/                          # TypeScript 类型定义
```

## 核心架构要点

### 认证机制
- JWT 无状态认证，通过 `JwtAuthenticationFilter` 从 `Authorization: Bearer <token>` 提取
- 用户端和管理员共用同一套认证体系，userType 区分 `USER` / `ADMIN`
- 路由守卫区分用户端 (`/dashboard` 等) 和管理员 (`/admin/*`) 路径

### API 规范
- 统一前缀 `/api` (通过 server.context-path 配置)
- 统一返回体 `Result<T>`: `{code: 200, message: "success", data: ...}`
- 异常通过 `GlobalExceptionHandler` 统一处理
- 分页接口使用 `PageResult`

### 安全规则
- `/auth/**` 和 `/admin/auth/**` 无需认证
- `/admin/**` 需要 `ROLE_ADMIN` 角色
- 其余所有接口需要认证

### 数据库
- MySQL (端口 3307, 数据库名 xinpa)
- 所有表使用逻辑删除 (`deleted` 字段, 0=未删, 1=已删)
- 主键策略: 自增
- 初始化 SQL: `sql/init.sql`

### 消息队列 (RabbitMQ)
- 预约提醒交换机: `xinpa.appointment` → 队列 `xinpa.appointment.queue`
- 回访提醒交换机: `xinpa.followup` → 队列 `xinpa.followup.queue`

### 文件存储
- 使用 MinIO (本地 `localhost:9000`, bucket: `xinpa`)
- 上传目录: `uploads/avatars`，静态资源映射 `/uploads/**`

### 环境依赖
- MySQL (端口 3307)、Redis (端口 6379)、RabbitMQ (端口 5672)、MinIO (端口 9000)
- 前端开发服务器 (端口 3000) 通过 Vite proxy 将 `/api` 转发到后端 (端口 8080)
- JWT 密钥/过期时间、MinIO 密钥均在 `application.yml` 中配置

<!-- ===================================================================== -->
<!-- 以下为 Karpathy Guidelines，由 skills/karpathy-guidelines 生成         -->
<!-- ===================================================================== -->

# Karpathy Guidelines

Behavioral guidelines to reduce common LLM coding mistakes, derived from [Andrej Karpathy's observations](https://x.com/karpathy/status/2015883857489522876) on LLM coding pitfalls.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.
