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

## 二、文档导航

项目共 4 份文档，**各管一件事、内容不重复**。找不到东西时，先看这张表：

| 我想…… | 看哪份 |
|---|---|
| 把项目跑起来、部署到服务器、排查报错 | [`docs/部署文档.md`](docs/部署文档.md) |
| 了解 23 张表怎么设计、写论文「系统设计」章 | [`docs/数据库设计文档.md`](docs/数据库设计文档.md) |
| 说明用了哪些技术、准备论文章节与答辩话术 | [`docs/技术选型与功能映射清单.md`](docs/技术选型与功能映射清单.md) |
| 查每个文件夹干什么用、开发要守哪些约定 | **本文件** |
| 把本项目复用成新毕设（批量改名交付） | [`tools/README.md`](tools/README.md) |

---

## 三、5 分钟跑起来

前置：本机已启动 **MySQL 8** 与 **Redis**。

```bash
# 1. 建库 + 导入（三个脚本必须按顺序，务必带字符集参数，否则中文乱码）
mysql -uroot -p -e "create database biyesheji default character set utf8mb4 collate utf8mb4_general_ci;"
mysql -uroot -p --default-character-set=utf8mb4 biyesheji < sql/01-schema.sql
mysql -uroot -p --default-character-set=utf8mb4 biyesheji < sql/02-data.sql
mysql -uroot -p --default-character-set=utf8mb4 biyesheji < sql/03-business-menu.sql

# 2. 启动后端（先改 pms-server/src/main/resources/application-dev.yml 的库账号密码）
cd pms-server
mvn clean package -DskipTests
java -jar target/pms-server.jar --server.port=8080

# 3. 启动前端
cd pms-web
npm install
npm run dev
```

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

> ⚠️ **后端必须显式加 `--server.port=8080`** —— 部分环境会注入 `SERVER_PORT` 变量导致端口不确定。
>
> 环境要求、Docker 部署、日常运维、报错排查 → 见 [`docs/部署文档.md`](docs/部署文档.md)

---

## 四、目录结构

### 4.1 目录用途一览

| 目录 / 文件 | 用途 | 入 Git |
|---|---|---|
| `sql/` | 数据库脚本：建表 + 初始化数据 + 业务菜单（按序号顺序执行） | ✅ |
| `pms-server/` | 后端源码（Spring Boot 2.7） | ✅ |
| `pms-web/` | 前端源码（Vue 3 + Vite） | ✅ |
| `docs/` | 交付文档：部署 / 数据库设计 / 技术选型 + 论文插图 | ✅ |
| `deploy/` | 部署**源文件**：生产 Nginx 配置、外部 MySQL 建库授权脚本 | ✅ |
| `tools/` | 模板复用工具：一键改名，把本项目复制成新毕设 | ✅ |
| `docker-compose.yml` | 部署编排**母版**（Redis + Backend + Frontend） | ✅ |
| `.env.example` | 部署配置模板，需覆盖默认值时复制为 `.env` 填写 | ✅ |
| `deploy-out/` | 构建**产物暂存区**，`scp` 上传服务器的就是这个目录 | ❌ |

> **一句话**：`sql` `pms-server` `pms-web` `docs` `deploy` `tools` 是源码与文档，全部保留；
> `deploy-out/` 是"要上传到服务器的成品"，随时可由构建重新生成。

### 4.2 目录树（要点）

```
cloud-pms/
├── sql/                     数据库脚本（01-schema / 02-data / 03-business-menu）
├── pms-server/              后端
│   ├── Dockerfile           （已不再被 compose 使用，保留备查）
│   └── src/main/java/com/biyesheji/pms/
│       ├── common/          通用层：统一响应 R<T> / 异常 / 枚举 / 工具
│       ├── framework/       框架层：Security / 配置 / 切面 / 定时任务 / WebSocket
│       └── module/          业务模块：system project task worklog file message report
├── pms-web/                 前端
│   └── src/                 api / router / store / layout / directive / styles / views
├── docs/                    交付文档
│   ├── 部署文档.md
│   ├── 数据库设计文档.md
│   ├── 技术选型与功能映射清单.md
│   └── diagrams/            论文插图（5 张 SVG，可直接插入 Word）
├── deploy/                  部署源文件（nginx.conf / init-external-db.sql）
├── deploy-out/              构建产物（不入库，scp 上传用）
├── tools/                   模板复用工具（rename.mjs + 配置模板）
├── docker-compose.yml       部署编排母版
└── .env.example             部署配置模板
```

