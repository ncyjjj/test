# Material Maker（素材制作网站）

一个可独立运行的素材制作网站：用户浏览模板、上传图片并提交制作任务，在任务后台处理期间继续使用网站，最后查看自己的历史记录和制作结果。

本项目严格按照笔试要求实现：Java 后端组织多步骤异步处理，Next.js 前端管理异步状态，任务状态与历史记录持久化至 MySQL。

## 技术版本
| 层级 | 技术 | 版本（选型并标明） |
|---|---|---|
| 前端 | Next.js（App Router）、React、TypeScript、Tailwind CSS | Next.js 15.0.3、React 19.0.0、TypeScript 5.6.3、Tailwind CSS 3.4.14 |
| 状态管理 | Zustand | 5.0.1 |
| 后端 | Java、Spring Boot、MyBatis | Java 21、Spring Boot 3.4.0、MyBatis Spring Boot Starter 3.0.4 |
| 数据库 | MySQL | MySQL 8.4 |
| 测试（前端） | Vitest、React Testing Library | Vitest 2.1.4、@testing-library/react 16.0.1 |
| 测试（后端） | JUnit 5、Spring Boot Test、H2（测试用） | 随 Spring Boot 3.4.0 |
| 容器 | Docker、Docker Compose | 按环境提供 |

## 项目结构
```text
material-maker/
├── .github/workflows/ci.yml        # CI（前端 lint/typecheck/build + 后端 test/build）
├── db/init.sql                    # 数据库结构、种子模板、初始化数据
├── docker-compose.yml             # 一键启动（frontend/backend/mysql）
├── backend/                       # Spring Boot + MyBatis 后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/materialmaker/  # config/controller/service/mapper/model/dto/exception/util
│       ├── main/resources/               # application.yml、mapper/*.xml、static/demo、static/results
│       └── test/java/com/materialmaker/  # 核心测试
├── frontend/                      # Next.js App Router 前端
│   ├── Dockerfile
│   ├── app/                       # 路由（/, /templates/[id], /history）
│   ├── components/
│   ├── lib/                       # api、store、类型等
│   ├── __tests__/
│   └── package.json
└── README.md
```

## 构建、测试、启动
### 方式一：Docker Compose（一键启动，推荐，符合 R-14）
```bash
# 在项目根目录
cp .env.example .env  # 可按需调整（非敏感示例）
docker compose up --build
```
启动后：
- 前端：http://localhost:3000
- 后端：http://localhost:8080
- MySQL：localhost:3306（用户 material_maker / 密码 material_maker_pass，数据库 material_maker）

首次启动会自动执行 `db/init.sql` 初始化表结构和种子模板。

### 方式二：本地开发（前后端分开）
**前提：** JDK 21、Maven、Node.js 22+、MySQL 8.4

1. 初始化数据库
```sql
SOURCE db/init.sql;
-- 或直接执行 init.sql 内容
```

2. 启动后端
```bash
cd backend
# 可通过环境变量覆盖连接配置（见 .env.example）
mvn spring-boot:run
# 后端默认 http://localhost:8080
```

3. 启动前端
```bash
cd frontend
npm install
echo "NEXT_PUBLIC_API_BASE_URL=http://localhost:8080" > .env.local
npm run dev
# 前端默认 http://localhost:3000
```

### 测试
```bash
# 前端 lint、类型检查、测试
cd frontend
npm run lint
npm run typecheck
npm run test

# 后端测试与构建
cd backend
mvn test
mvn package -DskipTests
```

## 前后端及数据库连接
- **前端 -> 后端：** 通过 `NEXT_PUBLIC_API_BASE_URL` 配置（默认 `http://localhost:8080`），所有 API 请求带 `X-Demo-User-Id` header 用于演示用户隔离（见下文用户身份设计）。
- **后端 -> 数据库：** 通过 `application.yml` 中的环境变量 `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` 配置，默认连 `localhost:3306/material_maker`。
- **文件存储：** 上传文件保存到后端 `uploads/materials/{taskId}/`，模拟结果保存到 `uploads/results/{taskId}/`，并通过 `/uploads/**` 静态资源映射暴露；模板示例封面和示例结果图位于 `backend/src/main/resources/static/`。

## 所需配置项及无真实凭据示例
见 `.env.example`：
```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=material_maker
DB_USER=material_maker
DB_PASSWORD=material_maker_pass
CORS_ALLOWED_ORIGINS=http://localhost:3000
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
UPLOAD_DIR=uploads
```
> 不使用任何真实 API 密钥、支付凭据或第三方账号。所有数据均为本地演示用。

