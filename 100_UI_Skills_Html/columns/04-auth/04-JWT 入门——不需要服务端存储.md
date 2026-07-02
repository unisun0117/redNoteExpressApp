# 用户认证从0到1 第4篇：JWT 入门：不需要服务端存储的认证

@[TOC](目录)

## 摘要
> JWT（JSON Web Token）让服务器不再需要存储 session——token 本身携带用户信息，通过签名防伪。一次签发、到处验证，天然适合分布式系统和前后端分离架构。

## 引言

在上一篇文章中我们讲到，Session 认证需要服务端存储每个用户的会话数据。用户量小还好，一旦用户量达到百万级，维护一个庞大的 Session 存储就成了不小的运维负担。更麻烦的是，当系统拆成多个微服务时，每个服务都需要访问同一个 Session 存储（通常是 Redis），架构复杂度直线上升。

JWT 解决这个问题的思路非常巧妙——既然服务端不想存储用户信息，那把用户信息直接写进 token 里不就行了？但这里有个显而易见的问题：如果信息写在 token 里，用户能不能自己改？比如把 `"role": "user"` 改成 `"role": "admin"`？答案是不能——因为 JWT 附带了一个只有服务端知道的签名，任何篡改都会导致签名验证失败。

这就是 JWT 的核心思想：用加密签名保证数据不可篡改，从而实现"无状态"的认证。签发一次 token，后续请求只需要验证签名，完全不需要查数据库或 Redis。这套机制让 JWT 天然适合前后端分离和微服务架构。

## 基础知识储备

1. **Base64 编码**：JWT 的三个部分都用 Base64URL 编码，这是一种将二进制数据转为可打印字符的方式，注意它不是加密。
2. **对称加密 vs 非对称加密**：HS256 使用同一个密钥签名和验证（对称），RS256 使用私钥签名、公钥验证（非对称）。入门阶段先理解 HS256 即可。
3. **JSON 格式**：JWT 的 Payload 部分是 JSON 对象，包含声明（Claims），如 `sub`（主体）、`exp`（过期时间）、`iat`（签发时间）。
4. **时间戳概念**：JWT 使用 Unix 时间戳（秒数）表示过期时间，理解 `datetime.utcnow().timestamp()` 的含义。
5. **无状态认证理念**：理解"服务端不存储任何会话信息，每次请求独立验证"的架构思想。

## 正文

### 解剖一个 JWT：三段结构解析

一个典型的 JWT 看起来像这样（三部分用 `.` 分隔）：

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJyb2xlIjoidXNlciIsImV4cCI6MTcwMDAwMDAwMH0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

第一部分是 Header（头部），Base64 解码后是一个 JSON 对象，声明了签名算法和 token 类型。典型内容是 `{"alg":"HS256","typ":"JWT"}`。Header 的意义在于告诉验证方"请用这个算法验证我"——但这也引入了一个安全问题：如果攻击者把算法改成 `none`，有些老旧的 JWT 库会跳过签名验证，这就是臭名昭著的"none algorithm"攻击。现代的 JWT 库已经修复了这个问题，但理解这个细节会让你在面试中脱颖而出。

第二部分是 Payload（载荷），Base64 解码后是实际的用户数据。你可以放任何字段——`user_id`、`role`、`email`——但绝不能放密码、信用卡号等敏感信息。因为 Base64 是编码不是加密，任何人都可以用在线工具解码 Payload 查看内容。

第三部分是 Signature（签名），这是 JWT 安全性的核心。签名算法把 Header 和 Payload 拼接起来，用服务端密钥算出一个哈希值。验证时，服务端用同样的算法重新计算一次——如果别人改了 Payload 中的任何字节，签名就对不上了。

下面用 Python 的 PyJWT 库实现完整的签发和验证：

```python
import jwt
from datetime import datetime, timedelta

SECRET_KEY = "prod-secret-key-change-me"  # 生产环境从环境变量读取

def create_access_token(user_id: int, role: str = "user") -> str:
    """签发 JWT Access Token"""
    payload = {
        "user_id": user_id,
        "role": role,
        "exp": datetime.utcnow() + timedelta(minutes=15),  # 15分钟过期
        "iat": datetime.utcnow(),
        "type": "access"
    }
    token = jwt.encode(payload, SECRET_KEY, algorithm="HS256")
    return token


def verify_token(token: str) -> dict:
    """验证 JWT 并返回 payload"""
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
        return payload
    except jwt.ExpiredSignatureError:
        raise Exception("Token 已过期，请重新登录")
    except jwt.InvalidTokenError:
        raise Exception("Token 无效 —— 可能被篡改")

# 演示
token = create_access_token(user_id=42, role="admin")
print(f"签发的 JWT:\n{token}\n")
print(f"解码后的 payload: {verify_token(token)}")

# 尝试篡改：修改 payload 后验证
parts = token.split(".")
# 把中间那段（payload）改掉一个字符 —— 模拟篡改
tampered = parts[0] + "." + parts[1][:-1] + "X" + "." + parts[2]
try:
    verify_token(tampered)
except Exception as e:
    print(f"篡改后验证结果: {e}")
```

