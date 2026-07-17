# REST API 设计从0到1 第1篇：什么是 REST？你每天都在用但可能不知道
@[TOC](目录)
## 摘要
> REST 不是协议，而是一套架构风格。它规定了客户端和服务器之间该怎么"对话"。理解 REST 的六个约束条件，是设计好 API 的第一步。本文用前端的视角帮你彻底搞懂 REST 的本质。
## 引言
你可能每天都在调接口：`fetch('/api/users')`、`axios.post('/api/login')`……但你有没有想过，为什么这些接口长这样？为什么有的用 GET、有的用 POST？为什么 URL 里大多是名词而不是动词？

这些约定的背后，就是 REST。REST 的全称是 Representational State Transfer，翻译过来叫"表述性状态转移"。这个名字听起来很唬人，但它本质上就是一套客户端和服务器之间通信的风格约定。

理解 REST 不是为了考试，而是让你在设计或调用 API 时有一个清晰的思维框架。当你知道为什么 URL 里不用动词、为什么 GET 请求不应该修改数据时，你写的代码就会更规范、更不容易出 bug。
## 基础知识储备
- **HTTP 协议基础**：了解 HTTP 是应用层协议，基于请求-响应模型。
- **客户端-服务器架构**：知道浏览器/app 是客户端，后端服务是服务器，两者通过网络通信。
- **JSON 数据格式**：理解键值对的结构化数据表示方式，这是 REST API 最常用的交互格式。
- **URL 基本概念**：知道什么是资源路径、什么是查询参数。
- **无状态概念**：理解每次 HTTP 请求是独立的，服务器不会自动记住上一次请求的信息。
## 正文
### 1. REST 的六个约束条件

REST 的定义者 Roy Fielding 在 2000 年的博士论文中提出了六个约束条件。一个 API 如果满足了这些约束，就可以称为 RESTful API。

**约束一：客户端-服务器分离**。前端负责界面展示，后端负责数据存储和业务逻辑。两者独立演进，互不干扰。这就是为什么你可以把 React 前端换成 Vue，而后端完全不需要改动。

**约束二：无状态**。服务器不保存客户端的会话状态。每个请求必须包含所有必要的信息，比如认证 token。这也解释了为什么每次调接口都要带 `Authorization` 头。

**约束三：可缓存**。服务器的响应应该标明自己是否可以被缓存。比如 `Cache-Control: max-age=3600` 告诉浏览器"这个数据一小时内不会变，你可以缓存起来"。

**约束四：统一接口**。这是 REST 最核心的约束。它要求：用 URL 标识资源，用 HTTP 方法表示对资源的操作，用状态码表示操作结果。

**约束五：分层系统**。客户端不需要知道自己是直接连的服务器，还是经过了负载均衡器、反向代理。每一层只关心与相邻层的交互。

**约束六：按需代码（可选）**。服务器可以向客户端发送可执行代码，比如 JavaScript。这是唯一一个可选的约束。

### 2. 资源 vs 操作：REST 的核心思想

用一个前端开发者最容易理解的类比来说明：

如果把 API 比作一个对象，那么 URL 就是对象的属性名，HTTP 方法就是你在对这个属性做什么操作。看下面这个对比：

```javascript
// 面向对象编程中：
const userStore = {
  users: [],
  getUsers() { return this.users; },
  addUser(user) { this.users.push(user); },
  updateUser(id, data) { /* ... */ },
  deleteUser(id) { /* ... */ }
};

// REST 就是把这种思维映射到 HTTP 协议上：
// GET    /api/users      → 获取用户列表（读）
// POST   /api/users      → 创建新用户（增）
// PUT    /api/users/123  → 更新用户 123（改）
// DELETE /api/users/123  → 删除用户 123（删）
```

注意一个关键点：URL 里用的是名词 `users`，而不是动词 `getUsers` 或 `createUser`。操作由 HTTP 方法来表达，资源由 URL 来表达。这种"名词 + 动词"的分离就是 REST 统一接口的核心思想。

### 3. RESTful vs 非 RESTful：一眼就能看出来

来看一个实际的对比。假设我们要实现用户管理功能：

**非 RESTful 设计（常见于老系统）：**
```
POST /api/getUserList
POST /api/createNewUser
POST /api/updateUserInfo
POST /api/deleteUserById
```
这种设计的典型特征：所有请求都用 POST，URL 里全是动词。每次新增功能就新增一个 URL，接口数量会爆炸式增长。

**RESTful 设计：**
```
GET    /api/users
POST   /api/users
PUT    /api/users/:id
DELETE /api/users/:id
```
只用了 4 个 URL 就覆盖了所有操作。新增功能时（比如查询单个用户），只需要 `GET /api/users/:id`，语义清晰，前端开发者一看就懂。

```python
# 一个极简的 Flask REST API 示例
from flask import Flask, request, jsonify

app = Flask(__name__)
users = [{"id": 1, "name": "张三", "email": "zhangsan@example.com"}]

@app.route('/api/users', methods=['GET'])
def list_users():
    return jsonify(users)

@app.route('/api/users', methods=['POST'])
def create_user():
    user = request.json
    user['id'] = len(users) + 1
    users.append(user)
    return jsonify(user), 201

@app.route('/api/users/<int:user_id>', methods=['GET'])
def get_user(user_id):
    user = next((u for u in users if u['id'] == user_id), None)
    return jsonify(user) if user else ('Not Found', 404)

if __name__ == '__main__':
    app.run(debug=True)
```

你可以把这个代码复制到本地跑起来，然后用 Postman 或 curl 亲自测试各个接口，直观感受 RESTful 风格。
## 总结
- REST 是一套架构风格，不是协议。满足六个约束的 API 就是 RESTful API。
- 核心思想是"用 URL 标识资源，用 HTTP 方法表达操作"，就像面向对象中的"对象 + 方法"。
- 一个好的 REST API 应该让前端开发者不看文档也能大致猜出怎么用。
- 无状态设计让系统更容易扩展，但也意味着每次请求都要携带认证信息。
- 理解 REST 理念后，设计 API 不再是"想到哪写到哪"，而是有章可循。
## 注意事项
- REST 是风格而非标准：没有权威机构来认证你的 API 是否"RESTful"，它是一个程度问题。
- 不要追求百分百 RESTful：实际业务中可能需要做一些妥协，比如用 POST 做搜索（因为 GET 请求不能带 body）。
- URL 中的资源名始终用名词复数形式，这是一个强烈推荐但非强制的约定。
- 区分"资源"和"集合"：`/users` 是集合，`/users/123` 是集合中的单个资源。
## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| URL 里用动词 | `/api/getUsers`、`/api/createOrder` 满天飞 | 不理解"HTTP 方法即操作"的思想 | URL 只用名词，GET/POST/PUT/DELETE 表达操作 |
| GET 请求修改数据 | GET /api/delete/user/123 就删除了用户 | 不知道 GET 应该是安全且幂等的 | 删除用 DELETE，修改用 PUT/PATCH |
| 所有请求用 POST | 不管增删改查全部 POST，URL 区分操作 | 老习惯或者觉得 POST 更"安全" | 严格按 HTTP 语义使用对应方法 |
| 忽略缓存 | 每次刷新页面都重新请求相同数据 | 没在响应头中设置缓存策略 | 对不常变化的数据设置 Cache-Control 头 |
