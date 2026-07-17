# REST API 设计从0到1 第5篇：请求参数：Query/Body/Path 各司其职
@[TOC](目录)
## 摘要
> 请求参数放 URL 里还是 Body 里？Query 参数和 Path 参数有什么区别？这些看似细小的问题，却决定了 API 的易用性和安全性。本文帮你彻底理清三种参数类型的适用场景和最佳实践。
## 引言
前端调接口时有三种方式传递参数：`/users/123`（Path 参数）、`/users?page=1&size=10`（Query 参数）和 `{"name": "张三"}`（Body 参数）。很多初级开发者搞不清楚什么参数该放哪里，经常把 body 里的数据塞到 query 里，或者把 path 里该有的东西放到 body 里。

参数位置的混乱会导致一系列问题：GET 请求的 URL 超长被截断、敏感信息暴露在服务器日志中、缓存策略失效、接口无法被搜索引擎正确索引……这些问题排查起来很痛苦，但实际上只要在设计阶段遵循几条简单的规则就能避免。

本篇文章从设计原则出发，结合代码示例，让你彻底掌握 REST API 中三种参数的使用规范。
## 基础知识储备
- **URL 结构认知**：了解 URL 由协议、域名、路径（Path）、问号、查询字符串（Query）组成。
- **HTTP 请求结构**：知道请求由请求行（含方法和路径）、请求头、请求体组成。
- **JSON 格式**：能构造和解析 JSON 格式的请求体。
- **GET 和 POST 的区别**：了解 GET 没有请求体、POST 有请求体的基本差异。
- **数据验证意识**：知道"永远不要信任客户端传来的数据"，所有参数都需要后端验证。
## 正文
### 1. 三种参数的位置与职责

这三种参数在 URL 中处于不同位置，职责也完全不同：

**Path 参数（路径参数）**：放在 URL 的路径中，用于标识资源。`/api/users/123` 中的 `123` 就是 Path 参数。它的核心职责是"定位到哪个资源"。Path 参数一定是必填的——如果 `/api/users/:id` 中不传 id，路由根本匹配不到。

**Query 参数（查询参数）**：放在 URL 的问号后面，用于过滤、排序、分页等辅助操作。`/api/users?page=1&size=10&sort=name` 中的 `page`、`size`、`sort` 都是 Query 参数。Query 参数的核心职责是"对资源做附加操作"，通常是可选的。

**Body 参数（请求体参数）**：放在请求体中，用于传递创建或更新的数据。`POST /api/users` 时 body 里的 `{"name":"张三"}` 就是 Body 参数。Body 参数的核心职责是"传递数据的内容"。

```javascript
// 前端开发者视角：三种参数的直观对比
const userId = 123;
const queryParams = { page: 1, size: 10, sort: 'name' };
const bodyData = { name: '张三', email: 'zhangsan@example.com' };

// GET: 用 Path 定位 + Query 过滤
// 等价于 GET /api/users/123/orders?page=1&size=10
fetch(`/api/users/${userId}/orders?${new URLSearchParams(queryParams)}`);

// POST: 用 Path 定位集合 + Body 传递数据
// 等价于 POST /api/users   body: {...}
fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(bodyData)
});

// PUT: 用 Path 定位单个资源 + Body 传递完整新数据
// 等价于 PUT /api/users/123   body: {...}
fetch(`/api/users/${userId}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(bodyData)
});
```

### 2. 参数校验：永远不要信任客户端

无论参数从哪里来，后端都必须做验证。前端也做验证，但前端的验证是为了用户体验（及时提示），后端的验证是为了数据安全（防御恶意请求）。

```python
from flask import Flask, request, jsonify
from email_validator import validate_email, EmailNotValidError
import re

app = Flask(__name__)

# 模拟数据库
users_db = []

@app.route('/api/users/<int:user_id>', methods=['GET'])
def get_user(user_id):
    """Path 参数：Flask 自动做类型转换和校验"""
    # <int:user_id> 确保必须是整数，否则返回 404
    user = next((u for u in users_db if u['id'] == user_id), None)
    if not user:
        return jsonify({"error": "用户不存在"}), 404
    return jsonify({"data": user})

@app.route('/api/users', methods=['GET'])
def list_users():
    """Query 参数：需要手动校验"""
    page = request.args.get('page', 1, type=int)
    size = request.args.get('size', 10, type=int)

    # 校验：page 必须大于 0，size 必须在 1-100 之间
    if page < 1:
        return jsonify({"error": "page 必须大于 0"}), 400
    if size < 1 or size > 100:
        return jsonify({"error": "size 必须在 1-100 之间"}), 400

    start = (page - 1) * size
    end = start + size
    result = users_db[start:end]
    return jsonify({"data": result, "page": page, "size": size, "total": len(users_db)})