## 数据库初始化、模拟数据与素材准备
- **结构与初始化：** `db/init.sql` 创建 `users`、`templates`、`materials`、`tasks`、`task_events` 表并插入种子模板（`demo-postcard`、`demo-poster`、`demo-avatar`）。
- **模拟数据：** 种子模板直接写入数据库，由后端 `/api/templates` 接口读取（非纯前端硬编码页面）。
- **素材准备：** 
  - 模板封面：`backend/src/main/resources/static/demo/*.svg`（本地可访问示例）
  - 模拟结果图：`backend/src/main/resources/static/results/result-sample.svg`（异步处理成功后复制到 `uploads/results/` 并返回 `result_url`）
  - 用户上传素材需为 `image/jpeg` 或 `image/png`，符合模板要求的数量、类型、大小；存放在后端本地目录并通过 `/uploads/**` 暴露。

## 主要接口、请求/响应字段、任务状态、阶段与后台执行方式
### 核心接口
| 方法 | 路径 | 说明 | 请求 | 响应（主要字段） |
|---|---|---|---|---|
| GET | `/api/templates?category=` | 模板列表（支持分类筛选） | query: category（可选） | `ApiResponse<Template[]>` |
| GET | `/api/templates/{id}` | 模板详情 | path: id | `ApiResponse<Template>`（找不到返回 `success:false`） |
| POST | `/api/tasks` | 提交制作任务（创建任务后即刻返回，不等待处理结束） | form: `templateId`, `files[]`（multipart） | `ApiResponse<Task>`，消息 `task accepted` |
| GET | `/api/tasks/{taskId}` | 查询单个任务（只读，不推进任务） | path: taskId | `ApiResponse<Task>` |
| GET | `/api/tasks` | 当前用户历史任务列表 | header: `X-Demo-User-Id` | `ApiResponse<Task[]>` |
| GET | `/api/users/me` | 获取/创建当前演示用户 | header: `X-Demo-User-Id` | `ApiResponse<User>` |
| POST | `/api/users/demo` | 创建演示用户（可用于切换演示） | - | `ApiResponse<User>` |
| GET/POST | `/api/dev/simulation/config` | **仅开发环境**模拟配置（演示用） | GET/POST JSON | 查看或修改 `failAtStage`、`delay-ms` |

### 任务状态与阶段
- **status（整体状态）：** `WAITING`、`PROCESSING`、`SUCCESS`、`FAILED`
- **stage（当前阶段）：** `ASSET_CHECK`、`GENERATE`、`FINALIZE`（仅在处理中/流转时更新）
- **失败信息：** `fail_stage`、`fail_reason`（失败后保存，后续查询不会变回处理中）

### 后台执行方式
- 使用 `@EnableAsync` + 自定义 `ThreadPoolTaskExecutor`（bean `taskExecutor`，核心4/最大8/队列100）实现**异步后台执行**。
- `TaskService.createTask`：校验模板、素材数量/类型/大小、保存素材、创建任务记录（`WAITING`）后**立即提交异步处理并返回 `taskId`**。
- `TaskProcessor.execute(taskId)`（`@Async("taskExecutor")`）：按顺序执行 **素材检查（ASSET_CHECK） → 模拟生成（GENERATE） →结果整理（FINALIZE）**。每一步更新 `status/stage` 并写入 `task_events`；若某一步触发失败则**终止后续步骤**，保存 `fail_stage/fail_reason/status=FAILED`。成功后生成结果图并设置 `result_url/status=SUCCESS/finished_at`。
- 查询接口（`GET /api/tasks/*`）**只读**，不会驱动任务向下一步推进。

## 如何演示（成功、失败、耗时、多任务、刷新恢复）
### 1) 成功流程
1. 打开 http://localhost:3000，进入模板列表（数据来自 Java 接口）
2. 点击模板（如“旅行明信片”）进入详情页
3. 按要求上传图片（预览、替换、移除），校验通过后点击“提交制作任务”
4. 提交成功后页面提示“任务已受理（任务ID：...）”，跳转或前往 `/history` 查看
5. 历史页会自动轮询跟踪，状态依次显示 `等待处理/处理中（素材检查/模拟生成/结果整理）`，最终变为 `成功` 并显示**可预览结果图片**

