# REST API 设计从0到1 第10篇：综合实战：在线图书馆 API 设计
@[TOC](目录)
## 摘要
> 前 9 篇文章学了那么多理论，最后来一次综合实战。本文从零开始设计一个"在线图书馆"REST API，涵盖 URL 设计、CRUD 操作、分页搜索、错误处理、Swagger 文档，用一个完整的项目串联所有知识点。
## 引言
学完前面 9 篇文章，你已经掌握了 REST API 设计的理论知识。但知识如果不落到实战上，就像学完游泳理论课却没下过水——看似都懂，真动手就慌。

在线图书馆是一个非常典型的 CRUD 场景，几乎每个后端开发者都会遇到类似的业务。用户要能浏览图书、搜索图书、借阅图书、归还图书。管理员要能上架新书、编辑信息、下架旧书。这些操作涵盖了 REST API 设计的方方面面。

本文会将前 9 篇文章的知识全部串联起来，从路由规划到代码实现，从分页搜索到错误处理，从 Swagger 文档到前端调用示例。读完这一篇，你就有了一个可以直接拿来参考的 REST API 项目模板。
## 基础知识储备
- **前 9 篇文章的核心内容**：REST 概念、URL 设计、HTTP 方法、状态码、参数传递、响应格式、分页过滤、错误处理、Swagger 文档。
- **Flask 路由和请求处理**：能够使用 Flask 开发基本的 Web API。
- **Python 数据操作**：能用列表和字典模拟数据库操作。
- **Postman 或 Swagger UI 使用经验**：能够发出不同类型 HTTP 请求来测试 API。
- **项目结构组织思想**：理解为什么代码需要分层（路由层、业务层、数据层）。
## 正文
### 1. 需求分析与 API 设计

在线图书馆的核心需求：

- **图书管理**：管理员可以添加、编辑、删除图书，用户可以浏览和搜索图书。
- **用户管理**：用户注册、登录、查看个人信息。
- **借阅管理**：用户借书、还书、查看借阅历史。

根据这些需求，我们设计出以下 API 端点：

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/v1/books | 获取图书列表（支持分页、搜索、筛选） |
| GET | /api/v1/books/:id | 获取单本图书详情 |
| POST | /api/v1/books | 添加新书（管理员） |
| PUT | /api/v1/books/:id | 更新图书信息（管理员） |
| DELETE | /api/v1/books/:id | 删除图书（管理员） |
| POST | /api/v1/users/register | 用户注册 |
| POST | /api/v1/users/login | 用户登录 |
| GET | /api/v1/users/me | 获取当前用户信息 |
| POST | /api/v1/borrows | 借阅图书 |
| PATCH | /api/v1/borrows/:id/return | 归还图书 |
| GET | /api/v1/borrows | 查看借阅记录 |

这 11 个端点覆盖了图书馆的核心业务，全部遵循 RESTful 设计原则：名词表示资源、HTTP 方法表示操作、版本号前缀便于未来演进。

### 2. 完整代码实现