### JWT 的地盘：什么时候用，什么时候别用

JWT 最适合的场景是前后端分离的 SPA 应用和微服务架构。前端拿到 token 存在内存或 localStorage，每次请求通过 `Authorization: Bearer <token>` 头传给后端。在微服务架构中，用户请求经过 API Gateway，Gateway 验证 JWT 后把用户信息写入请求头，下游服务不需要再查用户库——整个链路无状态、高性能。

但 JWT 也有明显的局限性。最大的痛点是 token 无法主动撤销——一旦签发出去，在过期之前，服务端没办法让它失效。用户改密码后，旧的 JWT 依然有效；管理员封禁用户后，已经签发的 JWT 还能用。解决方案有三种：一是把 Access Token 有效期设得非常短（15分钟），二是维护一个 token 黑名单（但这又回到了有状态模式），三是使用 Refresh Token 轮换机制（下篇文章详讲）。

JWT 不适合存大量数据。因为 token 每次请求都要在网络上传输，Payload 过大会增加每个请求的带宽开销。通常建议 JWT 的 Payload 只放用户标识和权限信息，业务数据通过 API 按需获取。

### 使用 JWT 的三条安全准则

第一，密钥绝对不能泄露。JWT 的安全性完全建立在密钥保密的基础上——拿到密钥 = 能签发任意 token。密钥从环境变量读取，写在 `.gitignore` 里，和代码仓库彻底隔离。

第二，始终设置合理的过期时间。不建议签发永久有效的 JWT。Web 应用的 Access Token 通常设为 15-60 分钟，移动 App 可以适当延长。过期时间越短，token 泄露后的窗口期越小。

第三，Payload 不要放敏感数据。再强调一次：Base64 是编码，不是加密。任何人都能看到 JWT 的 Payload 内容。密码、身份证号、手机号这些信息绝不能出现在 JWT 里。

## 总结

1. JWT 由 Header（算法声明）、Payload（用户数据）、Signature（防伪签名）三部分组成，用 `.` 分隔。
2. Base64 是编码而非加密，任何人都能解码 Payload 看到内容，所以绝不能放密码等敏感信息。
3. JWT 的最大优势是无状态——服务端不需要存储 session，天然适合分布式系统和微服务架构。
4. JWT 的最大劣势是无法主动撤销——在过期前，已签发的 token 始终有效，除非引入黑名单机制。
5. 密钥保密、合理过期时间、Payload 不放敏感数据，是使用 JWT 的三条安全底线。

## 注意事项

1. PyJWT 库安装后导入名为 `jwt`，而非 `pyjwt`。验证时必须传入 `algorithms` 列表，否则会报 DeprecationWarning。
2. JWT 标准使用 UTC 时间，Python 中用 `datetime.utcnow()` 而非 `datetime.now()`。
3. 不要手写 JWT 的签名验证逻辑，使用成熟的库（PyJWT、jsonwebtoken）即可。自己实现的签名验证大概率有漏洞。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| Payload 放密码或手机号 | 用户密码或隐私信息以明文形式出现在每次请求的请求头中，任何中间节点都能解码查看 | Base64 编码不提供任何机密性保护 | Payload 只放 user_id、role 等非敏感标识，敏感数据通过 API 按需获取 |
| 使用 HS256 时密钥太短 | 攻击者通过字典爆破密钥，成功伪造 token | HS256 要求密钥长度 >= 256 bits（32字节），短密钥容易被暴力破解 | 使用 `secrets.token_hex(32)` 生成 64 位十六进制密钥 |
| 不验证算法白名单 | 某些 JWT 库接受 `alg: "none"` 的 token，跳过签名验证 | 老旧的 JWT 实现允许"无算法"token，攻击者伪造任意 payload | 验证时显式指定 `algorithms=["HS256"]`，不接受 token 中声明的算法 |
| Token 存在 localStorage | XSS 攻击中攻击者通过 JS 读取 token，冒充用户 | localStorage 可被同域的任意 JS 代码读取 | 将 Access Token 存储在 JS 内存中（变量），Refresh Token 存储在 httpOnly Cookie 中 |
