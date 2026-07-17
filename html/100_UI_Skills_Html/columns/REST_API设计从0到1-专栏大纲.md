# REST API 设计从 0 到 1 — 10 篇专栏大纲

> 面向人群：会调接口但不知道为什么这样设计的开发者。每篇有前端视角 + 设计原则 + 实战练习。

---

## 第 1 篇：什么是 REST？你每天都在用但可能不知道

**核心内容：**
- 你每天都在调接口：`fetch('/api/users')` 这就是 REST
- REST 的 6 个约束（用人话版）：客户端-服务器、无状态、可缓存、统一接口、分层系统、按需代码
- 为什么叫 REST：Representational State Transfer（表现层状态转移）
- RESTful vs REST-ish：99% 的"RESTful API"其实只遵守了部分约束

**前端类比：**
- REST = 你和服务器之间的"通信协议格式"
- `GET /articles/123` = 把"第 123 号文章"的资源表示传给我
- 无状态 = 每个请求自己带 token，服务器不记你是谁

**【注意】** REST 不是协议，是风格。没有"REST 认证考试"，重点是理解设计思想，不是背概念。

**Lab 1：** 打开你的浏览器 DevTools → Network 面板。随便访问一个网站，观察它的 API 请求。记录 5 个请求：URL 是什么？方法是什么？返回了什么数据？你会发现大部分接口都是 GET 请求，URL 都指向某种"资源"。

---

## 第 2 篇：URL 设计——接口好不好，看 URL 就知道

**核心内容：**
- 资源命名：用名词复数 `/users` 不是 `/getUserList`
- 层级关系：`/users/123/posts` 一目了然
- 不要动词：`POST /users` 就够了，别叫 `/createUser`
- 版本号放哪：`/api/v1/users` 还是 `/api/users?version=1`
- 好 URL vs 坏 URL 对比

**前端类比：**
- 好的 URL = 好的组件命名，一眼看出是干什么的
- `/users/123/posts` = `UserDetailPage` 里的 `PostsTab`
- `/createUser` = 一个叫 `handleButtonClick` 的函数——没说清楚干啥

**【小提示】** 设计 URL 时问自己："这个 URL 读出来，能不能猜到返回什么数据？"

**Lab 2：** 下面这些 URL 哪些好哪些不好？为什么？改成好的版本：
- `GET /api/getAllProducts`
- `POST /api/createNewOrder`
- `GET /api/users/5/orders/3/items`
- `GET /api/search?q=手机&sort=price`
- `DELETE /api/removeUser?id=123`

---

## 第 3 篇：HTTP 方法——GET/POST/PUT/DELETE 到底怎么用

**核心内容：**
- GET：获取资源（安全、幂等、可缓存）
- POST：创建资源（不安全、不幂等）
- PUT：完整替换资源（幂等）
- PATCH：部分更新资源
- DELETE：删除资源（幂等）
- 幂等性：同一个请求发 10 次，结果一样吗？

**前端类比：**
- GET = `fetch` 读数据，刷新页面不会出问题
- POST = 提交表单，刷新浏览器会提示"是否重新提交"
- PUT = `Object.assign(target, source)` 整个替换
- PATCH = `Object.assign(target, { age: 26 })` 只改部分

**【注意】** POST 不幂等：同一个 POST 发 3 次 = 创建 3 条数据。设计接口时要考虑"用户网不好重复点了怎么办"。

**Lab 3：** 设计一个"文章管理系统"的 API，写出每个接口的 HTTP 方法 + URL：
- 获取文章列表（支持分页）
- 获取单篇文章详情
- 创建新文章
- 修改文章标题
- 删除文章
- 给文章点赞（这个用 POST / PATCH / PUT？为什么？）

---

## 第 4 篇：状态码——不只是 200 和 404

**核心内容：**
- 2xx 成功：200 OK、201 Created、204 No Content
- 3xx 重定向：301 永久、302 临时
- 4xx 客户端错误：400 参数错误、401 未登录、403 没权限、404 找不到、409 冲突、422 校验失败、429 请求太频繁
- 5xx 服务端错误：500 服务器崩了、502 网关错误、503 维护中
- 状态码怎么选：一张速查表

**前端类比：**
- 200 = Promise resolved，拿到数据了
- 401 = 跳登录页，token 过期了
- 403 = 弹出"你没有权限"提示
- 422 = 表单校验失败，显示错误信息
- 500 = 弹出"服务器开小差了，请稍后重试"

**【小提示】** 后端不要所有错误都返回 200 + `{"success": false}`。用对状态码，前端拦截器才能统一处理。