### 2) 指定阶段失败（演示用）
可通过开发环境模拟配置触发指定阶段失败：
- **方式 A：POST** `http://localhost:8080/api/dev/simulation/config`
```json
{"failAtStage": "GENERATE"}
```
可选值：`ASSET_CHECK`、`GENERATE`、`FINALIZE`，置空 `""` 恢复全成功。
- **方式 B：修改** `backend/src/main/resources/application.yml` 的 `app.simulation.fail-at-stage` 后重启后端

提交新任务后，后台会执行到指定阶段并**中断后续**，任务状态变为 `FAILED`，历史页显示 `失败阶段` 和 `失败原因`。

### 3) 调整处理耗时
修改 `application.yml`：
```yaml
app:
  simulation:
    delay-ms:
      asset-check: 1500
      generate: 3000
      finalize: 800
```
或通过上面的开发接口 POST 修改，便于观察“等待处理/处理中”和多任务状态。

### 4) 多任务（互不串扰）
连续提交多个任务（可切换模板或同模板多次提交）。前端用 `zustand` 独立管理 `tasksMap`，每个任务单独轮询跟踪；**一个任务成功/失败不影响另</当前>个任务**的状态、阶段、错误和结果。

### 5) 刷新恢复与查询异常处理
- **刷新恢复：** 刷新 `/history` 页面，前端会调用 `GET /api/tasks` 恢复个人历史记录，并自动跟踪所有 `WAITING/PROCESSING` 的未结束任务，继续轮询直至结束。
- **查询接口暂时不可用：** 前端轮询时若 `GET /api/tasks/{id}` 抛出网络错误，仅标记 `queryError` 并保留已有任务信息，**不会**将后台任务标记为 `FAILED`，同时提示“查询暂时不可用，已保留当前任务信息，将自动恢复查询”。离开页面或无活跃任务时会停止轮询并清理定时器。

## 用户身份与个人记录的设计及演示方法
### 设计方案（R-10）
采用**方案 A：自动创建演示用户 + localStorage + 请求头**：
- **前端：** 首次访问时在 `localStorage` 生成 `mm_user_id`（如 `demo-xxxx`），并在所有 API 请求中通过 `X-Demo-User-Id` header 传递。
- **后端：** `UserContext` 解析 header；`UserController` 的 `GET /api/users/me` 在用户不存在时自动创建用户；所有任务、历史接口均基于该用户 ID 过滤个人记录。
- **隔离：** 清空 `localStorage` 或手动切换 `X-Demo-User-Id`（浏览器开发工具/接口工具）即可切换到另一个演示用户，观察“个人历史任务”仅显示该用户的记录。

### 演示方法
1. 正常使用默认用户提交任务并查看历史
2. 打开浏览控件（Application > Local Storage）将 `mm_user_id` 改为 `demo-other` 或直接清空
3. 刷新页面后再提交任务，访问 `/history` 将只看到新用户的任务记录，旧用户的记录不会出现

## 已完成／未完成项、已知限制与实际投入
### 已完成项
- [x] 模板列表（分类筛选）+ 详情页（上传、预览、替换、移除、校验）
- [x] 提交后即刻返回任务 ID，后台用 `@Async` 顺序执行三步骤（素材检查 → 模拟生成 →结果整理）
- [x] 任务状态/阶段/失败原因/结果 URL 持久化，查询只读不推进任务
- [x] 指定阶段失败模拟（开发环境配置）+ 可调整耗时
- [x] 多任务跟踪，任务结束后停止跟踪，任务间互不串扰
- [x] 查询异常不标记为任务失败，保留状态并提示恢复
- [x] 个人历史、刷新恢复、成功结果预览
- [x] 用户身份（演示用户）与个人记录隔离
- [x] CI 配置（GitHub Actions）
- [x] 核�心测试（后端上下文加载、前端历史页渲染）
- [x] Docker Compose 一键启动、Dockerfile、.env.example
- [x] 完整 README

### 未完成项（不在本题范围或有意取舍）
- [ ] **服务重启后自动恢复执行**：题目明确不要求。Spring Boot `@Async` 任务不具备重启恢复能力。取舍：优先保证“提交即受理、状态持久化、查询只读”这套闭环，避免引入 MQ/Redis 等复杂运维组件。
- [ ] **失败任务重新执行**：题目明确“不需要用户重新执行失败任务的功能”，仅支持观察模拟失败。
- [ ] **任务受理幂等/断点续跑/自动重试**：题目允许在 README 讨论取舍，不作为未实现扣分项。本项目未实现，理由是演示闭环足够且减少复杂度。
- [ ] **复杂模板后台、积分/会员/第三方账号/国际化/集群/消息队列运维**：题目明确不要求。

