# Lab 04：JWT 用户认证（上）— 注册和登录接口

> **预计用时：** 40-45 分钟
> **难度：** ⭐⭐
> **前置 Lab：** Lab 03（数据库必须已有 users 表）

---

## 📌 前言

JWT 是我踩过最深的一个坑。当时做第一个全栈项目，注册登录调了两天没调通。最后发现两个问题：第一，bcrypt 版本装错了（5.x 和 passlib 不兼容，必须用 4.0.1）；第二，token 生成后前端不知道怎么存、怎么传、过期了怎么刷新。

后来我把整套认证流程摸透了，发现它就是一个三部曲：**注册（创建用户 + 哈希密码）→ 登录（验证密码 + 签发 token）→ 后续请求（带 token 访问）**。这一 Lab 先搞定前两部——注册和登录接口。

---

## 📚 基础知识储备

- **哈希（Hash）** — 把密码变成一串不可逆的乱码。即使数据库泄露，黑客也拿不到原始密码。我们用 bcrypt 算法
- **JWT（JSON Web Token）** — 一串加密的字符串，里面装着用户信息。登录成功后后端返回 token，前端存起来，以后每次请求都带着它证明"我是我"
- **Access Token vs Refresh Token** — Access Token 短期有效（15 分钟），用来访问 API；Refresh Token 长期有效（7 天），用来刷新过期的 Access Token。双 token 机制 = 安全 + 用户体验
- **Bearer Token** — HTTP 请求头里的一种认证格式：`Authorization: Bearer <token>`

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 理解密码哈希的完整流程（注册时哈希 → 存库 → 登录时验证）
2. 实现 `/api/auth/register` 和 `/api/auth/login` 接口
3. 用 Postman 或 Swagger 测试注册登录，拿到 JWT token
4. 理解 token 的签发和验证机制

---

## 🛠 动手实战

### 步骤 1：读懂认证路由 `routers/auth.py`

**打开文件：** `backend/app/routers/auth.py`

**核心结构（从上到下读）：**

```python
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from pydantic import BaseModel
from jose import jwt
from datetime import datetime, timedelta
from passlib.context import CryptContext
from app.database import get_db
from app.config import settings
from app.models.user import User

router = APIRouter()

# CryptContext 负责密码哈希和验证
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
```

**第一步理解：** `CryptContext` 是 passlib 提供的密码工具，它能：
- `.hash("明文密码")` → 返回哈希后的乱码
- `.verify("明文密码", "哈希值")` → 返回 True 或 False

---

### 步骤 2：理解注册接口

```python
class RegisterRequest(BaseModel):
    email: str
    password: str

@router.post("/register", response_model=TokenResponse)
def register(req: RegisterRequest, db: Session = Depends(get_db)):
    # 1. 检查邮箱是否已注册
    if db.query(User).filter(User.email == req.email).first():
        raise HTTPException(status_code=400, detail="Email already registered")

    # 2. 创建用户（密码哈希存储）
    user = User(
        email=req.email,
        password_hash=pwd_context.hash(req.password),  # ← 关键：存哈希，不存明文
        credits_remaining=settings.FREE_CREDITS,         # ← 新用户送 10 次
    )
    db.add(user)
    db.commit()
    db.refresh(user)

    # 3. 直接返回 token（注册即登录）
    return create_tokens(user.id)
```

**逐行理解：**
- 第 2 行：`BaseModel` 定义请求体格式 → 前端传 `{"email": "...", "password": "..."}`
- 第 6 行：`db.query(User).filter(...)` 查数据库有没有这个邮箱
- 第 11 行：`pwd_context.hash(req.password)` 把明文密码变成哈希
- 第 13 行：`settings.FREE_CREDITS` 默认是 10，新用户送 10 次生成机会
- 第 19 行：注册成功后直接返回 token，用户不需要再登录

---

### 步骤 3：理解登录接口