```python
"""
在线图书馆 REST API — 综合实战
将所有前面学到的知识整合到一个完整的项目中
"""
from flask import Flask, request, jsonify
from datetime import datetime, timedelta
import hashlib
import uuid

app = Flask(__name__)

# ============================================================
# 数据层：模拟数据库（实际项目应使用 SQLAlchemy + PostgreSQL）
# ============================================================
books_db = []
users_db = []
borrows_db = []
tokens_db = {}  # token -> user_id 映射

# ============================================================
# 工具函数：统一响应格式、错误处理、序列化
# ============================================================

def success(data, message="success"):
    return jsonify({"code": 0, "message": message, "data": data})

def error(code, message, http_status=400, detail=None):
    resp = {"code": code, "message": message, "data": None}
    if detail:
        resp["detail"] = detail
    response = jsonify(resp)
    response.status_code = http_status
    return response

def paginated(items, total, page, size):
    return success({
        "items": items,
        "total": total,
        "page": page,
        "size": size,
        "totalPages": (total + size - 1) // size
    })

def serialize_book(b):
    return {"id": b["id"], "title": b["title"], "author": b["author"],
            "isbn": b.get("isbn", ""), "year": b.get("year"), "status": b["status"]}

def serialize_user(u):
    return {"id": u["id"], "username": u["username"], "email": u["email"],
            "role": u["role"], "createdAt": u["created_at"]}

def serialize_borrow(br):
    return {"id": br["id"], "bookId": br["book_id"], "userId": br["user_id"],
            "borrowDate": br["borrow_date"], "returnDate": br.get("return_date"),
            "status": br["status"]}

# ============================================================
# 认证中间件
# ============================================================
def get_current_user():
    """从请求头解析当前登录用户"""
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        return None
    token = auth_header[7:]
    user_id = tokens_db.get(token)
    if not user_id:
        return None
    return next((u for u in users_db if u["id"] == user_id), None)

def require_auth():
    """需要登录的接口调用此函数"""
    user = get_current_user()
    if not user:
        return error(20003, "请先登录", 401)
    return user

def require_admin():
    """需要管理员权限的接口调用此函数"""
    user = require_auth()
    if isinstance(user, tuple):  # 是错误响应
        return user
    if user["role"] != "admin":
        return error(20004, "无权限，需要管理员角色", 403)
    return user

# ============================================================
# 图书管理 API
# ============================================================
@app.route("/api/v1/books", methods=["GET"])
def list_books():
    """获取图书列表，支持分页、搜索、按状态筛选"""
    page = request.args.get("page", 1, type=int)
    size = request.args.get("size", 10, type=int)
    keyword = request.args.get("keyword", "").strip()
    status = request.args.get("status", "").strip()

    if page < 1:
        return error(10001, "page 必须大于 0", 400)
    if size < 1 or size > 100:
        return error(10001, "size 必须在 1-100 之间", 400)

    filtered = books_db
    if keyword:
        filtered = [b for b in filtered
                    if keyword.lower() in b["title"].lower()
                    or keyword.lower() in b.get("author", "").lower()]
    if status:
        filtered = [b for b in filtered if b["status"] == status]

    total = len(filtered)
    start = (page - 1) * size
    end = start + size
    return paginated(
        [serialize_book(b) for b in filtered[start:end]],
        total, page, size
    )

@app.route("/api/v1/books/<int:book_id>", methods=["GET"])
def get_book(book_id):
    """获取单本图书详情"""
    book = next((b for b in books_db if b["id"] == book_id), None)
    if not book:
        return error(30001, "图书不存在", 404)
    return success(serialize_book(book))

@app.route("/api/v1/books", methods=["POST"])
def create_book():
    """添加新书（管理员权限）"""
    admin = require_admin()
    if isinstance(admin, tuple):
        return admin

    data = request.json
    if not data:
        return error(10001, "请求体不能为空", 400)
    if "title" not in data or not data["title"].strip():
        return error(10001, "书名不能为空", 400,
                     detail={"field": "title", "reason": "必填"})
    if "author" not in data or not data["author"].strip():
        return error(10001, "作者不能为空", 400,
                     detail={"field": "author", "reason": "必填"})

    # 检查 ISBN 重复
    isbn = data.get("isbn", "")
    if isbn and any(b.get("isbn") == isbn for b in books_db):
        return error(30002, f"ISBN {isbn} 已存在", 422)

    new_id = max([b["id"] for b in books_db], default=0) + 1
    book = {
        "id": new_id,
        "title": data["title"].strip(),
        "author": data["author"].strip(),
        "isbn": isbn,
        "year": data.get("year"),
        "status": "available",
        "created_at": datetime.now().isoformat()
    }
    books_db.append(book)
    resp = success(serialize_book(book), "图书添加成功")
    resp.status_code = 201
    return resp

@app.route("/api/v1/books/<int:book_id>", methods=["PUT"])
def update_book(book_id):
    """完整更新图书信息（管理员权限）"""
    admin = require_admin()
    if isinstance(admin, tuple):
        return admin

    book = next((b for b in books_db if b["id"] == book_id), None)
    if not book:
        return error(30001, "图书不存在", 404)

    data = request.json
    if not data:
        return error(10001, "请求体不能为空", 400)

    book["title"] = data.get("title", "").strip()
    book["author"] = data.get("author", "").strip()
    book["isbn"] = data.get("isbn", "")
    book["year"] = data.get("year")
    return success(serialize_book(book), "图书更新成功")

@app.route("/api/v1/books/<int:book_id>", methods=["DELETE"])
def delete_book(book_id):
    """删除图书（管理员权限）"""
    admin = require_admin()
    if isinstance(admin, tuple):
        return admin

    global books_db
    book = next((b for b in books_db if b["id"] == book_id), None)
    if not book:
        return error(30001, "图书不存在", 404)

    # 检查是否有未归还的借阅记录
    active = [br for br in borrows_db
              if br["book_id"] == book_id and br["status"] == "active"]
    if active:
        return error(30003, "该图书还有未归还的借阅记录，无法删除", 422)

    books_db = [b for b in books_db if b["id"] != book_id]
    return success(None, "图书删除成功")

# ============================================================
# 用户 API
# ============================================================
@app.route("/api/v1/users/register", methods=["POST"])
def register():
    """用户注册"""
    data = request.json
    if not data:
        return error(10001, "请求体不能为空", 400)

    username = data.get("username", "").strip()
    email = data.get("email", "").strip()
    password = data.get("password", "")

    if not username:
        return error(20001, "用户名不能为空", 400)
    if not password or len(password) < 6:
        return error(20001, "密码长度不能少于6位", 400)

    # 检查重复
    if any(u["username"] == username for u in users_db):
        return error(20001, "用户名已存在", 422)
    if any(u["email"] == email for u in users_db):
        return error(20001, "邮箱已被注册", 422)

    new_id = max([u["id"] for u in users_db], default=0) + 1
    user = {
        "id": new_id,
        "username": username,
        "email": email,
        "password_hash": hashlib.sha256(password.encode()).hexdigest(),
        "role": "user",
        "created_at": datetime.now().isoformat()
    }
    users_db.append(user)
    return success(serialize_user(user), "注册成功"), 201

@app.route("/api/v1/users/login", methods=["POST"])
def login():
    """用户登录"""
    data = request.json
    username = data.get("username", "").strip()
    password = data.get("password", "")

    user = next((u for u in users_db if u["username"] == username), None)
    if not user:
        return error(20002, "用户名或密码错误", 401)

    pwd_hash = hashlib.sha256(password.encode()).hexdigest()
    if user["password_hash"] != pwd_hash:
        return error(20002, "用户名或密码错误", 401)

    # 生成 token
    token = uuid.uuid4().hex
    tokens_db[token] = user["id"]
    return success({"token": token, "user": serialize_user(user)}, "登录成功")

@app.route("/api/v1/users/me", methods=["GET"])
def get_me():
    """获取当前用户信息"""
    user = require_auth()
    if isinstance(user, tuple):
        return user
    return success(serialize_user(user))

# ============================================================
# 借阅 API
# ============================================================
@app.route("/api/v1/borrows", methods=["POST"])
def borrow_book():
    """借阅图书"""
    user = require_auth()
    if isinstance(user, tuple):
        return user

    data = request.json
    book_id = data.get("book_id")
    if not book_id:
        return error(10001, "book_id 必填", 400)

    book = next((b for b in books_db if b["id"] == book_id), None)
    if not book:
        return error(30001, "图书不存在", 404)
    if book["status"] != "available":
        return error(40001, "该图书已被借出", 422)

    # 检查用户是否已在借该图书
    already_borrowed = any(
        br for br in borrows_db
        if br["user_id"] == user["id"]
        and br["book_id"] == book_id
        and br["status"] == "active"
    )
    if already_borrowed:
        return error(40002, "您已借阅该图书，请先归还", 422)

    new_id = max([br["id"] for br in borrows_db], default=0) + 1
    borrow = {
        "id": new_id,
        "user_id": user["id"],
        "book_id": book_id,
        "borrow_date": datetime.now().isoformat(),
        "due_date": (datetime.now() + timedelta(days=30)).isoformat(),
        "return_date": None,
        "status": "active"
    }
    borrows_db.append(borrow)
    book["status"] = "borrowed"
    return success(serialize_borrow(borrow), "借阅成功"), 201

@app.route("/api/v1/borrows/<int:borrow_id>/return", methods=["PATCH"])
def return_book(borrow_id):
    """归还图书"""
    user = require_auth()
    if isinstance(user, tuple):
        return user

    borrow = next((br for br in borrows_db if br["id"] == borrow_id), None)
    if not borrow:
        return error(40003, "借阅记录不存在", 404)
    if borrow["user_id"] != user["id"] and user["role"] != "admin":
        return error(20004, "只能归还自己的借阅记录", 403)
    if borrow["status"] != "active":
        return error(40004, "该记录已归还", 422)

    borrow["status"] = "returned"
    borrow["return_date"] = datetime.now().isoformat()

    # 恢复图书状态
    book = next((b for b in books_db if b["id"] == borrow["book_id"]), None)
    if book:
        book["status"] = "available"

    return success(serialize_borrow(borrow), "归还成功")

@app.route("/api/v1/borrows", methods=["GET"])
def list_borrows():
    """查看借阅记录（自己的或全部管理员）"""
    user = require_auth()
    if isinstance(user, tuple):
        return user

    page = request.args.get("page", 1, type=int)
    size = request.args.get("size", 10, type=int)

    # 普通用户只看自己的，管理员可看全部
    if user["role"] == "admin":
        filtered = borrows_db
    else:
        filtered = [br for br in borrows_db if br["user_id"] == user["id"]]

    total = len(filtered)
    start = (page - 1) * size
    end = start + size
    return paginated(
        [serialize_borrow(br) for br in filtered[start:end]],
        total, page, size
    )

# ============================================================
# 初始化测试数据
# ============================================================
def init_test_data():
    """初始化一些测试数据，方便开发调试"""
    global books_db, users_db
    books_db = [
        {"id": 1, "title": "REST API 设计指南", "author": "张三",
         "isbn": "978-7-001-00001-1", "year": 2024, "status": "available",
         "created_at": "2024-01-01T00:00:00"},
        {"id": 2, "title": "Python Web 开发实战", "author": "李四",
         "isbn": "978-7-001-00002-8", "year": 2023, "status": "available",
         "created_at": "2023-06-15T00:00:00"},
        {"id": 3, "title": "深入理解计算机系统", "author": "Randal E. Bryant",
         "isbn": "978-7-111-54493-7", "year": 2016, "status": "borrowed",
         "created_at": "2022-03-20T00:00:00"},
        {"id": 4, "title": "代码整洁之道", "author": "Robert C. Martin",
         "isbn": "978-7-115-21687-8", "year": 2010, "status": "available",
         "created_at": "2021-09-01T00:00:00"},
        {"id": 5, "title": "设计模式：可复用面向对象软件的基础", "author": "GoF",
         "isbn": "978-7-111-07554-6", "year": 2004, "status": "available",
         "created_at": "2020-01-15T00:00:00"},
    ]
    users_db = [
        {"id": 1, "username": "admin", "email": "admin@library.com",
         "password_hash": hashlib.sha256("admin123".encode()).hexdigest(),
         "role": "admin", "created_at": "2024-01-01T00:00:00"},
        {"id": 2, "username": "reader", "email": "reader@library.com",
         "password_hash": hashlib.sha256("reader123".encode()).hexdigest(),
         "role": "user", "created_at": "2024-01-02T00:00:00"},
    ]

# 全局异常处理
@app.errorhandler(Exception)
def handle_exception(e):
    app.logger.error(f"Unhandled exception: {type(e).__name__}: {str(e)}",
                     exc_info=True)
    return error(99999, "服务器内部错误", 500)

if __name__ == "__main__":
    init_test_data()
    print("""在线图书馆 API 已启动！
测试账号：
  管理员 — username: admin, password: admin123
  读者 — username: reader, password: reader123
API 端点：
  图书管理: GET/POST /api/v1/books, GET/PUT/DELETE /api/v1/books/<id>
  用户: POST /api/v1/users/register, POST /api/v1/users/login, GET /api/v1/users/me
  借阅: POST /api/v1/borrows, PATCH /api/v1/borrows/<id>/return, GET /api/v1/borrows
""")
    app.run(debug=True, port=5000)
```

