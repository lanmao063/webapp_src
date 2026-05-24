# webapp
东北大学web开发大作业-驿站管理系统
# 快递驿站管理系统

Spring Boot 3 + Vue 3 全栈项目，实现快递驿站的包裹入库、取件、寄件、异常处理和统计分析。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.5.14 (Java 21) |
| ORM | MyBatis-Plus 3.5.7 |
| 认证 | JWT (jjwt 0.12.6) + BCrypt |
| 数据库 | MySQL 8 |
| 前端框架 | Vue 3 (Composition API) |
| UI 组件库 | Element Plus 2 |
| 图表 | ECharts 6 + vue-echarts 8 |
| 日志 | Log4j2 |

## 角色功能

### 普通用户 (REGULAR)
- 首页查看取件码、待取包裹
- 快递追踪、取件
- 寄件申请、在线支付
- 查看待付款订单
- 异常包裹登记、查看我的异常记录

### 快递员 (COURIER)
- 工作台统计（今日揽收、今日入库）
- 包裹入库（生成取件码）
- 揽收已付款寄件包裹

### 管理员 (MANAGER)
- 数据概览（在库/已取件/总包裹/未处理异常）
- 包裹搜索、库存盘点（ECharts 图表）
- 寄件审批/驳回（可修改重量自动重算费用，自动分配快递员）
- 异常包裹处理
- 自动出库记录查看
- 数据初始化（通行码: `admin123456`）

## 数据库设计

5 张核心表：

### system_user — 统一用户表
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT (PK, AUTO) | |
| username | VARCHAR(50) | 唯一 |
| password | VARCHAR(255) | BCrypt 加密 |
| role | VARCHAR(20) | REGULAR / COURIER / MANAGER |
| real_name | VARCHAR(50) | |
| phone | VARCHAR(20) | |
| id_number | VARCHAR(18) | 快递员/管理员工号 |
| status | TINYINT | 1=启用, 0=禁用 |

### package — 包裹基础信息表
| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT (PK, AUTO) | |
| tracking_number | VARCHAR(50) | 快递单号，唯一 |
| package_name | VARCHAR(100) | |
| weight / volume | DOUBLE | 重量(kg) / 体积(cm³) |
| sender_name / sender_phone / sender_address | VARCHAR | 寄件人信息 |
| receiver_name / receiver_phone / receiver_address | VARCHAR | 收件人信息 |

### inbound_package — 入库包裹表
关联 `package_id`，含取件码、柜型(SMALL/MEDIUM/LARGE)、代取人手机号。状态流转：`IN_WAREHOUSE` → `CHECKED_OUT`。

### send_package — 寄件包裹表
关联 `package_id`，含费用、付款状态、取件方式、分配的快递员。状态流转：`SUBMITTED` → `APPROVED` → `PAID` → `COLLECTED`（或 → `REJECTED`）。

### error_parcel — 异常包裹表
关联 `package_id`，含异常类型(DAMAGED/LOST/WRONG_ADDRESS/OTHER)和处理结果。状态流转：`UNRESOLVED` → `RESOLVED`。

## 后端架构

标准分层架构：Controller → Service → Mapper (MyBatis-Plus BaseMapper)。

### 项目结构

```
src/main/java/com/neu/webapp/
├── config/          # 配置类（CORS、MyBatis-Plus分页、BCrypt）
├── common/          # 通用响应类 Result<T>
├── controller/      # 7个控制器
├── dto/             # 请求体（LoginRequest、RegisterRequest 等）
├── entity/          # 数据库实体（5个）
├── exception/       # BusinessException + GlobalExceptionHandler
├── mapper/          # MyBatis-Plus Mapper（5个）
├── security/        # JWT 认证（JwtUtil、JwtInterceptor、UserContext）
├── service/         # 业务逻辑（7个 Service + 1个定时任务调度器）
└── vo/              # 响应体（LoginResponse、UserInfo）
```

### API 端点汇总（约30个）