---

## 五、技术栈

| 层 | 主要技术 |
|---|---|
| 后端 | Spring Boot 2.7.18 · Spring Security · JJWT · MyBatis-Plus 3.5.5 · MySQL 8 · Redis · Druid · Quartz · WebSocket · Knife4j · EasyExcel · Hutool |
| 前端 | Vue 3.4 · Vite 5 · Element Plus · Vue Router · Pinia · Axios · ECharts · Sass |

> 完整版本号、每项技术的选型理由、功能-技术映射、论文可写点 →
> [`docs/技术选型与功能映射清单.md`](docs/技术选型与功能映射清单.md)

---

## 六、核心设计

### 6.1 认证流程

```
登录 → 校验验证码 → Spring Security 校验密码（BCrypt）
    → 生成随机令牌标识，登录用户信息存 Redis（120 分钟）
    → 签发 JWT（仅携带令牌标识，不放业务数据）
    → 前端存 localStorage，请求头携带 Authorization: Bearer <token>
    → 过滤器解析令牌 → 从 Redis 取登录用户 → 写入 SecurityContext
```

**为什么令牌不直接放用户信息？** ① 令牌体积小；② 支持服务端主动注销；③ 权限变更可即时生效。

### 6.2 动态路由

菜单由后端 `/getRouters` 下发，前端转换后动态注册 —— 后台调整菜单**无需改前端代码重新发布**。

注意：后端菜单不含 `/` 路径，前端需补一条根路径重定向到「用户第一个可访问菜单」，否则访问根路径会落到兜底 404。

### 6.3 权限模型

```
用户 ──< 用户角色 >── 角色 ──< 角色菜单 >── 菜单（含按钮）
                      │
                      └── data_scope 数据范围（1全部 2本部门 3本部门及以下 4仅本人）
```

- **接口级**：`@PreAuthorize("hasAuthority('system:user:list')")`
- **按钮级**：前端 `v-permission="['system:user:add']"`
- **数据级**：非管理员查询项目列表时，SQL 层限定为「自己参与的项目」

### 6.4 存储抽象

文件存储定义 `StorageService` 接口，当前实现为本地磁盘（`LocalStorageServiceImpl`），业务代码只依赖接口。

切换到 MinIO / 阿里云 OSS 只需**新增一个实现类，业务代码零改动**。

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

**最高频的三个坑**

| 现象 | 原因 | 解决 |
|---|---|---|
| 登录一直提示密码错误 | 实体里的非表字段漏标 `@TableField(exist = false)` | 检查实体定义 |
| 启动端口不对 / 启动失败 | 环境注入了 `SERVER_PORT` | 显式加 `--server.port=8080` |
| 中文乱码 | 导入 SQL 未指定字符集 | 加 `--default-character-set=utf8mb4` |

---

## 八、部署（简述）

采用**混合部署**：MySQL 独立部署（数据边界清晰、便于单独备份），Redis 与应用容器化。

```bash
# 1. 准备外部 MySQL（建库 + 导入脚本 + 授权，一次性）
mysql -uroot -p < deploy/init-external-db.sql

# 2. 按需配置数据库连接（不建 .env 则直接使用内嵌默认值）
cp .env.example .env && vi .env

# 3. 构建产物
cd pms-server && mvn clean package -DskipTests && cd ..
cd pms-web && npm install && npm run build && cd ..

# 4. 启动
docker compose up -d
```

访问入口 `http://<服务器IP>:9000`（后端 18080 → 容器 8080，前端 9000 → 容器 80）。
默认值刻意避开 **80 / 8080** —— 1Panel 等面板自带的 OpenResty 通常占用这两个端口。

> 完整流程（产物放置、上传、版本升级、连通性排查、非 Docker 部署）→
> [`docs/部署文档.md`](docs/部署文档.md) 第三章

---

## 九、License

本项目仅供学习与毕业设计参考使用。
