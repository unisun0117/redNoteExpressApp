# REST API 设计从0到1 第3篇：HTTP 方法：GET/POST/PUT/DELETE 详解
@[TOC](目录)
## 摘要
> HTTP 方法是 REST API 的"动词"。搞不清楚 GET 和 POST 的区别、不知道 PUT 和 PATCH 该用哪个？本文彻底讲透四种主要 HTTP 方法的使用场景、安全性与幂等性，附带可运行的代码示例。
## 引言
如果你问一个前端开发者："GET 和 POST 有什么区别？"最常见的回答是："GET 参数在 URL 里，POST 在 body 里。"这个回答对不对？对，但不完整。更重要的是，它们背后有不同的语义约定和安全性要求。

REST API 之所以简洁，很大程度上是因为它复用了 HTTP 协议内置的"动词系统"。就像 SQL 的核心就四个操作（SELECT/INSERT/UPDATE/DELETE）一样，REST 的核心也是四个 HTTP 方法：GET、POST、PUT、DELETE。

本篇文章不仅要讲清楚每个方法怎么用，还会深入两个关键概念：**安全性**和**幂等性**。这两个概念是区分 HTTP 方法的试金石，也是面试中的高频考点。
## 基础知识储备
- **CRUD 概念**：Create（增）、Read（查）、Update（改）、Delete（删），这四个操作几乎涵盖所有业务。
- **HTTP 协议基础**：了解请求由方法、URL、请求头、请求体组成。
- **JSON 处理能力**：能用 JSON 格式构造请求体，也能解析返回的 JSON 数据。
- **Postman 或 curl 使用经验**：能亲手发送不同类型的 HTTP 请求来验证接口行为。
- **数据库基本操作概念**：理解增删改查在数据库层面意味着什么。
## 正文
### 1. 四种核心方法：一张表看懂

REST API 的核心操作用四个 HTTP 方法就能覆盖：

| HTTP 方法 | CRUD 对应 | URL 示例 | 含义 | 安全性 | 幂等性 |
|-----------|----------|----------|------|--------|--------|
| GET | Read | GET /users | 获取用户列表 | 安全 | 幂等 |
| POST | Create | POST /users | 创建新用户 | 不安全 | 不幂等 |
| PUT | Update | PUT /users/1 | 完整更新用户1 | 不安全 | 幂等 |
| DELETE | Delete | DELETE /users/1 | 删除用户1 | 不安全 | 幂等 |

**安全性**指的是请求会不会修改服务器上的数据。GET 是安全的（只读），POST/PUT/DELETE 是不安全的（会写）。注意这里的安全跟 HTTPS 的加密安全是两码事。

**幂等性**指的是同一个请求执行一次和执行多次，结果是否一样。GET、PUT、DELETE 是幂等的——执行 100 次 GET /users/1 结果都一样，执行 100 次 DELETE /users/1 也只有第一次真正删除了数据。POST 是不幂等的——每次 POST /users 都会创建一个新用户。

### 2. 每个方法深入解析

**GET：获取资源。** GET 是最常用的方法，用于读取数据。请求参数放在 URL 的查询字符串中。GET 请求不应该有请求体（虽然 HTTP 协议没有禁止，但绝大多数框架和代理服务器不支持或会忽略 GET 请求的 body）。

```python
# Flask 中四种核心方法的完整实现
from flask import Flask, request, jsonify

app = Flask(__name__)
articles = [
    {"id": 1, "title": "REST API 入门", "content": "..."},
    {"id": 2, "title": "Flask 实战", "content": "..."},
]

@app.route('/api/articles', methods=['GET'])
def list_articles():
    """GET: 获取文章列表，安全且幂等"""
    return jsonify({"data": articles, "total": len(articles)})

@app.route('/api/articles/<int:article_id>', methods=['GET'])
def get_article(article_id):
    """GET: 获取单篇文章，安全且幂等"""
    article = next((a for a in articles if a['id'] == article_id), None)
    if article:
        return jsonify({"data": article})
    return jsonify({"error": "文章不存在"}), 404
```

**POST：创建资源。** POST 用于向服务器提交新数据。数据放在请求体中（通常是 JSON 格式）。POST 不是幂等的——连续发送两次相同的 POST 请求，会创建两条一模一样的数据（可能除了 id 不同）。这也是为什么浏览器会提示"是否重新提交表单"。

```python
@app.route('/api/articles', methods=['POST'])
def create_article():
    """POST: 创建新文章，不安全且不幂等"""
    data = request.json
    if not data or 'title' not in data:
        return jsonify({"error": "title 字段必填"}), 400
    new_article = {
        "id": len(articles) + 1,
        "title": data['title'],
        "content": data.get('content', ''),
    }
    articles.append(new_article)
    return jsonify({"data": new_article}), 201
```

