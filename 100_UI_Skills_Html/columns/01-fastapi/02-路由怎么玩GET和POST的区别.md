# FastAPI 从0到1 第2篇：路由怎么玩？GET 和 POST 的区别

@[TOC](目录)

## 摘要
> 本文带你掌握 FastAPI 路由的核心玩法，深入理解 GET 和 POST 的本质区别。适合刚入门后端的前端开发者，学完你就能写出带路径参数和查询参数的 API 接口。

## 引言

如果你写过前端，一定用过 `fetch` 或者 `axios` 发请求。你有没有好奇过：为什么获取数据用 `fetch(url)` 默认就是 GET，而提交表单要写成 `method: 'POST'`？这两种请求方式到底有什么区别？

路由是 Web 后端的骨架。每一个 API 接口就是一条路由，它决定了"用户访问哪个 URL、用什么方法，后端该执行什么逻辑"。FastAPI 把路由这件事做得极其简洁——甚至比 Express.js 还直观。

这篇文章从你熟悉的前端 `fetch` 出发，帮你彻底搞懂 FastAPI 的路由系统。读完你会发现，后端路由和你写前端路由其实是一个思路，只是换了种写法而已。

## 基础知识储备

- **HTTP 方法**：GET（获取数据）、POST（创建/提交数据）、PUT（更新）、DELETE（删除），前两种最常用。
- **URL 路径**：就是 `/api/users/123` 这种结构，和前端路由的 `/user/123` 完全是一个套路。
- **路径参数 vs 查询参数**：`/users/123` 里的 `123` 是路径参数，`/users?page=2` 里的 `page=2` 是查询参数。
- **RESTful 风格**：用 URL 标识资源，用 HTTP 方法表达操作，是一种约定俗成的 API 设计规范。

## 正文

### 最简单的路由：@app.get()

FastAPI 定义路由的方式就是加一个装饰器。对比一下前端代码你马上就懂了：

```python
from fastapi import FastAPI

app = FastAPI()

# 前端 fetch('/') → 返回首页
@app.get("/")
def home():
    return {"message": "Hello, FastAPI!"}

# 前端 fetch('/api/users') → 返回用户列表
@app.get("/api/users")
def get_users():
    return [
        {"id": 1, "name": "小明"},
        {"id": 2, "name": "小红"},
    ]
```

这和你用 React Router 定义 `<Route path="/" element={<Home />} />` 是不是很像？都是一种"路径到处理函数"的映射关系。区别在于前端路由返回的是页面组件，后端路由返回的是 JSON 数据。

### 路径参数：把数据写进 URL

```python
# 获取单个用户：/api/users/1 → {"id": 1, "name": "小明"}
@app.get("/api/users/{user_id}")
def get_user(user_id: int):
    return {"id": user_id, "name": f"用户{user_id}"}
```

前端用的时候是这样的：`fetch(\`/api/users/${userId}\`)`。路径参数是 RESTful API 的基础，用它来表示"我要操作哪个资源"是最直观的。

FastAPI 会自动做类型转换——你声明了 `user_id: int`，如果你访问 `/api/users/abc`，它会直接返回一个清晰的 422 错误，而不是在代码里炸掉。这个体验比 Express 的 `req.params.id`（永远是字符串）强太多了。

### 查询参数：URL 问号后面的东西

```python
# /api/users?page=1&size=10&keyword=小明
@app.get("/api/users")
def get_users(page: int = 1, size: int = 10, keyword: str = ""):
    return {
        "page": page,
        "size": size,
        "keyword": keyword,
    }
```

前端传参：`fetch('/api/users?page=1&size=10')`。查询参数适合分页、筛选、排序这些"不改变资源本身"的请求。

一个小技巧：如果你有很多查询参数，可以把它们抽成一个 Pydantic 类，用 `Depends()` 注入。不过那是第 3 篇的内容了，先记住有这个方向就行。

### POST 和请求体

```python
from pydantic import BaseModel

class CreateUserRequest(BaseModel):
    name: str
    age: int
    email: str

@app.post("/api/users")
def create_user(user: CreateUserRequest):
    # 假装存到数据库
    return {"id": 1, **user.model_dump()}
```

前端调用：
```javascript
fetch('/api/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: '小明', age: 25, email: 'xm@test.com' })
})
```

GET 和 POST 最核心的区别：GET 的数据拼在 URL 里（明文、有长度限制、适合查询），POST 的数据放在请求体里（可以是 JSON、表单、文件等任意格式，适合提交和创建）。

一句话记住：**查数据用 GET，写数据用 POST**。

## 总结

1. FastAPI 路由用装饰器定义，`@app.get()` / `@app.post()` 对标 HTTP 方法，简洁直观。
2. 路径参数写在 URL 里，用 `{变量名}` 声明，FastAPI 自动做类型转换。
3. 查询参数写在 URL 问号后面，通过函数参数默认值声明，适合分页和筛选。
4. POST 的数据放在请求体里，用 Pydantic 模型接收，安全且支持复杂结构。
5. 路由的本质就是"URL + HTTP 方法 → 处理函数"的映射，和前端路由思想一致。

## 注意事项

1. 路径参数和查询参数别搞混——URL 路径里的变量用路径参数，问号后面的用查询参数。
2. 同一个路径可以用不同的 HTTP 方法（GET `/users` 拿列表，POST `/users` 创建），这是 RESTful 的标准做法。
3. `@app.get()` 只能接收查询参数、路径参数，不能接收请求体。如果你需要传 JSON 数据，必须用 POST/PUT/PATCH。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 路径参数顺序搞反 | `/api/users/me` 被当成 user_id 匹配 | FastAPI 从上到下匹配路由，`/api/users/{user_id}` 在 `/api/users/me` 上面会先匹配 | 把固定路径的路由写在参数化路由上面 |
| GET 传 JSON | POST / GET 用错导致前端 axios 报错 | axios 的 GET 请求不能传 body，只有 POST 可以 | 查数据一律用 GET 传 query，写数据用 POST 传 body |
| 查询参数类型不匹配 | `/api/users?page=abc` 返回 422 | FastAPI 自动校验参数类型，传了字符串给 int 会拒绝 | 前端确保传正确的类型，或者用 `Optional[int]` 允许缺省 |
| POST 没装 pydantic | `ImportError: cannot import name 'BaseModel'` | Pydantic 是 FastAPI 的依赖，但新项目可能忘了装 | `pip install pydantic` 或者直接 `pip install fastapi[all]` |
