# FastAPI 从 0 到 1 — 10 篇专栏大纲

> 面向人群：有前端基础、想学后端开发的程序员。每篇包含前端类比 + 可运行的代码 + Lab 习题。

---

## 第 1 篇：这是什么？为什么前端要学它？

**核心内容：**
- 后端到底在做什么？用你熟悉的 `fetch` 请求来解释
- FastAPI vs Flask vs Django：为什么 2026 年首选 FastAPI
- 自动生成 API 文档：`/docs` 到底多香
- 5 分钟跑起第一个 FastAPI 服务

**前端类比：**
- FastAPI 的路由 = React Router 的 `<Route>`
- Pydantic 模型 = TypeScript 的 `interface`
- `/docs` = 不需要 Postman，框架自带 Swagger

**Lab 1：** 装好 Python 环境，跑起来一个返回 `{"message": "Hello, 后端世界"}` 的接口，浏览器访问 `http://localhost:8000/docs` 看到自动生成的文档页面，截图留念（这是你后端之路的起点）。

---

## 第 2 篇：路由怎么玩？GET 和 POST 的区别

**核心内容：**
- 路由装饰器 `@app.get()` / `@app.post()` 的本质
- 路径参数 vs 查询参数的区别
- 用 `curl` 或浏览器测试接口
- REST 风格 URL 设计初体验

**前端类比：**
- `@app.get("/users")` = 前端写 `router.get("/users", handler)`
- 查询参数 `?page=1&size=10` = URLSearchParams
- 路径参数 `/users/123` = React Router 的 `:id`

**Lab 2：** 写 4 个接口：`GET /todos`（返回列表）、`GET /todos/{id}`（返回单条）、`POST /todos`（新增）、`DELETE /todos/{id}`（删除）。数据先存内存列表。用 `/docs` 页面测试每个接口。

---

## 第 3 篇：请求体怎么定义？认识 Pydantic

**核心内容：**
- 为什么需要数据校验？后端不接受"随便传什么都行"
- Pydantic BaseModel：定义请求体的"形状"
- 字段类型：`str`、`int`、`bool`、`Optional`、`EmailStr`
- FastAPI 自动校验 + 自动生成文档示例

**前端类比：**
- Pydantic Model = TypeScript `interface`
- `Optional[str]` = `string | undefined`
- 自动校验 = 前端表单的 `required` + `pattern`

**Lab 3：** 给 Lab 2 的 TODO 接口加上 Pydantic 模型。定义 `TodoCreate`（title、description、priority）和 `TodoResponse`（多了 id 和 created_at）。试着 `POST` 一个缺少 title 的数据，观察 FastAPI 自动返回的 422 错误信息有多详细。

---

## 第 4 篇：数据真的存下来了——SQLite + SQLAlchemy 入门

**核心内容：**
- 为什么不能一直存内存？重启服务数据就没了
- SQLite：一个文件就是数据库，零配置
- SQLAlchemy ORM：写 Python 类 = 建数据库表
- CRUD 操作：从内存版改成数据库版

**前端类比：**
- SQLite = 浏览器的 localStorage，但强 1000 倍
- SQLAlchemy Model = 定义 `localStorage` 的 key 结构
- 数据库文件 `app.db` = 前端的 `indexedDB`

**Lab 4：** 把 Lab 3 的 TODO 从内存列表改成 SQLite 存储。重启服务 → 数据还在。写一个 `GET /todos?priority=high` 接口，筛选高优先级 TODO。提示：用 `db.query(Todo).filter(Todo.priority == "high").all()`。

---

## 第 5 篇：项目结构——不止一个 `main.py`

**核心内容：**
- 真实项目不会把所有代码塞一个文件
- FastAPI 推荐项目结构：routers、models、schemas、services
- `APIRouter`：拆分路由到不同文件
- 依赖注入 `Depends()`：共享数据库连接

**前端类比：**
- 项目结构 = `src/pages/`、`src/components/`、`src/hooks/`
- `APIRouter` = React Router 的嵌套路由
- `Depends(get_db)` = React Context Provider 注入全局状态

**Lab 5：** 重构你的 TODO 项目，拆成标准的 FastAPI 项目结构：
```
backend/
├── main.py          # 入口 + app 创建
├── database.py      # 数据库连接
├── models.py        # SQLAlchemy 模型
├── schemas.py       # Pydantic 请求/响应模型
└── routers/
    └── todos.py     # TODO 路由
```
确保拆分后功能跟 Lab 4 一模一样。

---

## 第 6 篇：错误处理——别让用户看到 500

**核心内容：**
- HTTPException：主动抛错
- 自定义异常处理器
- 统一响应格式：`{"code": 200, "data": ..., "message": "success"}`
- 常见错误码：400（参数错误）、404（找不到）、409（冲突）、422（校验失败）