**Lab 4：** 写一个 FastAPI 接口（或任何你熟悉的框架），让它返回以下状态码，然后用 Postman 或 curl 测试：
- `GET /health` → 200
- `POST /register` 用户名重复 → 409
- `GET /article/99999` 不存在 → 404
- `POST /article` 缺少必填字段 → 422
- `DELETE /article/1` 成功 → 204（没有响应体）

观察每种状态码的响应格式。

---

## 第 5 篇：请求参数——Query、Body、Path 各司其职

**核心内容：**
- Path 参数：`/users/{id}` 定位资源
- Query 参数：`/users?page=1&size=20&sort=name` 筛选/分页/排序
- Request Body：`POST /users` + `{"name":"张三"}` 提交数据
- Header 参数：`Authorization: Bearer xxx` 鉴权信息
- 什么时候用哪个？（带决策流程图）

**前端类比：**
- Path = `useParams()` 获取的 `id`
- Query = `useSearchParams()` 获取的 `?page=1`
- Body = `fetch(url, { method: 'POST', body: JSON.stringify(data) })`
- Header = `fetch(url, { headers: { Authorization: 'Bearer xxx' } })`

**【注意】** 不要把敏感信息放在 Query 参数里（`?password=123456`）。URL 会出现在日志、浏览器历史、referer header 中。

**Lab 5：** 实现一个"商品搜索 API"：
```
GET /api/v1/products?keyword=手机&category=电子&min_price=1000&max_price=5000&sort=price_asc&page=2&size=20
```
要求：所有参数都是可选的，不传就用默认值；keyword 支持模糊搜索；排序支持 price_asc、price_desc、sales_desc。

---

## 第 6 篇：响应格式——让前端开心的数据结构

**核心内容：**
- 统一响应格式：`{"code": 200, "data": {...}, "message": "ok"}`
- 列表响应格式：`{"code": 200, "data": {"items": [...], "total": 100, "page": 1, "size": 20}}`
- 错误响应格式：`{"code": 422, "message": "校验失败", "errors": [{"field": "email", "msg": "格式不正确"}]}`
- 不要返回数据库字段名：`created_at` → 前端期望 `createdAt`
- 不要返回多余数据：密码 hash、内部状态等

**前端类比：**
- 统一格式 = axios response interceptor，解构 `response.data.data`
- 列表格式 = 前端 Table 组件需要的 `{dataSource, total}`
- 字段名映射 = `JSON.parse(JSON.stringify(dbRow), replacer)`
- 隐藏敏感字段 = TypeScript 的 `Omit<User, 'password'>`

**【小提示】** 后端接口格式统一了，前端可以写一个通用 `request()` 函数，所有人 `const { data } = await request('/api/users')` 就行。

**Lab 6：** 给你现有的 TODO API 加上统一响应包装。改造前：
```json
{"id": 1, "title": "学数据库"}
```
改造后：
```json
{"code": 200, "data": {"id": 1, "title": "学数据库"}, "message": "ok"}
```
写一个 Python 函数 `success_response(data)` 和 `error_response(code, message)`，所有接口都用它们返回。前端同事表示感动。

---

## 第 7 篇：分页与过滤——数据多了怎么办

**核心内容：**
- 为什么不一次返回全部？100 万条数据谁敢全查
- 偏移分页：`LIMIT 20 OFFSET 40`（最常用）
- 游标分页：`WHERE id > last_id LIMIT 20`（大数据量、实时场景）
- 过滤参数设计：`?status=active&category=tech&created_after=2026-01-01`
- 搜索：`?q=关键词` 用 LIKE 或全文搜索

**前端类比：**
- 偏移分页 = Ant Design Table 的 `pagination: { current: 3, pageSize: 20 }`
- 游标分页 = 无限滚动，`IntersectionObserver` 触发加载更多
- 过滤参数 = Table 组件顶部的筛选表单

**【注意】** 偏移分页在数据频繁变化时会有"翻页时看到重复数据"的问题（第 1 页和第 2 页之间插入了新数据）。游标分页解决这个问题。

**Lab 7：** 改造你的 TODO API，实现：
- `GET /todos?page=1&size=5` → 返回第 1 页 5 条，附带 `total`、`page`、`size`
- `GET /todos?priority=high&completed=false` → 只返回高优先级且未完成的
- `GET /todos?q=数据库` → 模糊搜索标题含"数据库"的 TODO
- 生成 50 条假数据，测试分页翻到第 10 页是否正常

---

## 第 8 篇：错误处理——别让前端接一堆不同格式的 error

