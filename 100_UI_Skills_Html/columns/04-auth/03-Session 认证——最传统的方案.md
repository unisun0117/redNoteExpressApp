# 用户认证从0到1 第3篇：Session 认证：最传统但最稳的方案

@[TOC](目录)

## 摘要
> Session 认证是 Web 认证的鼻祖——登录后服务端生成一段随机字符串，存到内存或 Redis，浏览器通过 Cookie 自动携带。方案虽老但毫不过时，SSR 应用至今首选。

## 引言

在 JWT 大行其道的今天，Session 认证似乎显得有些"过时"。很多初学者看了一篇 JWT 的文章就觉得 Session 是落后的技术，迫不及待要"迁移到 JWT"。这种想法忽略了 Session 的核心优势：服务端完全掌控登录状态，随时可以踢人下线。

Session 认证的模型非常直观。你可以把它想象成健身房的存包柜——登录时前台给你一把带编号的钥匙（session_id），你把物品锁进对应的柜子。后续每次你去取东西，只要出示钥匙，前台通过编号找到你的柜子。整个过程中，钥匙本身不包含任何信息，真正的数据安全地锁在服务端的柜子里。

这个类比几乎完美对应 Session 认证的每个环节。前端拿着 session_id 只是一个无意义的随机字符串，真正的用户信息存在服务端内存或 Redis 里。即使 session_id 被截获（且没有配置 HTTPS），攻击者也只能拿到一串随机字符，无法解码出任何有效信息。

## 基础知识储备

1. **Cookie 机制**：浏览器会自动存储服务器通过 `Set-Cookie` 响应头下发的 Cookie，并在后续请求中自动携带。
2. **Cookie 属性**：了解 `HttpOnly`（禁止 JS 读取）、`Secure`（仅 HTTPS 传输）、`SameSite`（跨站请求控制）的含义。
3. **字典/Map 数据结构**：Session 存储本质是一个键值对映射，key 是 session_id，value 是用户信息。
4. **过期机制**：理解 TTL（Time To Live）的概念，Session 需要有过期时间，不能永远有效。

## 正文

### Session 认证的完整流程

第一步，用户输入账号密码，前端 POST 到 `/login`。后端验证凭据通过后，生成一个全局唯一的随机字符串（通常用 `uuid4()` 或 `secrets.token_hex()`），这就是 session_id。

第二步，后端在服务端的存储（内存字典、Redis、数据库）中创建一个键值对，key 是 session_id，value 是用户信息（user_id、角色、登录时间等）。

第三步，后端通过 `Set-Cookie` 响应头把 session_id 写入浏览器 Cookie。关键的安全配置是：`HttpOnly=true`（禁止 JavaScript 读取，防 XSS）、`Secure=true`（仅 HTTPS 下传输）、`SameSite=Lax`（防 CSRF）。

第四步，浏览器收到响应后自动存储 Cookie。后续所有到该域名的请求，浏览器都会自动在 Cookie 请求头里带上 session_id，前端不需要写任何额外代码。

第五步，后端在每个请求中从 Cookie 取出 session_id，去存储中查找对应的用户信息。找到就认证通过，找不到就返回 401。

下面是一个完整的 FastAPI 实现，不依赖任何第三方认证库，纯手工理解每一步：

```python
from fastapi import FastAPI, Request, Response, HTTPException
from uuid import uuid4
from datetime import datetime, timedelta

app = FastAPI()

# 简易 Session 存储（生产环境用 Redis）
sessions: dict[str, dict] = {}

# 模拟用户数据库
users_db = {
    "alice@example.com": {"password": "alice_pw", "name": "Alice", "role": "admin"}
}

@app.post("/login")
def login(response: Response, email: str = "", password: str = ""):
    user = users_db.get(email)
    if not user or user["password"] != password:
        raise HTTPException(status_code=401, detail="邮箱或密码错误")

    # 生成随机 session_id
    session_id = uuid4().hex

    # 存入服务端存储
    sessions[session_id] = {
        "email": email,
        "name": user["name"],
        "role": user["role"],
        "created_at": datetime.utcnow().isoformat(),
    }

    # 写入浏览器 Cookie（三个关键安全属性）
    response.set_cookie(
        key="session_id",
        value=session_id,
        httponly=True,    # JS 无法读取
        secure=True,      # 仅 HTTPS
        samesite="lax",   # 防 CSRF
        max_age=3600,     # 1小时过期
    )
    return {"message": f"欢迎, {user['name']}!"}

@app.get("/me")
def get_current_user(request: Request):
    session_id = request.cookies.get("session_id")
    if not session_id or session_id not in sessions:
        raise HTTPException(status_code=401, detail="未登录或会话已过期")
    user = sessions[session_id]
    return {"email": user["email"], "name": user["name"], "role": user["role"]}

@app.post("/logout")
def logout(request: Request, response: Response):
    session_id = request.cookies.get("session_id")
    if session_id and session_id in sessions:
        del sessions[session_id]  # 服务端删除 session
    response.delete_cookie("session_id")  # 通知浏览器删除 Cookie
    return {"message": "已登出"}
```