### 已知限制
- 上传文件仅支持 `image/jpeg`、`image/png`（按模板要求演示）
- 模拟结果仅生成 `result.svg` 预览图（不要求下载、删除、重新制作、视频处理）
- 前端使用轮询更新状态（未强制 SSE/WebSocket），但已实现“无活跃任务时停止轮询”和清理机制
- 开发模拟控制器默认在 `dev/default` profile 可用，生产场景不应暴露

### 实际投入（诚实记录）
- **设计与搭建：** 按题目要求梳理 R-01~R-14，设计数据模型、异步流程、前端状态管理、CI、容器交付等
- **编码：** 前后端全栈实现、配置、测试、Docker 化
- **验证：** 按演示清单自测（成功、指定阶段失败、耗时调整、多任务、刷新恢复、查询异常处理）
- **说明书写：** 完整 README 覆盖所有要求项

## 部署与回滚的基本步骤
### 部署（容器化）
1. 准备环境：Docker、Docker Compose
2. 克隆仓库或解压源码压缩包
3. 配置：复制 `.env.example` 为 `.env`（如需自定义）
4. 各容器配置参见 `docker-compose.yml`
5. 启动：`docker compose up --build -d`
6. 初始化：MySQL 容器会自动执行 `db/init.sql`

### 回滚（基本思路）
- **回滚到之前的镜像/代码：** 使用 `git` 切换到历史提交（`git checkout <commit>`）后重新 `docker compose up --build -d`
- **清理数据：** 若需要重置数据库，停止容器并删除卷（`docker compose down -v`）后重新启动会重新初始化 `db/init.sql`
- **本地文件：** `uploads/` 卷中的上传文件和结果文件需根据实际情况备份或清理

## 是否使用 AI
**是，使用了 AI 编程工具。**

### AI 工作流（具体例子）
以“实现后端多步骤异步处理”为例：
1. **任务拆解：** 将 R-04/R-05/R-06 拆解为“任务创建（即刻返回）”、“异步线程池”、“三步骤顺序”、“失败中断”、“状态持久化”、“查询只读”等子任务
2. **上下文提供：** 将题目全文、计划文档、数据库设计、现有目录结构提供给 AI，明确要求“不强制 MQ/Redis/SSE/WebSocket”、“关闭页面不应中断”、“查询接口异常不能标记任务失败”等硬约束
3. **生成/修改：** AI 辅助生成 `AsyncConfig`、`TaskProcessor`、Mapper XML 等代码结构，随后根据实际需求调整字段、异常处理、事务等细节
4. **验证结果：** 手动检查代码逻辑（每步是否更新阶段、失败是否终止、`updateSuccess/updateFailed` 是否正确、查询接口是否只读）、运行 `mvn test` 验证后端测试
5. **修正问题：** 针对“查询异常不标记失败”、“多任务互不串扰”、“刷新恢复”等场景反复修正前端轮询逻辑和后端状态约束

> 未提交私人聊天记录、账号或全部提示词，仅以该具体例子说明工作流。

## 使用现有脚手架、开源代码或素材时说明来源及主要复用范围
- **脚手架：** 未使用 `create-next-app` 一键生成的完整模板仓库，而是**根据题目要求自主搭建** Next.js App Router、Spring Boot 工程结构。依赖均为官方或常用稳定版本。
- **开源代码：** 未直接复制大型第三方开源项目源码，仅参考 Spring Boot/MyBatis 官方文配置模式。
- **素材：** 模板封面、示例结果图均使用 **SVG 自制**（见 `backend/src/main/resources/static/demo/*.svg`、`backend/src/main/resources/static/results/result-sample.svg`），不依赖付费、公司内部或外部需登录下载的素材。
- **复用范围：** 主要为项目配置规范（Maven、npm、Docker）和基础架构模式，无核心业务逻辑复用。

## 验收说明
- **交付形式：** 可接受 GitHub 仓库、本地项目目录或源码压缩包。本项目包含前后端完整源码、依赖版本/锁文件、`db/init.sql`、CI配置、README。
- **CI 配置保留：** `.github/workflows/ci.yml` 已保留。
- **运行验收：** 按题目要求，运行时构建、安装、测试与应用执行均在容器隔离环境进行。提供了 `docker compose up --build` 配�一键启动。
- **源码证明优先：** 完整、可靠且能解释的方案优先。由于环境或素材缺失无法验证的部分记为未验证，不直接判作未实现。
- **验收 commit：** 推送到 GitHub 后将在 README 或提交信息中注明验收 commit SHA。
