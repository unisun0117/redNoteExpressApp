# REST API 设计从0到1 第2篇：URL 设计：接口好不好看URL就知道
@[TOC](目录)
## 摘要
> URL 是 API 的门面。好的 URL 让人一看就懂，坏的 URL 让人抓狂。本文从资源命名、层级关系、版本控制三个维度，教你设计出清晰、规范的 API 地址。
## 引言
你见过这样的 URL 吗？`/api/v1/getUserInfoByUserId?userId=123`。乍一看似乎没什么问题，但多看几眼就觉得不对劲——`getUserInfoByUserId` 这么长的名字，而且 `getUser` 和 `?userId` 似乎在重复表达同一件事。

评价一个 API 设计水平，最简单的方法就是看它的 URL。好的 URL 像一篇好文章的目录，逻辑清晰、层次分明。坏的 URL 像一团乱麻，每次对接都要翻半天文档。

本篇文章将系统讲解 REST API 中 URL 的设计规范。从资源命名到层级嵌套、从版本管理到特殊场景处理，让你以后设计接口时心里有杆秤。
## 基础知识储备
- **REST 资源概念**：理解 URL 路径代表的是"资源"而非"操作"，这是第一篇讲过的核心思想。
- **HTTP 方法语义**：知道 GET 是读、POST 是增、PUT 是改、DELETE 是删。
- **层级路径概念**：理解 `/users/123/orders` 表示"用户 123 下的订单"这种父子关系。
- **URL 编码规则**：知道 URL 中特殊字符需要百分号编码，中文不能直接放在 URL 里。
- **API 版本演进意识**：理解为什么 `/api/v1/` 这样的前缀几乎出现在所有公开 API 中。
## 正文
### 1. 资源命名——名词、复数、小写

REST URL 的三个黄金法则：**名词、复数、小写**。

**为什么是名词而不是动词？** 因为操作已经由 HTTP 方法表达了。`POST /users` 就是"创建用户"，不需要写成 `POST /createUser`。记住：URL 描述的是"谁"，HTTP 方法描述的是"做什么"。

**为什么用复数？** `/users` 表示用户集合，`/users/123` 表示集合中的单个用户。用复数保持一致性，避免 `/user/123` 和 `/users` 这种混合写法。

**为什么用小写？** URL 的标准约定，`/api/users` 比 `/api/Users` 或 `/api/USERS` 更易读。部分 Web 服务器对大小写敏感，统一小写可以避免诡异的路由问题。

**为什么用短横线而不是下划线？** `/api/user-profiles` 优于 `/api/user_profiles`。这是因为下划线在带下划线的链接中会被遮住，看起来像一个空格。而且主流 SEO 引擎将短横线视为词分隔符。

```javascript
// 前端开发者的视角：URL 就是资源路径的"文件系统"
// 好设计 —— 像清晰的目录结构
GET  /api/articles           // 文章集合
GET  /api/articles/42        // 第 42 号文章
GET  /api/articles/42/comments  // 文章 42 下的评论

// 坏设计 —— 像把所有文件都扔在桌面
GET  /api/getAllArticles
GET  /api/getArticleById?id=42
GET  /api/fetchCommentsOfArticle?articleId=42
```

### 2. 层级关系——不要超过三层

资源之间经常有关联关系：用户有订单，订单有商品，商品有评论。怎么在 URL 中表达这些关系？

**基本规则：用路径层级表示"属于"关系。** 比如 `GET /users/5/orders` 表示"用户 5 的所有订单"。斜杠每多一层，就表示多一层从属关系。

**黄金法则：嵌套深度不要超过三层。** 超过三层后 URL 又长又难读，而且路由配置和维护都变得困难。

```python
# Flask 中嵌套路由的实现示例
from flask import Flask, jsonify

app = Flask(__name__)

# 不超过三层嵌套 —— 推荐做法
@app.route('/api/users/<int:user_id>/orders')
def user_orders(user_id):
    # 返回该用户的所有订单
    return jsonify({"user_id": user_id, "orders": []})

@app.route('/api/users/<int:user_id>/orders/<int:order_id>')
def user_order_detail(user_id, order_id):
    # 返回该用户的某个订单详情
    return jsonify({"user_id": user_id, "order_id": order_id})

# 如果需要深层嵌套的资源，改为独立的顶层端点
@app.route('/api/order-items/<int:item_id>')
def order_item(item_id):
    # 而不是 /api/users/5/orders/10/items/3
    return jsonify({"item_id": item_id})

if __name__ == '__main__':
    app.run(debug=True)
```

当嵌套超过三层时，应该把深层资源提升为独立的顶层路径。比如订单商品不应该写成 `/users/5/orders/10/items/3`，而是 `/order-items/3`。

### 3. 版本控制与特殊场景

**版本控制**：API 一定会演进。今天返回 3 个字段，明天要加 2 个。如果不做版本管理，改了接口就会搞崩所有老客户端。最常见的做法是在 URL 中加版本号前缀：`/api/v1/users`、`/api/v2/users`。

其他版本控制方式还有请求头版本（`Accept: application/vnd.api.v2+json`）和查询参数版本（`/api/users?version=2`），但 URL 前缀最简单直接，也最符合"一眼看懂"的原则。

**特殊场景处理：**

第一，搜索操作。搜索本质上是对资源的过滤，用查询参数而非路径：`GET /api/articles?keyword=REST&tag=api`，不要写成 `GET /api/articles/search?q=REST`。

第二，非 CRUD 操作。有些操作确实不是标准的增删改查，比如"发送验证码"、"审核通过"。这类操作可以用动词作为子资源：`POST /api/users/123/send-verification-code`、`POST /api/articles/42/approve`。

第三，批量操作。比如批量删除：`DELETE /api/users?ids=1,2,3`，或者用专门的批量端点：`POST /api/users/batch-delete` 并在 body 中传 id 数组。
## 总结
- URL 设计的三个黄金法则：名词、复数、小写。URL 表达资源，HTTP 方法表达操作。
- 层级嵌套表示资源的从属关系，但不要超过三层，深层资源应提升为独立端点。
- API 版本号建议放在 URL 前缀中，简单直观，`/api/v1/` 是业界主流做法。
- 非标准操作可以在资源路径后追加动词子资源，但要慎重使用。
- 好的 URL 设计让 API 自解释——前端开发者不翻文档也能大致猜出怎么用。
## 注意事项
- 不要把动词放在 URL 路径的末尾作为资源名（如 `/api/createUser`），非 CRUD 操作可以用子资源形式（`/users/123/activate`）。
- URL 中不要暴露内部实现细节，比如 `/api/usersFromMySQL` 或 `/api/getUserViaORM`。
- 查询参数（`?` 后面的部分）用于过滤和排序，不要用来区分资源类型。
- 避免在 URL 中传输敏感信息（如密码、token），敏感数据应当放在请求头或 POST body 中。
## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 单复数混用 | 有时 `/user/123`，有时 `/users` | 没有统一的命名规范 | 统一使用名词复数：`/users`、`/users/123` |
| 嵌套过深 | `/a/b/c/d/e/f/g` 这样的七层 URL | 把数据库外键关系直接映射到 URL | 最多三层，更深资源提升为独立端点 |
| URL 中出现动词 | `/api/searchArticles`、`/api/downloadFile` | 没有用 HTTP 方法表达操作 | GET /articles?q=xxx 做搜索，下载用 GET /files/:id/download |
| 忽略版本号 | 改了接口格式前端就报错 | 没有做 API 版本管理 | 上线第一天就加 `/v1/` 前缀，哪怕最初只有一个版本 |