```python
class LoginRequest(BaseModel):
    email: str
    password: str

@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, db: Session = Depends(get_db)):
    # 1. 查用户
    user = db.query(User).filter(User.email == req.email).first()
    if not user or not user.password_hash:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    # 2. 验证密码
    if not pwd_context.verify(req.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    #              ↑ 明文          ↑ 数据库里的哈希

    # 3. 签发 token
    return create_tokens(user.id)
```

**关键：** `pwd_context.verify("用户输入的密码", "数据库里的哈希")` → 内部会用相同的算法哈希然后比对，匹配就返回 True。

---

### 步骤 4：理解 Token 签发函数

```python
def create_tokens(user_id: str) -> TokenResponse:
    # Access Token：15 分钟有效
    access_payload = {
        "sub": user_id,
        "exp": datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES),
        "type": "access",
    }
    # Refresh Token：7 天有效
    refresh_payload = {
        "sub": user_id,
        "exp": datetime.utcnow() + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
        "type": "refresh",
    }
    return TokenResponse(
        access_token=jwt.encode(access_payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM),
        refresh_token=jwt.encode(refresh_payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM),
    )
```

**Token 里面有什么？**
- `sub`：用户 ID（subject）
- `exp`：过期时间
- `type`：token 类型（access 或 refresh）
- 然后用 `JWT_SECRET` 签名，防止伪造

---

### 步骤 5：用 Swagger 测试注册

**操作：**
1. 启动后端：`.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000`
2. 打开 http://localhost:8000/docs
3. 找到 `POST /api/auth/register`
4. 点 "Try it out"
5. 输入请求体：
```json
{
  "email": "test@example.com",
  "password": "123456"
}
```
6. 点 "Execute"
7. 看返回：
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
  "token_type": "bearer"
}
```

**验证：** 返回了 `access_token` 和 `refresh_token` ✅

---

### 步骤 6：用 Swagger 测试登录

**操作：**
1. 找到 `POST /api/auth/login`
2. 输入同样的邮箱密码
3. 执行，应该也返回两个 token

**验证：** 登录成功，返回 token ✅

---

### 步骤 7：测试"查看个人信息"接口（带 token）

**操作：**
1. 找到 `GET /api/auth/me`
2. 点 "Try it out"
3. 但这需要认证！点击页面右上角的 🔒 图标或 "Authorize" 按钮
4. 输入：`Bearer <你的 access_token>`（注意 Bearer 后面有空格）
5. 点 "Authorize"，然后关闭弹窗
6. 回到 `/api/auth/me`，点 "Execute"
7. 返回：
```json
{
  "id": "uuid...",
  "email": "test@example.com",
  "tier": "free",
  "credits_remaining": 10,
  "created_at": "2026-06-25T..."
}
```

**验证：** 能返回你的用户信息 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| bcrypt 版本不对 | `passlib` 报错 `MissingBackendError` | 装了 bcrypt 5.x | 必须装 `bcrypt==4.0.1`：`pip install bcrypt==4.0.1` |
| Token 过期 | 接口返回 401 | Access Token 只有 15 分钟 | 用 Refresh Token 刷新（Lab 05 会讲） |
| 密码存明文 | 数据库里直接是 123456 | 没用 `pwd_context.hash()` | 必须哈希存储，这是安全底线 |
| Bearer 格式写错 | 认证失败 | 写成了 `Bearer eyJ...`（没空格）或 `bearer`（小写） | 正确格式：`Bearer eyJ...`（B 大写，中间有空格） |

---

## 📝 总结

**本章核心要点：**
- 密码永远存哈希，不存明文——`pwd_context.hash(password)`
- 登录时用 `pwd_context.verify(明文, 哈希)` 验证
- JWT 双 token 机制：Access Token 短期（15 分钟）+ Refresh Token 长期（7 天）
- 后续请求在 Header 里带 `Authorization: Bearer <token>`

**你现在应该能做到：**
- 用 Swagger 测试注册和登录
- 说出 JWT token 的三个组成部分（header.payload.signature）
- 理解为什么密码不能存明文

**下一步：** Lab 05 我们把前端登录页面接上，实现完整的注册登录流程——包括 token 自动刷新和路由守卫。

---

> —— 阿珊，前端开发者 & AI 提效实践者
