# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

陪玩星助手 (XinPA) — 陪玩个人助理 Web 版，前后端分离的全栈管理平台。帮助陪玩从业者管理订单、客户、财务、素材、预约等日常事务。内置 AI Agent 可通过自然语言操作业务数据。

## 技术栈

- **后端**: Spring Boot 3.2.5 + Java 17 + Maven
  - Spring Security (JWT 无状态认证) + IP 过滤 + 限流
  - MyBatis-Plus 3.5.5 (ORM)
  - MySQL (端口 3307)、Redis (端口 6379)、RabbitMQ (端口 5672)
  - MinIO (文件存储, 端口 9000, bucket: xinpa)
  - 阿里云 OSS / 一键登录 / STS
  - Lombok
- **前端**: Vue 3 + TypeScript 6 + Vite 8
  - Element Plus (UI 组件库)
  - Pinia (状态管理)、Vue Router
  - ECharts + vue-echarts (图表)
  - Axios (HTTP 客户端)、Day.js
- **AI Agent**: 兼容 OpenAI API 格式的 LLM，默认 Ollama + qwen2.5:7b 本地模型

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

# 构建生产版本 (type-check + build)
cd frontend && npm run build

# 预览构建产物
cd frontend && npm run preview

# 安装依赖
cd frontend && npm install
```

## 项目结构

### 后端 (`backend/`)

```
src/main/java/com/xinpa/
├── XinpaApplication.java          # 启动类 @MapperScan + @EnableScheduling
├── config/                         # 配置类 (CORS/JWT/MinIO/MyBatis-Plus/RabbitMQ/Redis/WebMvc)
├── security/                       # 认证安全层
│   ├── JwtUtils.java               # JWT 生成/解析/校验
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter, 解析 Bearer token
│   └── SecurityConfig.java         # 安全规则 + IP过滤/限流过滤器注册
├── filter/                         # 安全过滤器
│   ├── IpFilter.java               # IP 白名单/地理限制
│   └── RateLimitFilter.java        # 请求频率限制
├── controller/                     # REST 控制器
│   ├── AuthController.java         # 登录/注册/刷新 token
│   ├── OrderController.java        # 订单 CRUD
│   ├── CustomerController.java     # 客户管理
│   ├── FinanceController.java      # 财务记账
│   ├── MaterialController.java     # 素材库
│   ├── PackageController.java      # 价目套餐
│   ├── PackageTypeController.java  # 套餐类型
│   ├── DashboardController.java    # 工作台
│   ├── AnnouncementController.java # 公告
│   ├── OrderSourceController.java  # 来源管理
│   ├── PaymentMethodController.java # 支付方式
│   ├── TodoController.java         # 待办事项
│   └── admin/                      # 管理员控制器
│       ├── AdminAuthController.java
│       ├── AdminStatsController.java
│       └── AdminSystemController.java # 管理员/来源/支付/套餐类型/VIP/公告/系统配置 CRUD
├── service/                        # 20+ 业务接口 + impl/ 实现
│   ├── impl/
│   ├── StatsService.java           # 数据统计
│   ├── SmsService.java / SmsCodeService.java  # 短信
│   └── ... (Customer, Order, Finance, Material, PricePackage, 等)
├── mapper/                         # MyBatis-Plus Mapper 接口 (22个)
├── entity/                         # 数据库实体 (21个)
├── dto/                            # 数据传输对象 (LoginRequest, RegisterRequest, OrderQueryDTO, OrderTimerDTO)
├── vo/                             # 视图对象 (LoginVO, UserInfoVO)
├── common/                         # 公共工具
│   ├── Result.java                 # 统一返回体 {code, message, data}
│   ├── PageResult.java             # 分页返回体
│   ├── BusinessException.java      # 业务异常
│   ├── GlobalExceptionHandler.java # 统一异常处理 (业务/认证/参数校验/运行时)
│   └── UserContext.java            # 当前用户上下文 (ThreadLocal)
├── handler/
│   └── MyMetaObjectHandler.java    # 自动填充 createTime/updateTime
├── agent/                          # AI Agent 模块
│   ├── config/                     # AgentConfig(RestTemplate), LlmConfig(apiBaseUrl/apiKey/model)
│   ├── controller/AgentController.java  # /api/agent/chat, /confirm, /history
│   ├── service/AgentOrchestrator.java   # 对话编排 + Function Calling 循环 (最多10轮)
│   ├── service/LlmService.java          # 调用外部 LLM (兼容 OpenAI API 格式)
│   └── tools/                           # 15个 Function Calling 工具
│       ├── AgentTool.java (接口)
│       ├── CreateCustomerTool, CreateFinanceRecordTool, CreateOrderTool, CreateTodoTool
│       ├── QueryCustomerTool, QueryFinanceTool, QueryOrdersTool, QueryPackagesTool
│       ├── QuerySettingTool, QueryStatsTool, QueryTimeTool, QueryTodosTool, QueryVipConfigTool
│       └── UpdateCustomerTool, UpdateOrderTool, UpdateSettingTool
├── mq/
│   └── AppointmentConsumer.java    # 预约提醒 RabbitMQ 消费者
├── schedule/
│   └── AppointmentRemindTask.java  # 定时推送预约提醒
└── util/                           # 工具类 (SpringContextUtil, ChineseIpChecker, RateLimiter)
```

### 前端 (`frontend/`)

```
src/
├── main.ts                         # 入口
├── App.vue
├── style.css                       # 全局样式
├── router/index.ts                 # 路由定义 + 路由守卫 (用户端/管理员端分离)
├── store/user.ts                   # Pinia 用户状态 (token/refreshToken/userInfo/userType)
├── utils/request.ts                # Axios 实例 + 请求/响应拦截器 (含 token 刷新排队)
├── api/                            # 14 个 API 模块
│   ├── auth.ts, admin.ts, orders.ts, customers.ts,
│   ├── finance.ts, dashboard.ts, profile.ts,
│   ├── tools.ts, orderSource.ts, paymentMethod.ts,
│   ├── materials.ts, packages.ts, packageType.ts
│   └── agent.ts                    # AI Agent 聊天/历史/创建订单确认
├── views/
│   ├── user/                       # 用户端页面
│   │   ├── Login.vue, Register.vue, Dashboard.vue
│   │   ├── orders/                 # 订单列表、详情、预约日历
│   │   ├── customers/              # 客户列表、详情
│   │   ├── finance/                # 财务记账
│   │   ├── packages/               # 价目套餐
│   │   ├── materials/              # 素材库 (上传图片/音频/视频)
│   │   ├── agent/                  # AI 助理聊天界面
│   │   ├── settings/               # 个人中心
│   │   └── tools/                  # 待办事项
│   ├── admin/                      # 管理员页面
│   │   ├── Login.vue
│   │   ├── dashboard/              # 平台总览、数据统计
│   │   └── system/                 # 管理员/公告/来源/支付/套餐类型/VIP/系统配置
│   └── showcase/                   # 陪玩展示主页
├── layouts/                        # UserLayout, AdminLayout
├── components/                     # 通用组件
│   ├── user/                       # 用户端组件
│   ├── admin/                      # 管理员端组件
│   └── common/                     # 公共组件 (PixelSticker等)
└── types/index.ts                  # TypeScript 类型定义
```

## 核心架构要点

### 认证机制
- JWT 无状态认证，通过 `JwtAuthenticationFilter` 从 `Authorization: Bearer <token>` 提取
- 用户端和管理员共用同一套认证体系，userType 区分 `USER` / `ADMIN`
- 支持 refresh token 自动续期，前端 `request.ts` 中实现了 401 时排队等待 token 刷新
- 路由守卫区分用户端 (`/dashboard` 等) 和管理员 (`/admin/*`) 路径

### API 规范
- 统一前缀 `/api` (通过 server.context-path 配置)
- 统一返回体 `Result<T>`: `{code: 200, message: "success", data: ...}`
- 异常通过 `GlobalExceptionHandler` 统一处理 (业务异常 500/认证 401/权限 403/校验 400)
- 分页接口使用 `PageResult`

### 安全规则
- `/auth/**` 、 `/admin/auth/**` 、 `/error` 以及 OPTIONS 请求无需认证
- `/admin/**` 需要 `ROLE_ADMIN` 角色
- 其余所有接口需要认证
- 注册了 IP 过滤器 (`IpFilter`) 和限流过滤器 (`RateLimitFilter`)

### 数据库
- MySQL (端口 3307, 数据库名 xinpa)
- 所有表使用逻辑删除 (`deleted` 字段, 0=未删, 1=已删)
- 主键策略: 自增
- 初始化 SQL: `sql/init.sql`

### AI Agent
- 路径: `/api/agent/chat` (POST), 支持 Function Calling 自动编排
- 默认使用 Ollama 本地模型 (qwen2.5:7b), 通过 `application.yml` 中 `llm.*` 配置
- 对话历史存储在 Redis, 1 小时 TTL
- 支持确认后创建订单 (`/api/agent/order/confirm`)
- 支持 15 种业务工具调用 (CRUD 客户/订单/财务/待办/查询统计等)

### 环境配置
- 配置文件中使用 `${VAR_NAME:default}` 环境变量占位符 (如 `DB_USERNAME`, `DB_PASSWORD`)
- 支持 `.env` 文件 (通过 `spring-dotenv` 依赖)
- JWT 密钥/过期时间、MinIO 密钥均由 `application.yml` 管理

### 消息队列 (RabbitMQ)
- 预约提醒交换机: `xinpa.appointment` → 队列 `xinpa.appointment.queue`

### 文件存储
- 支持 MinIO (本地) 和阿里云 OSS
- 上传目录: `uploads/avatars`，静态资源映射 `/uploads/**`
- 前端支持图片/音频/视频素材上传

### 实体关系要点
- `Customer` (客户) → 关联 `Order` (订单) → 关联 `FinanceRecord` (财务记录)
- `Order` 关联 `OrderSource` (来源), `OrderTimerLog` (计时日志)
- `PricePackage` (套餐) 关联 `PackageType` (套餐类型)
- `Material` (素材) 关联 `MaterialType` (素材类型)
- `VIP` 等级通过 `VipLevel` 实体管理

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