这个项目虽小，但五脏俱全：统一的响应信封、错误码分段管理、认证中间件、RBAC 权限控制、分页搜索、资源状态管理。

### 3. 如何用 Swagger UI 和前端调用测试

复制上面的代码到本地文件 `app.py`，运行：
```bash
pip install flask
python app.py
```

然后用 curl 测试：

```bash
# 1. 浏览图书
curl http://localhost:5000/api/v1/books

# 2. 搜索图书
curl "http://localhost:5000/api/v1/books?keyword=Python&size=5"

# 3. 用户登录
curl -X POST http://localhost:5000/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"reader","password":"reader123"}'

# 4. 借书（用登录返回的 token）
curl -X POST http://localhost:5000/api/v1/borrows \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{"book_id": 1}'

# 5. 还书
curl -X PATCH http://localhost:5000/api/v1/borrows/1/return \
  -H "Authorization: Bearer <your_token>"

# 6. 管理员添加新书
curl -X POST http://localhost:5000/api/v1/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"title":"新书测试","author":"测试作者"}'
```

前端调用的简单示例：
```javascript
// 搜索图书
const books = await fetch(
  '/api/v1/books?keyword=Python&page=1&size=10'
).then(r => r.json());

// 借阅图书（需登录后的 token）
const borrowResp = await fetch('/api/v1/borrows', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  },
  body: JSON.stringify({ book_id: 1 })
}).then(r => r.json());
```
## 总结
- 在线图书馆 API 证明了 RESTful 设计可以覆盖一个完整的业务系统：11 个端点覆盖了图书管理、用户认证、借阅操作三大模块。
- 统一的 `{code, message, data}` 响应格式让前端处理变得极其简单，一个拦截器搞定所有接口。
- 认证中间件 `require_auth()` 和 `require_admin()` 用函数组合的方式实现了清晰的权限控制。
- 错误码按模块分段（10000 通用、20000 用户、30000 图书、40000 借阅），出问题时定位飞快。
- 这个项目可以直接作为新项目的 API 脚手架，改改业务逻辑就能用。
## 注意事项
- 这是教学用的模拟数据（内存中的列表），生产环境需要替换为真实数据库（PostgreSQL/MySQL），并使用迁移工具管理表结构。
- 密码哈希使用了 SHA-256 仅作演示，生产环境必须使用 bcrypt（passlib 库）进行加盐哈希。
- 借阅超过应还日期（due_date）的超期处理逻辑本文未实现，可以根据业务需要添加定时任务来标记超期。
- token 存储在当前程序的字典中，重启服务就会丢失，生产环境应使用 Redis 或数据库存储 token，并设置过期时间。
## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 内存数据重启丢失 | 刚添加的图书重启服务后消失了 | 数据存储在 Python 字典中 | 生产环境使用 PostgreSQL/MySQL 持久化数据 |
| PUT 只传部分字段导致数据丢失 | 更新书名后作者变成了空字符串 | 混淆了 PUT 和 PATCH 的语义 | 用 PUT 时必须传所有字段，或改用 PATCH 做部分更新 |
| 借阅时未校验图书状态 | 两个用户同时借同一本书都成功 | 没有检查 status == "available" | 创建借阅前先检查图书状态，生产环境加数据库行锁 |
| 归还时操作了已归还记录 | 重复归还导致图书变为 available 但借阅记录混乱 | 归还接口未校验当前状态 | 归还前检查 borrow.status == "active"，不是则拒绝操作 |