| 模块 | 路径前缀 | 主要端点 |
|---|---|---|
| AuthController | `/auth` | POST login/register/forgot-password/logout, GET current |
| AdminController | `/admin` | POST package-init（通行码保护） |
| InboundPackageController | `/inbound` | PUT warehouse-entry/checkout/authorize-proxy, GET search/my-pickup-codes/auto-checkout-list |
| SendPackageController | `/send-package` | POST /, GET search/pending-list/my-unpaid/paid-list, PUT approve/reject/pay/collect |
| PackageController | `/package` | GET track, GET search |
| ErrorParcelController | `/error-parcel` | POST /, GET list/my-list, PUT handle |
| StatisticsController | `/statistics` | GET overview/yearly/monthly/chart/courier-overview |

### 安全机制

- 自定义 JWT 拦截器（不使用 Spring Security），排除 `/auth/**` 和 `/admin/**`
- 短期 token 2小时，记住密码 7天
- ThreadLocal (UserContext) 传递用户上下文
- 注意：后端 API 层面未做角色鉴权，仅前端路由守卫控制

### 定时任务

- **自动出库**：每小时检查入库超5天且状态为 IN_WAREHOUSE 的包裹，自动标记 CHECKED_OUT
- **自动清理**：每天凌晨2点删除出库超7天的记录

### 核心业务流程

**入库流程**: 管理员数据初始化 → 快递员入库扫描(根据体积分配柜号+生成取件码) → 用户取件(手机号匹配)

**寄件流程**: 用户创建寄件 → 管理员审批(分配快递员，可调重量自动重算费用) → 用户付款 → 快递员揽收

**异常处理**: 用户登记异常 → 管理员处理并标记解决

## 前端架构

Vue 3 (Composition API + `<script setup>`)，hash 路由模式。

### 项目结构

```
webapp-frontend/src/
├── router/index.js    # 路由定义 + 导航守卫（Token检查 + 角色匹配）
├── utils/
│   ├── auth.js        # Token/用户信息本地存储管理
│   └── request.js     # Axios 封装（baseURL=/webapp，15s超时，401自动跳转登录）
└── views/
    ├── LoginView.vue          # 登录页
    ├── RegisterView.vue       # 注册页
    ├── ForgotPasswordView.vue # 忘记密码页
    ├── AdminDataInit.vue      # 管理员数据初始化
    ├── Index.vue              # 主布局（顶栏 + 角色菜单 + router-view）
    ├── Welcome.vue            # 仪表盘（按角色渲染不同内容）
    ├── ParcelTrack.vue        # [REGULAR] 查件
    ├── ParcelPickup.vue       # [REGULAR] 取件
    ├── ParcelSend.vue         # [REGULAR] 寄件
    ├── MyUnpaid.vue           # [REGULAR] 待付款订单
    ├── ErrorRegister.vue      # [REGULAR] 异常登记
    ├── MyErrors.vue           # [REGULAR] 我的异常记录
    ├── ParcelSearch.vue       # [MANAGER] 包裹查询
    ├── ErrorSearch.vue        # [MANAGER] 异常查询
    ├── Inventory.vue          # [MANAGER] 库存盘点（ECharts图表）
    ├── SendManage.vue         # [MANAGER] 寄件审批
    ├── ErrorHandle.vue        # [MANAGER] 异常处理
    ├── AutoCheckout.vue       # [MANAGER] 自动出库记录
    ├── PickupQuery.vue        # [COURIER] 揽收
    └── ParcelDelivery.vue     # [COURIER] 快递入库
```

### 角色菜单

| 角色 | 菜单 |
|---|---|
| REGULAR | 快递服务(首页/查件/取件/寄件) → 订单管理(待付款) → 异常管理(登记/我的异常) |
| MANAGER | 包裹管理(首页/查询/盘点/自动出库) → 审批管理(寄件审批) → 异常管理(查询/处理) |
| COURIER | 首页(工作台) → 任务管理(揽收/快递入库) |

### 路由守卫

三层检查：Token 存在性 → 角色精确匹配 → 未登录重定向 `/login`


