# 用户认证从0到1 第5篇：双Token方案：Access + Refresh

@[TOC](目录)

## 摘要
> 单 JWT 方案有个致命弱点：token 过期用户就要重新登录。双 Token 方案用短命的 Access Token 调接口，长命的 Refresh Token 静默续期，用户无感、安全在线。

## 引言

上一篇文章我们讲了 JWT 的基本用法：登录后签一个 token，调接口时带上它。但这里面藏着一个令人抓狂的用户体验问题——token 过期后怎么办？如果把有效期设得很长（比如 30 天），万一 token 泄露，攻击者有整整一个月的时间可以冒充用户。如果把有效期设得很短（比如 15 分钟），用户每隔 15 分钟就要输一次密码——这谁受得了？

双 Token 方案就是为了解决这个两难困境而生的。它引入了一对配合工作的 token：Access Token 有效期极短（通常 15 分钟），直接用来调用业务接口，即使泄露影响也有限；Refresh Token 有效期较长（通常 7 天），唯一用途就是换取新的 Access Token，不参与日常的业务接口调用。

这个设计背后是一个安全工程的经典原则：最小暴露面。Access Token 在每次请求中都在网络上传输，暴露风险高，所以必须短命。Refresh Token 只在极少数请求（刷新）中出现一次，暴露面小得多，因此可以长命。

## 基础知识储备

1. **JWT 的签发与验证**：需要掌握前一篇 JWT 的基础内容，理解 payload、签名、过期时间的概念。
2. **HTTP 请求拦截器**：前端 axios/fetch 拦截器可以在每个请求发出前自动附加 token，在收到 401 时自动刷新。
3. **Promise 和异步编程**：前端无感刷新逻辑涉及 Promise 缓存，防止多个并发请求同时触发刷新。
4. **Cookie 安全属性**：Refresh Token 通常存在 httpOnly Cookie 中，需要理解 HttpOnly、Secure、SameSite 的作用。
5. **token 泄露的风险模型**：理解为什么频繁传输的 token 要短命、偶尔传输的 token 可以长命。

## 正文

### 双 Token 的工作流程

整个流程分为正常使用和过期刷新两种路径。

正常路径：用户登录成功后，后端一次性返回两个 token——短命的 Access Token（15分钟）和长命的 Refresh Token（7天）。前端把 Access Token 存在内存中（JS 变量），后续每次调 API 都在请求头加上 `Authorization: Bearer <access_token>`。后端验证 Access Token 有效，正常响应。

过期刷新路径：当 Access Token 过期（或者用户直接拿一个已过期的 token 调接口），后端返回 401。前端拦截器发现 401，自动调 `/api/auth/refresh` 接口，带上 Refresh Token。后端验证 Refresh Token 有效，签发新的 Access Token 并返回。前端拿到新 token，用新 token 重试刚才失败的那个请求。这整个过程对用户完全透明。

强制重新登录路径：当 Refresh Token 也过期了（用户 7 天没打开应用），刷新接口也返回 401。此时前端清空 token，跳转到登录页。

下面用 Python 实现后端双 Token 逻辑：

```python
import jwt
from datetime import datetime, timedelta

SECRET = "change-me-in-production"
ACCESS_EXPIRE_MINUTES = 15
REFRESH_EXPIRE_DAYS = 7

def create_access_token(user_id: int, role: str = "user") -> str:
    payload = {
        "user_id": user_id,
        "role": role,
        "exp": datetime.utcnow() + timedelta(minutes=ACCESS_EXPIRE_MINUTES),
        "type": "access"
    }
    return jwt.encode(payload, SECRET, algorithm="HS256")

def create_refresh_token(user_id: int) -> str:
    payload = {
        "user_id": user_id,
        "exp": datetime.utcnow() + timedelta(days=REFRESH_EXPIRE_DAYS),
        "type": "refresh"
    }
    return jwt.encode(payload, SECRET, algorithm="HS256")

def verify_token(token: str, expected_type: str = "access"):
    payload = jwt.decode(token, SECRET, algorithms=["HS256"])
    if payload.get("type") != expected_type:
        raise jwt.InvalidTokenError("Token 类型不匹配")
    return payload

# --- FastAPI 路由 ---
from fastapi import FastAPI, HTTPException, Request, Response

app = FastAPI()

@app.post("/api/login")
def login(response: Response):
    user_id = 1  # 实际项目中验证密码后获取
    access = create_access_token(user_id)
    refresh = create_refresh_token(user_id)
    # Access Token 放响应体，Refresh Token 放 httpOnly Cookie
    response.set_cookie(
        key="refresh_token", value=refresh,
        httponly=True, secure=True, samesite="lax",
        max_age=REFRESH_EXPIRE_DAYS * 86400,
        path="/api/auth"  # 只在 /api/auth 路径下发送
    )
    return {"access_token": access}

@app.post("/api/auth/refresh")
def refresh(request: Request):
    refresh_token = request.cookies.get("refresh_token")
    if not refresh_token:
        raise HTTPException(status_code=401, detail="缺少 Refresh Token")
    try:
        payload = verify_token(refresh_token, expected_type="refresh")
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Refresh Token 已过期，请重新登录")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Refresh Token 无效")
    # 签发新的 Access Token
    new_access = create_access_token(payload["user_id"], payload.get("role", "user"))
    return {"access_token": new_access}

@app.get("/api/me")
def get_current_user(request: Request):
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="缺少 Access Token")
    try:
        payload = verify_token(auth_header[7:], expected_type="access")
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Access Token 已过期")
    return {"user_id": payload["user_id"], "role": payload["role"]}
```