**PUT：完整更新资源。** PUT 要求你提供资源的完整表示，它会用你提供的数据**整体替换**服务器上的资源。如果你只传了 title 没传 content，那么 content 就会被清空。PUT 是幂等的——相同的更新数据执行多少次结果都一样。

```python
@app.route('/api/articles/<int:article_id>', methods=['PUT'])
def update_article(article_id):
    """PUT: 完整更新文章，需要提供所有字段"""
    data = request.json
    article = next((a for a in articles if a['id'] == article_id), None)
    if not article:
        return jsonify({"error": "文章不存在"}), 404
    # PUT 会用新数据完全替换旧数据
    article['title'] = data.get('title', '')
    article['content'] = data.get('content', '')
    return jsonify({"data": article})
```

**PATCH：部分更新（补充方法）。** PATCH 不在四大核心方法中，但实际开发中很常用。它用于部分更新——只传你想改的字段。PATCH 不是幂等的（虽然可以实现为幂等）。

**DELETE：删除资源。** DELETE 用于删除指定资源。删除成功后通常返回 204 No Content 或 200 OK。DELETE 是幂等的——删一次和删十次，最终都是"资源不存在"。

```python
@app.route('/api/articles/<int:article_id>', methods=['DELETE'])
def delete_article(article_id):
    """DELETE: 删除文章，不安全但幂等"""
    global articles
    original_len = len(articles)
    articles = [a for a in articles if a['id'] != article_id]
    if len(articles) < original_len:
        return '', 204  # 删除成功，无返回内容
    return jsonify({"error": "文章不存在"}), 404

if __name__ == '__main__':
    app.run(debug=True)
```

### 3. 前端如何正确使用这些方法

```javascript
// 前端调用示例（使用 fetch API）

// GET —— 获取数据
const articles = await fetch('/api/articles').then(r => r.json());

// POST —— 创建数据，注意 headers 和 body
const newArticle = await fetch('/api/articles', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ title: '新文章', content: '内容...' })
}).then(r => r.json());

// PUT —— 完整更新，必须传所有字段
await fetch('/api/articles/1', {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ title: '修改后的标题', content: '修改后的内容' })
});

// PATCH —— 部分更新，只传要改的字段
await fetch('/api/articles/1', {
  method: 'PATCH',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ title: '只改标题' })
});

// DELETE —— 删除
await fetch('/api/articles/1', { method: 'DELETE' });
```

注意一个常见错误：HTML 表单只支持 GET 和 POST 两种方法。用纯 HTML 表单无法发送 PUT 或 DELETE 请求。前端框架（React/Vue/Angular）通过 JavaScript 的 fetch 或 axios 可以发送所有 HTTP 方法，所以这不是问题。但如果你还在用 jQuery 的 `$.ajax`，记得设置 `type` 而不是 `method`。
## 总结
- GET 用于读取，安全且幂等。POST 用于创建，不安全且不幂等。
- PUT 用于完整替换，DELETE 用于删除。两者都不安全但幂等。
- 安全性不等于幂等性：安全意味着不修改数据，幂等意味着多次执行结果相同。
- PATCH 用于部分更新，在需要只修改个别字段时比 PUT 更实用。
- 前端使用这些方法时，除了 GET 以外都需要注意设置 `Content-Type` 请求头。
## 注意事项
- 不要把 GET 请求用于修改数据的操作。搜索引擎爬虫、浏览器预加载都可能触发 GET 请求，如果是删除操作，后果不堪设想。
- POST 可以用于任何非标准操作（搜索、批量处理等），它是最灵活的方法。但能用 GET/PUT/DELETE 表达的操作，优先用对应方法。
- PUT 和 PATCH 的区别：PUT 是"给你一份新数据，整体替换"，PATCH 是"告诉你改哪里，部分修改"。
- 浏览器限制：HTML 表单只支持 GET 和 POST，PUT/DELETE 需要通过 JavaScript 或框架的 `_method` 参数来模拟。
## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| GET 请求删数据 | 搜索引擎爬虫触发 GET `/api/delete/123`，数据被意外删除 | 用 GET 做写操作 | 删除操作必须用 DELETE 方法，GET 只用于读取 |
| POST 重复提交 | 网速慢时用户多点了几次提交，创建了多条相同数据 | POST 不幂等 | 前端做防重复点击 + 后端用唯一键去重 |
| PUT 只传部分字段 | 用 PUT 更新用户名后，其他字段全被清空了 | PUT 要求完整替换 | 用 PUT 时传所有字段，或改用 PATCH 做部分更新 |
| 忘记 Content-Type | POST 请求后端收不到数据 | 没设 Content-Type: application/json | 使用 JSON 格式的请求体时一定要设置 Content-Type 头 |
