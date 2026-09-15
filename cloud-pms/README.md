# 云协同 PMS · 企业项目管理系统

> 基于 Spring Boot 2.7 + Vue 3 的前后端分离项目管理系统
> 面向中小企业的项目全生命周期管理：立项 → 计划 → 任务 → 执行 → 工时 → 验收 → 结项

[![Java](https://img.shields.io/badge/Java-1.8-blue)]()
[![SpringBoot](https://img.shields.io/badge/SpringBoot-2.7.18-green)]()
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)]()
[![Redis](https://img.shields.io/badge/Redis-3.0%2B-red)]()

---

## 一、项目简介

云协同 PMS 是一套完整的企业级项目管理系统，覆盖项目从立项到结项的全流程，同时内置了完整的权限体系与系统管理能力。

**主要能力**

- 🔐 **认证鉴权** —— JWT 无状态令牌 + 图形验证码 + 滑动续期 + 主动注销
- 👥 **RBAC 权限** —— 用户/角色/菜单三级模型，支持按钮级权限与数据隔离
- 📁 **项目管理** —— 立项、成员分配、里程碑、进度自动计算、状态流转
- ✅ **任务管理** —— 任务拆解、指派、看板视图、甘特图、评论、逾期预警
- ⏱️ **工时管理** —— 填报、审批（通过/驳回）、单日上限校验、绩效统计
- 📎 **文件管理** —— 附件上传、MD5 秒传、存储抽象（本地/MinIO/OSS 可切换）
- 🔔 **消息通知** —— 站内信 + WebSocket 实时推送 + 定时逾期预警
- 📊 **统计报表** —— 数据看板、工时趋势、成员绩效、燃尽图、Excel 导出

---

## 二、技术栈

### 后端

| 技术 | 版本 | 用途 |
|---|---|---|
| Spring Boot | 2.7.18 | 核心框架、自动装配 |
| Spring Security | 5.7 | 认证授权、过滤器链 |
| JJWT | 0.11.5 | 令牌签发与校验 |
| MyBatis-Plus | 3.5.5 | ORM、分页、逻辑删除、自动填充 |
| MySQL | 8.0 | 主数据库 |
| Druid | 1.2.20 | 连接池与 SQL 监控 |
| Redis + Jedis | 3.0+ | 令牌/验证码/参数缓存 |
| Spring AOP | — | 操作日志切面 |
| Quartz / @Scheduled | — | 定时任务 |
| WebSocket | — | 消息实时推送 |
| Knife4j | 4.3.0 | 接口文档 |
| EasyExcel | 3.3.4 | Excel 导入导出 |
| Hutool | 5.8.25 | 工具库（含验证码生成） |
| Lombok | 1.18.30 | 代码简化 |
| Maven | 3.6+ | 构建 |

### 前端

| 技术 | 版本 | 用途 |
|---|---|---|
| Vue | 3.4 | 前端框架（组合式 API） |
| Vite | 5.2 | 构建工具 |
| Element Plus | 2.5 | UI 组件库 |
| Vue Router | 4.x | 动态路由 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.6 | HTTP 客户端（拦截器封装） |
| ECharts | 5.5 | 图表（甘特图/看板/燃尽图） |
| Sass | — | 样式预处理 |

---

## 三、快速开始

### 3.1 前置准备

确保本机已安装并启动 **MySQL 8** 与 **Redis**。

### 3.2 初始化数据库

```bash
mysql -uroot -p -e "create database biyesheji default character set utf8mb4 collate utf8mb4_general_ci;"
mysql -uroot -p --default-character-set=utf8mb4 biyesheji < sql/01-schema.sql
mysql -uroot -p --default-character-set=utf8mb4 biyesheji < sql/02-data.sql
mysql -uroot -p --default-character-set=utf8mb4 biyesheji < sql/03-business-menu.sql
```

> **三个脚本必须按顺序执行**，且要加 `--default-character-set=utf8mb4`（否则中文乱码）。
>
> 用 MySQL Workbench / Navicat 导入时若不报错说明正常；若提示
> `Error Code: 1175 safe update mode`，见 [部署文档 2.1 节](docs/部署文档.md) —— 脚本已内置处理，通常只需**重新连接一次数据库**再执行。

### 3.3 启动后端

```bash
cd pms-server

# 修改 src/main/resources/application-dev.yml 里的数据库账号密码

mvn clean package -DskipTests
java -jar target/pms-server.jar --server.port=8080
```

> ⚠️ **必须显式加 `--server.port=8080`**。部分环境会注入 `SERVER_PORT` 变量导致端口不确定，命令行参数优先级最高可避免此问题。

### 3.4 启动前端

```bash
cd pms-web
npm install
npm run dev
```

### 3.5 访问

| 入口 | 地址 |
|---|---|
| 前端系统 | http://localhost:5173 |
| 接口文档 | http://localhost:8080/doc.html |
| 健康检查 | http://localhost:8080/actuator/health |

**默认账号**

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `admin123` | 超级管理员 |
| `zhangwei` | `admin123` | 项目经理 |
| `lilei` | `admin123` | 项目成员 |

---

## 四、目录结构

### 4.1 目录用途一览

| 目录/文件 | 用途 | 是否入 Git | 说明 |
|---|---|---|---|
| `sql/` | 数据库脚本 | ✅ | 建表 + 初始化数据 + 业务菜单，按序号顺序执行 |
| `pms-server/` | 后端源码 | ✅（`target/`、`logs/` 除外） | Spring Boot 2.7 工程 |
| `pms-web/` | 前端源码 | ✅（`node_modules/`、`dist/` 除外） | Vue 3 + Vite 工程 |
| `docs/` | 交付文档 | ✅ | 数据库设计文档、部署文档、论文插图 |
| `deploy/` | 部署辅助 | ✅ | Nginx 配置、外部 MySQL 授权脚本 |
| `tools/` | 模板复用工具 | ✅ | 接单改名用（批量替换包名/工程名/库名） |
| `docker-compose.yml` | 部署编排 | ✅ | 服务器上的编排文件（Redis + Backend + Frontend） |
| `.env.example` | 配置模板 | ✅ | 需覆盖默认数据库配置时，复制为 `.env` 后填写 |
| `.gitignore` | 忽略规则 | ✅ | 根目录与本项目各一份（根目录的实际生效） |
| `deploy-out/` | **构建产物输出** | ❌ 不入库 | 手动构建（`mvn package` + `npm run build`）后把产物拷到这里，要上传到服务器的 4 项产物就在这里 |
| `release/` | ⚠️ **旧版产物（残留）** | ❌ 已忽略 | 旧版打包脚本的输出，已被 `deploy-out/` 取代，**建议删除** |

> **一句话总结**：`sql` `pms-server` `pms-web` `docs` `deploy` `tools` 是源码与文档，全部保留；
> `deploy-out` 是"要上传到服务器的产物"；`release` 是过时残留，可删。

### 4.2 目录树

```
cloud-pms/
├── sql/                          数据库脚本（按顺序执行）
│   ├── 01-schema.sql             23 张表建表语句
│   ├── 02-data.sql               系统域初始化数据（部门/用户/角色/菜单/字典/参数）
│   └── 03-business-menu.sql      业务模块菜单与角色授权
│
├── pms-server/                   后端服务
│   ├── Dockerfile                （已不再被 compose 使用，保留备查）
│   └── src/main/java/com/biyesheji/pms/
│       ├── PmsApplication.java   启动类
│       ├── common/               通用层
│       │   ├── constant/         常量定义
│       │   ├── core/             统一响应 R<T> / 分页 / 实体基类
│       │   ├── enums/            状态码 / 业务类型枚举
│       │   ├── exception/        业务异常 / 全局异常处理
│       │   └── utils/            Servlet 工具 / 密码生成工具
│       ├── framework/            框架层
│       │   ├── annotation/       @Log 注解
│       │   ├── aspect/           操作日志切面
│       │   ├── config/           Security / MyBatis-Plus / Redis / Knife4j / WebSocket 配置
│       │   ├── security/         JWT 过滤器 / 令牌服务 / 登录用户
│       │   ├── task/             定时任务（燃尽图快照 / 逾期预警）
│       │   ├── web/              登录服务与控制器
│       │   └── websocket/        WebSocket 端点
│       └── module/               业务模块
│           ├── system/           用户 / 角色 / 菜单 / 部门 / 岗位 / 字典 / 参数 / 日志
│           ├── project/          项目 / 成员 / 里程碑
│           ├── task/             任务 / 评论
│           ├── worklog/          工时
│           ├── file/             附件（含存储抽象）
│           ├── message/          站内消息
│           └── report/           统计报表
│
├── pms-web/                      前端工程
│   ├── vite.config.js            代理配置（/dev-api、/ws）
│   └── src/
│       ├── api/                  接口定义（login / system / business）
│       ├── directive/            v-permission 权限指令
│       ├── layout/               主框架布局（侧边栏 + 顶栏）
│       ├── router/               动态路由与守卫
│       ├── store/                Pinia 状态
│       ├── styles/               全局样式与主题变量
│       ├── utils/                请求封装 / 令牌存取
│       └── views/                页面
│           ├── login/            登录页
│           ├── dashboard/        工作台
│           ├── system/           系统管理（7 个页面）
│           ├── monitor/          日志（2 个页面）
│           ├── project/          项目 / 任务 / 工时 / 里程碑
│           ├── report/           数据看板 / 工时统计 / 成员绩效
│           └── message/          消息中心
│
├── docs/                         交付文档
│   ├── 数据库设计文档.md         23 张表完整说明 + E-R 图
│   ├── 部署文档.md               本地/生产部署 + 问题排查
│   └── diagrams/                 论文插图（SVG，可直接插入 Word）
│       ├── 01-系统分层架构图.svg
│       ├── 02-系统功能模块图.svg
│       ├── 03-登录认证时序图.svg
│       ├── 04-任务状态流转图.svg
│       └── 05-系统部署架构图.svg
│
├── deploy/                       部署辅助（源码级，全部入库）
│   ├── nginx.conf                生产环境 Nginx 配置（会随产物上传服务器）
│   └── init-external-db.sql      外部 MySQL 建库与授权脚本
│
├── deploy-out/                   ⚠️ 构建产物（不入库，随时可重新生成，可删）
│   ├── pms-server.jar            后端产物 → 上传服务器
│   ├── dist/                     前端产物 → 上传服务器
│   ├── docker-compose.yml        → 上传服务器
│   └── nginx.conf                → 上传服务器
│
├── release/                      ⚠️ 旧版打包脚本残留（已被 deploy-out 取代，建议删除）
│
├── tools/                        模板复用工具（接单提效用）
│   ├── rename.mjs                一键改名脚本
│   ├── template.config.json      配置模板
│   ├── example-student.json      示例配置
│   └── README.md                 使用说明
│
├── .env.example                  部署配置模板（默认配置已内嵌 compose，此文件可选）
├── .gitignore
├── docker-compose.yml            部署编排（redis + backend + frontend，官方镜像挂载 jar）
└── README.md
```

### 4.3 可删除项梳理

| 项 | 体积 | 性质 | 建议 |
|---|---|---|---|
| `release/` | 77.9 MB / 50 文件 | **旧版打包脚本的残留产物**，内容已被 `deploy-out/` 取代，且 compose 结构已过时 | **建议删除**，留着只会造成"该用哪个"的混淆 |
| `deploy-out/` | 77.8 MB / 41 文件 | 构建产物，手动构建后随时可以重新拷贝生成 | **本地可留可删**；已加入 .gitignore，不会进仓库 |
| `pms-server/target/` | ~76 MB | Maven 构建中间产物 | 不入库；本地建议保留（避免每次全量编译） |
| `pms-web/node_modules/` | ~171 MB / 1.3 万文件 | npm 依赖 | 不入库；本地必须保留（删了要重新 npm install） |
| `pms-server/logs/` | 少量 | 运行日志 | 不入库；本地可删 |
| `pms-web/dist/` | ~2.7 MB | 前端构建产物 | 不入库；`npm run build` 会重新生成 |

> 删除任何一项前确认：`release/` 和 `deploy-out/` 删了**不影响任何功能**，
> 重新执行一次打包命令即可还原；`node_modules` 删了则需要重新 `npm install`。

---

## 五、核心设计说明

### 5.1 认证流程

```
登录 → 校验验证码 → Spring Security 校验密码（BCrypt）
    → 生成随机令牌标识，登录用户信息存 Redis（120 分钟）
    → 签发 JWT（仅携带令牌标识，不放业务数据）
    → 前端存 localStorage，请求头携带 Authorization: Bearer <token>
    → 过滤器解析令牌 → 从 Redis 取登录用户 → 写入 SecurityContext
```

**为什么令牌不直接放用户信息？**
① 令牌体积小；② 支持服务端主动注销；③ 权限变更可即时生效（改角色后不用等令牌过期）。

### 5.2 动态路由

菜单由后端 `/getRouters` 下发，前端转换成 vue-router 路由后动态注册。

**好处**：后台调整菜单不需要改前端代码重新发布。

**注意**：后端菜单不含 `/` 路径，前端需补一条根路径重定向到「用户第一个可访问菜单」，否则访问根路径会落到兜底 404。

### 5.3 权限模型

```
用户 ──< 用户角色 >── 角色 ──< 角色菜单 >── 菜单（含按钮）
                      │
                      └── data_scope 数据范围（1全部 2本部门 3本部门及以下 4仅本人）
```

- **接口级**：`@PreAuthorize("hasAuthority('system:user:list')")`
- **按钮级**：前端 `v-permission="['system:user:add']"`
- **数据级**：非管理员查询项目列表时，SQL 层限定为「自己参与的项目」

### 5.4 存储抽象

文件存储定义了 `StorageService` 接口，当前实现为本地磁盘（`LocalStorageServiceImpl`），业务代码只依赖接口。

若要切换到 MinIO / 阿里云 OSS，**只需新增一个实现类，业务代码零改动**。

---

## 六、常见问题

完整排查手册见 [`docs/部署文档.md`](docs/部署文档.md) 第四章。

**最高频的三个问题：**

| 问题 | 原因 | 解决 |
|---|---|---|
| 登录一直提示密码错误 | 实体里的非表字段漏标 `@TableField(exist = false)`，导致 ORM 去查不存在的列 | 检查实体定义 |
| 启动端口不对 / 启动失败 | 环境注入了 `SERVER_PORT` | 显式加 `--server.port=8080` |
| 中文乱码 | 导入 SQL 未指定字符集 | 加 `--default-character-set=utf8mb4` |

---

## 七、开发约定

| 约定 | 说明 |
|---|---|
| 统一响应 | 所有接口返回 `R<T>`，`code=200` 为成功 |
| 业务异常 | 抛 `ServiceException`，由全局异常处理器转换，不写 try-catch 兜底 |
| 权限标识 | 格式 `模块:实体:动作`，如 `system:user:list` |
| 操作日志 | 增删改加 `@Log`，**查询不加**（避免日志表膨胀） |
| 逻辑删除 | 统一用 `del_flag`（0存在 2删除），由 MyBatis-Plus 自动处理 |
| 实体基类 | 只有含 `create_by`/`update_by` 列的表才继承 `BaseEntity` |
| 非表字段 | 一律标 `@TableField(exist = false)` |

---

## 八、部署

采用**混合部署**：MySQL 独立部署（数据边界清晰、便于单独备份），Redis 与应用容器化。

```bash
# 1. 准备外部 MySQL（建库 + 导入脚本 + 授权）
mysql -uroot -p < deploy/init-external-db.sql

# 2. 配置数据库连接
cp .env.example .env && vi .env     # 填 DB_HOST / DB_USER / DB_PASSWORD / DB_NAME

# 3. 构建产物
cd pms-server && mvn clean package -DskipTests && cd ..
cd pms-web && npm install && npm run build && cd ..

# 4. 启动
docker compose up -d
```

**`DB_HOST` 取值**：同机填 `host.docker.internal`；异机填对方 IP。

> 容器里的 `127.0.0.1` 指容器自己，不是宿主机 —— 这是混合部署最容易踩的坑。
> 本项目已在编排中声明 `extra_hosts: host.docker.internal:host-gateway`，Linux 下同样可用。

编排内容：`redis` + `backend` + `frontend` 三个容器。按需加回 `mysql` 服务即可切换为全容器化（适合本地演示）。

详见 [`docs/部署文档.md`](docs/部署文档.md) 第三章。

---

## 九、License

本项目仅供学习与毕业设计参考使用。