### Session vs JWT：选择背后的权衡

Session 的核心优势是服务端完全掌控。如果管理员要踢掉某个用户，直接从存储里删除对应的 session 记录即可，用户的 session_id 立刻失效。这个操作在 JWT 方案中要复杂得多——因为 JWT 是无状态的，你无法"删除"一个已经签发出去的 token，只能维护黑名单或者缩短有效期来变相实现。

Session 的劣势也很明显。首先，服务端需要存储，用户量大会带来内存或 Redis 成本。其次，Session 依赖 Cookie 机制，跨域场景（前端在 `localhost:5173`，后端在 `api.example.com`）需要额外配置 CORS 和 Cookie 的 `domain` 属性。最后，水平扩展时，Session 必须放在共享存储（Redis）中，否则用户被负载均衡分配到不同服务器时，另一台服务器不认识这个 session_id。

JWT 的最大卖点是"无状态"，天然适合分布式系统和跨域场景。但 JWT 也有致命弱点——一旦签发就无法撤销（除非引入黑名单，那又变回了有状态方案）。因此，Session 和 JWT 不是先进与落后的区别，而是不同场景下的工程取舍。

### 生产环境的三条铁律

第一条，Cookie 安全三件套缺一不可。`HttpOnly` 防止 XSS 攻击窃取 session_id；`Secure` 阻止 Cookie 在 HTTP 明文传输中被中间人截获；`SameSite=Lax` 防止跨站请求伪造攻击（CSRF）。这三个属性设置不当，Session 认证的安全基础就崩塌了。

第二条，Session 存储必须有过期策略。无限期有效的 session 是安全隐患——用户离开后再也没登出过，session 记录在数据库里越积越多。给 session 设置 TTL（比如 24 小时），到期自动清理。

第三条，生产环境不要用内存字典存 session。本文演示代码用的是 Python 字典，但生产环境的服务器进程会重启、会多实例部署。Session 存储必须用 Redis 或数据库，保证跨进程、跨重启都能访问到。

## 总结

1. Session 认证的模型是"钥匙-柜子"：前端持有无意义的 session_id（钥匙），真正的数据在服务端存储（柜子）。
2. Cookie 安全三件套（HttpOnly + Secure + SameSite）是 Session 认证的安全基石，配置不当等于裸奔。
3. Session 的核心优势是服务端完全掌控登录状态，随时可以踢人下线，这是 JWT 难以做到的。
4. 生产环境必须用 Redis 或数据库做 Session 存储，内存字典在进程重启和多实例部署下会失效。
5. Session 和 JWT 不是替代关系，而是不同场景下的工程选择——SSR 和单体应用首选 Session。

## 注意事项

1. session_id 必须用密码学安全的随机数生成器（`uuid4()` 或 `secrets` 模块），不能用自增 ID 或时间戳——攻击者可以猜出规律。
2. Cookie 的 `max_age` 或 `expires` 必须设置，否则 Cookie 成为"会话 Cookie"，浏览器关闭即失效，用户体验较差。
3. 登出时要同时做两件事：服务端删除 session 记录，客户端通过 `delete_cookie` 清除浏览器中的 Cookie。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| Cookie 没设 HttpOnly | XSS 攻击中，攻击者通过 `document.cookie` 拿到 session_id，冒充用户身份 | JavaScript 可以读取没有 HttpOnly 标记的 Cookie | 所有认证相关的 Cookie 必须设 `httponly=True` |
| 服务重启后所有用户掉线 | 开发时一切正常，一发布用户全部需要重新登录 | Session 存在内存字典中，进程重启即丢失 | 使用 Redis 或数据库做持久化 Session 存储 |
| 跨域请求不携带 Cookie | 前端调接口时 Cookie 没有自动发送 | CORS 跨域时，默认不携带跨域 Cookie | 后端设置 `Access-Control-Allow-Credentials: true`，前端 fetch 设 `credentials: "include"` |
| session_id 可预测 | 攻击者枚举 session_id 成功冒充其他用户 | 用了自增 ID 或简单时间戳作为 session_id | 使用 `uuid4()` 或 `secrets.token_hex(32)` 生成 session_id |