### 前端无感刷新：拦截器里的精巧设计

前端实现无感刷新的关键点在 axios 的响应拦截器。拦截器捕获到 401 错误后，不直接跳转到登录页，而是先去调 refresh 接口。如果刷新成功，用新 token 重试原请求；如果刷新也失败（Refresh Token 过期），才跳转到登录页。

这里面有一个容易踩的并发陷阱：假如页面上同时发了 5 个 API 请求，它们几乎同时收到 401。如果每个请求都独立去调 refresh 接口，5 个刷新请求会同时到达后端——这不仅是浪费，更可能触发风控。解决方案是用一个 Promise 做"刷新锁"：第一个 401 请求触发刷新并缓存这个 Promise，后续的 401 请求直接等这个 Promise 完成，拿到同一个新 token。

下面是前端刷新锁的核心逻辑（概念代码，非完整实现）：

```javascript
let refreshPromise = null;

async function refreshAccessToken() {
  // 如果已经有刷新在进行中，直接返回那个 Promise
  if (refreshPromise) return refreshPromise;

  refreshPromise = fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'include'  // 让浏览器发送 Cookie
  })
    .then(res => res.json())
    .then(data => {
      setAccessToken(data.access_token);  // 更新内存中的 token
      return data.access_token;
    })
    .catch(err => {
      logout();  // 刷新失败，跳转登录页
      throw err;
    })
    .finally(() => {
      refreshPromise = null;  // 清除锁，允许下次刷新
    });

  return refreshPromise;
}
```

这个小小的 Promise 缓存机制，就是"专业"和"能用"之间的分界线。

### Refresh Token 存储策略的权衡

Access Token 的存储没什么争议——放在 JS 内存里最安全，页面刷新就丢了，需要调 refresh 接口重新获取。但 Refresh Token 的存储有三种流派：

存 httpOnly Cookie（最推荐）：浏览器自动管理，JS 绝对读不到（防 XSS），同域自动发送。缺点是跨域需要额外配置，移动端不适用。适合 Web 前端。

存 localStorage（不推荐）：JS 能读写，也就意味着 XSS 攻击能获取。优点是简单，跨域不需要配置。适合对安全性要求不高的内部工具。

存内存（最安全但体验差）：页面一刷新 Refresh Token 就消失，用户需要重新登录。适合银行、支付等对安全要求极高的场景。

## 总结

1. 双 Token 方案用短命 Access Token（分钟级）调接口，长命 Refresh Token（天级）静默续期，兼顾安全性与用户体验。
2. Access Token 在每次请求中传输，暴露面大，所以必须短；Refresh Token 只在刷新时用一次，暴露面小，所以可以长。
3. 前端拦截器的核心设计是"刷新锁"——用一个缓存的 Promise 防止并发 401 触发多次刷新请求。
4. Refresh Token 应存在 httpOnly Cookie 中，这是目前安全性最佳的前端存储方案。
5. Refresh Token 过期 = 用户必须重新登录，这是用户能感知到的唯一"断点"。

## 注意事项

1. Refresh Token 的 Cookie 可以设置 `path=/api/auth`，让浏览器只在调用刷新接口时才发送，进一步缩小暴露面。
2. Refresh Token 建议实现"轮换"机制——每次刷新时签发新的 Refresh Token 并废弃旧的，这样即使旧 token 被截获也只能用一次。
3. 双 Token 不要用同一个密钥签发——万一 Access Token 的密钥泄露，Refresh Token 仍然安全。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 多个请求同时 401 | 刷新接口被调用了 N 次（N = 并发请求数），服务端报错或风控拦截 | 每个失败的请求各自触发了一次 refresh | 用 Promise 缓存（刷新锁），第一个请求去刷新，后续请求排队等结果 |
| Refresh Token 存在 localStorage | XSS 攻击后攻击者拿到 Refresh Token，在 token 有效期内持续冒充用户 | localStorage 可被 JS 任意读取 | Refresh Token 放到 httpOnly Cookie，JS 永远读不到 |
| 刷新后原请求重试失败 | 请求体（如 FormData）在重试时丢失 | 部分请求体（stream、FormData）只能读取一次，重试时 bodyUsed 为 true | 重试前重新构造请求体，或者对 GET 请求重试（POST/PUT 折中处理） |
| Access 和 Refresh 用同一个密钥 | Access Token 的密钥泄露后，攻击者可以签发任意 Refresh Token 实现持久化攻击 | 没有做密钥隔离 | Access Token 和 Refresh Token 使用不同的密钥签名 |