**核心内容：**
- 统一错误响应格式：不管什么错，前端解析方式一样
- 业务错误 vs 系统错误：余额不足（业务错）vs 数据库挂了（系统错）
- FastAPI 的异常处理器：`@app.exception_handler`
- 错误码设计：前端根据 `code` 决定怎么提示用户

**前端类比：**
- 统一错误格式 = axios error interceptor，`if(error.response.status === 401) logout()`
- 业务错误 vs 系统错误 = 表单校验失败（红色提示）vs 网络断了（toast 提示）
- 错误码 = 前端的 ErrorBoundary + 自定义 Error 类

**【小提示】** 后端可以定义一个 `AppException` 类，包含 `code`、`message`、`http_status`。所有业务错误 throw 这个类，全局 handler 统一捕获 → 统一格式。

**Lab 8：** 给你的 API 加上以下错误场景：
- 用户名已存在 → `409 {"code": "USER_EXISTS", "message": "该用户名已被注册"}`
- 登录密码错误 → `401 {"code": "WRONG_PASSWORD", "message": "密码错误"}`
- 余额不足 → `400 {"code": "INSUFFICIENT_BALANCE", "message": "余额不足，当前余额 ¥50"}`
- JSON 格式错误 → `400 {"code": "INVALID_JSON", "message": "请求体不是有效的 JSON"}`

确保所有错误格式一致：`{"code": "ERROR_CODE", "message": "人类可读的描述"}`。

---

## 第 9 篇：API 文档——Swagger 自动生成 + OpenAPI 规范

**核心内容：**
- Swagger UI：FastAPI 自带 `/docs`，不用手写文档
- OpenAPI 规范：描述 API 的标准格式（JSON/YAML）
- 让你的文档更好看：docstring、response_model、example
- ReDoc：另一个风格的自动文档
- 导出 OpenAPI JSON → 导入 Postman / Apifox

**前端类比：**
- Swagger UI = 交互式的 API 使用说明书
- OpenAPI 规范 = TypeScript 的类型定义文件 `.d.ts`
- `/docs` = 前后端联调的"共同语言翻译器"

**【注意】** 后端写完接口不给文档 = 前端要靠猜和反复问来联调。`/docs` 自动生成就是个举手之劳，但对前端体验天差地别。

**Lab 9：** 给你现有的所有接口加上：
- 每个接口的 `summary` 和 `description`（用 docstring）
- 请求体的 `example`（模拟真实数据）
- 响应的 `response_model`（让 Swagger 显示返回结构）
- 访问 `http://localhost:8000/docs`，确认每个接口都有完整的说明文档
- 导出 OpenAPI JSON，导入 Postman 生成 Collection

---

## 第 10 篇：从 0 到 1 设计一套 API——综合实战

**核心内容：**
- 拿到需求 → 识别资源 → 设计 URL → 选 HTTP 方法 → 定响应格式
- 实战：设计一套"在线图书馆"的完整 API
  - 图书管理（CRUD）
  - 借阅管理（借书/还书）
  - 用户管理（注册/登录/个人信息）
  - 搜索 + 分页
- API 设计评审清单
- 前后端联调时的撕逼预防指南

**前端类比：**
- API 设计 = 前后端的"施工合同"
- 后端先出接口文档 → 前端可以 mock 数据并行开发
- 联调 = 把前端的 mock 换成真实接口，90% 的问题来自接口格式不一致

**【小提示】** 前后端分离项目最关键的文档不是需求文档，是 API 文档。需求文档可以模糊，接口格式必须精确。

**Lab 10（综合实战）：** 设计"在线图书馆"的完整 API，产出：
1. 资源列表（有哪些实体）
2. 每个资源的 URL + HTTP 方法 + 请求/响应格式
3. 统一错误码定义
4. 用 FastAPI 实现其中 3 个核心接口
5. 用 Swagger UI 展示完整文档
6. 前端同事拿到这份文档就能并行开发（你可以写个简单的 HTML 页面验证）

---

## 配套 Lab 答案

```
labs/rest-api/
├── lab01-observe/          ← 浏览器抓包观察
├── lab02-url-design/       ← 好的URL vs 坏的URL
├── lab03-http-methods/     ← 文章系统接口设计
├── lab04-status-codes/     ← 状态码实践
├── lab05-query-params/     ← 商品搜索接口
├── lab06-response-format/  ← 统一响应包装
├── lab07-pagination/       ← 分页+过滤+搜索
├── lab08-error-handling/   ← 统一错误处理
├── lab09-swagger/          ← Swagger文档
└── lab10-library-api/      ← 在线图书馆综合实战
```