**前端类比：**
- HTTPException = `throw new Error()` + axios interceptor 统一处理
- 统一响应格式 = 前端封装的 `ajaxWrapper`，所有请求返回同样结构
- 状态码 = HTTP 响应的 `status` 字段

**Lab 6：** 给 TODO API 加上完整的错误处理：
- 查不存在的 TODO → 返回 404 `{"message": "TODO不存在"}`
- 创建重复的 TODO（同标题）→ 返回 409
- 所有接口统一响应格式 `{"code": 200, "data": {...}}`
- 写一个自定义的 `GET /todos/{id}/complete` 接口，如果 TODO 已完成就抛 400

---

## 第 7 篇：文件上传——图片和附件怎么处理

**核心内容：**
- `UploadFile`：FastAPI 处理文件上传
- 文件大小限制、类型校验
- 静态文件服务：`StaticFiles` 挂载
- 存储策略：本地 vs OSS（简单介绍）

**前端类比：**
- `UploadFile` = `<input type="file">` + `FormData`
- 静态文件服务 = Vite 的 `public/` 目录
- 文件类型校验 = `accept="image/*"` 但后端校验才真正安全

**Lab 7：** 给每个 TODO 支持上传一张配图。实现：
- `POST /todos` 改为 `Form` 提交（`title`、`description` + `image` 文件）
- 图片存到 `uploads/` 目录
- `GET /todos` 返回的图片 URL 能直接在浏览器打开
- 限制图片最大 2MB，只允许 jpg/png

---

## 第 8 篇：中间件——请求的收费站

**核心内容：**
- 中间件是什么：每个请求都要经过的"关卡"
- CORS 中间件：为什么前端调后端会跨域
- 自定义中间件：请求日志、耗时统计
- 中间件的执行顺序

**前端类比：**
- 中间件 = axios interceptor，每个请求自动经过
- CORS = 浏览器的同源策略 + 后端 `Access-Control-Allow-Origin`
- 中间件链 = Express/Koa 的洋葱模型
- 请求日志 = 前端的 `console.log(request.url, Date.now())`

**Lab 8：** 实现 3 个中间件：
1. **请求日志中间件**：打印每个请求的 `{method} {url} - {耗时}ms`
2. **简易限流中间件**：同一个 IP 1 分钟内最多请求 60 次，超过返回 429
3. **CORS 中间件**：配置允许前端的 `localhost:5173` 跨域访问

---

## 第 9 篇：测试——你的代码靠得住吗？

**核心内容：**
- 为什么要写测试？改了代码怎么确认没改坏
- `TestClient`：FastAPI 内置的测试客户端
- 写第一个测试：测试创建 TODO 接口
- 测试数据库：用独立测试数据库，不污染开发数据
- pytest fixture：复用测试环境

**前端类比：**
- `TestClient` = 前端的 `jest` + `@testing-library/react`
- fixture = `beforeEach` 中准备测试数据
- 测试数据库 = 前端测试用的 mock server

**Lab 9：** 给 TODO API 写测试。覆盖：
- ✅ 创建 TODO 成功（201）
- ✅ 创建 TODO 缺少 title 返回 422
- ✅ 获取 TODO 列表（200，列表不为空）
- ✅ 删除不存在的 TODO 返回 404
- ✅ 获取单个 TODO（200，title 正确）
跑 `pytest`，看到 5 个绿点。

---

## 第 10 篇：接下来学什么？后端进阶路线图

**核心内容：**
- 10 篇回顾：你学会了什么
- 进阶路线图：用户认证 → 数据库高级 → 部署 → 微服务
- FastAPI 生态推荐：SQLModel、Alembic、Celery、Docker
- 真实项目推荐：把你前端的 TODO App 加上 FastAPI 后端，变成一个全栈项目
- 后端面试会问什么

**前端类比：**
- FastAPI 生态 = React 生态（Next.js / Zustand / React Query）
- 全栈项目 = 前端 + 后端 + 数据库 三件套跑通
- 后端面试 = 算法 + 数据库设计 + 系统设计

**Lab 10（综合实战）：** 做一个完整的「任务管理系统」：
- 支持创建/编辑/删除/完成/筛选任务
- 支持用户注册登录（用第 4 系列的 JWT 知识）
- 前后端分离：FastAPI 后端 + 你熟悉的前端框架前端
- 所有接口有测试
- 这就是你的全栈毕业设计 🎓

---

## 配套：每篇 Lab 答案

每篇的 Lab 答案，放在 `labs/fastapi/` 目录下，包含可以直接运行的完整代码。

```
labs/fastapi/
├── lab01-hello-world/
├── lab02-crud-routes/
├── lab03-pydantic/
├── lab04-sqlite/
├── lab05-project-structure/
├── lab06-error-handling/
├── lab07-file-upload/
├── lab08-middleware/
├── lab09-testing/
└── lab10-fullstack/
```