@app.route('/api/users', methods=['POST'])
def create_user():
    """Body 参数：需要校验每个字段"""
    data = request.json

    # 必填字段检查
    if not data:
        return jsonify({"error": "请求体不能为空"}), 400
    if 'name' not in data:
        return jsonify({"error": "name 字段必填"}), 400
    if 'email' not in data:
        return jsonify({"error": "email 字段必填"}), 400

    # 格式检查
    if len(data['name']) < 2 or len(data['name']) > 50:
        return jsonify({"error": "name 长度必须在 2-50 之间"}), 400

    # 邮箱格式检查
    try:
        validate_email(data['email'])
    except EmailNotValidError:
        return jsonify({"error": "邮箱格式不正确"}), 400

    # 唯一性检查
    if any(u['email'] == data['email'] for u in users_db):
        return jsonify({"error": "该邮箱已被注册"}), 422

    new_user = {
        "id": len(users_db) + 1,
        "name": data['name'],
        "email": data['email'],
    }
    users_db.append(new_user)
    return jsonify({"data": new_user}), 201

if __name__ == '__main__':
    app.run(debug=True)
```

这段代码展示了三种参数位置对应的三种校验模式：Path 参数由框架自动校验（类型转换），Query 参数需要手动做范围校验，Body 参数需要逐字段校验内容。

### 3. 特殊场景：搜索、文件上传、批量操作

**搜索**：搜索条件复杂多变，用 Query 参数最合适：`GET /api/articles?keyword=REST&tag=python&date_from=2024-01-01`。如果搜索条件特别复杂（十几个字段、嵌套条件），GET 的 URL 会非常长，这时可以用 POST + Body 来做搜索，但这是 REST 规范允许的少数"妥协"场景之一。

**文件上传**：文件只能用 Body 参数传递（multipart/form-data 格式），同时可以在同一个请求中附带其他字段。

**敏感数据**：密码、身份证号、银行卡号等敏感信息，绝对不要放在 URL 的 Query 参数中。URL 会被记录在服务器日志、浏览器历史、代理服务器日志中。敏感数据一律放在 Body 中，并用 POST 方法发送。
## 总结
- Path 参数用于"定位资源"（必填），Query 参数用于"过滤/排序/分页"（可选），Body 参数用于"传递数据内容"。
- 所有参数都需要后端校验，且后端校验的标准要比前端更严格，因为客户端不可信。
- 敏感信息（密码、token、身份证号等）绝对不能放在 URL 里，必须放在 Body 中。
- GET 请求理论上有 URL 长度限制（浏览器约 2000 字符），非常复杂的查询可以考虑 POST + Body 的折中方案。
- 参数校验失败时返回 400 Bad Request，并明确告诉客户端哪个字段有问题。
## 注意事项
- Query 参数的值始终是字符串类型，后端需要做类型转换。`?page=1` 中的 1 是字符串 `"1"`，不是数字 1。
- URL 中的中文和特殊字符必须做百分号编码。`fetch` 和 `axios` 会自动处理，但手动拼接 URL 时要记得编码。
- Body 参数的 `Content-Type` 一定要设置正确。JSON 格式是 `application/json`，表单格式是 `application/x-www-form-urlencoded`，文件上传是 `multipart/form-data`。
- 不要在 Query 参数中传递数组时用 `?ids=[1,2,3]` 这种格式，应该用 `?ids=1&ids=2&ids=3`，大多数框架能自动解析为数组。
## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 敏感信息出现在 URL 中 | 密码明文出现在服务器日志和浏览器历史 | 把密码等敏感数据放在 Query 参数 | 敏感数据都放在 POST Body 中传输 |
| Query 参数未做类型转换 | `?page=1` 在后端变成了字符串 `"1"`，`"1" * 10 = "1111111111"` | 忘记 Query 参数全是字符串 | 后端 `int(request.args.get('page'))` 显式转换 |
| Body 参数没校验 | 数据库被插入了奇怪的数据 | 信任了客户端传来的所有数据 | 每个字段都要做类型、长度、格式、范围校验 |
| URL 超长被截断 | 复杂搜索条件导致 URL 超过 2000 字符 | GET URL 有长度限制 | 复杂查询用 POST + Body 替代 GET + Query |
