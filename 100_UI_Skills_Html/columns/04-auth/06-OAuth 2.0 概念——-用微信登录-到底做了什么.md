# 用户认证从0到1 第6篇：OAuth 2.0：用微信登录做了什么

@[TOC](目录)

## 摘要
> 为什么"用微信登录"不需要把微信密码告诉第三方应用？答案就是 OAuth 2.0——一种让第三方应用在不知道你密码的情况下，安全访问你资源的授权协议。

## 引言

你访问一个新网站，注册太麻烦，于是点击"用微信登录"或"Sign in with GitHub"。浏览器跳转到微信的授权页面，微信问你"某某应用请求访问你的昵称和头像，是否同意？"你点了同意，然后又跳回原网站——神奇的事情发生了，网站不需要你设密码，也不需要填邮箱，直接用微信身份完成了登录。

整个过程下来，原网站从头到尾都没有拿到你的微信密码。它拿到的只是一个临时的"授权码"，用这个授权码加上自己的密钥（只有网站和微信平台知道），换取了一个有时效性的"访问令牌"，靠这个令牌获取你的基本资料。

这就是 OAuth 2.0 的核心价值：让你安全地给第三方应用授权，访问你在某个平台上的受保护资源，而第三方应用永远接触不到你的密码。

## 基础知识储备

1. **HTTP 重定向**：理解浏览器收到 302 响应后会自动跳转到新 URL 的机制，这是 OAuth 流程中页面跳转的基础。
2. **回调 URL（Redirect URI）**：第三方应用需要提前注册一个回调地址，授权完成后平台会把用户引导回这个地址并附带授权码。
3. **客户端-服务器-授权服务器三角模型**：OAuth 涉及三方——用户（Resource Owner）、第三方应用（Client）、授权平台（Authorization Server）。
4. **Access Token 的概念**：已经学过 JWT 的你应该对 token 不陌生，OAuth 中的 Access Token 类似，但由授权平台签发，用于访问平台的 API。

## 正文

### 一张图读懂 OAuth 2.0 授权码流程

OAuth 2.0 定义了四种授权模式，其中最常用、最安全的是**授权码模式（Authorization Code Grant）**。它专为有后端服务器的 Web 应用设计。

整个过程涉及 7 个步骤，可以分成两段来理解。前半段在浏览器里发生，后端不参与；后半段在后端完成，浏览器毫不知情。

第一段——获取授权码（发生在浏览器）：
- 用户在 A 网站点"用微信登录"
- A 网站前端将浏览器重定向到微信的授权页面，URL 中携带 A 网站的 Client ID 和回调地址
- 微信显示授权页面："A 网站请求访问你的昵称、头像，是否同意？"
- 用户点击同意
- 微信将浏览器重定向回 A 网站的回调地址，URL 中附带一个临时授权码（code），有效期通常只有 10 分钟且只能用一次

第二段——用授权码换 Access Token（发生在后端）：
- A 网站的后端收到回调请求，从 URL 参数中取出 code
- A 网站后端向微信的 token 接口发 POST 请求，提交 code + Client ID + Client Secret
- 微信验证 code 和 secret 都有效，返回 Access Token（以及可选的 Refresh Token）

至此，A 网站拿到了 Access Token，可以用它调用微信 API 获取用户的昵称、头像等信息。从头到尾，A 网站没有接触过用户的微信密码，Client Secret 也从未暴露给浏览器。

### 为什么授权码模式最安全？

关键设计在于"code 和 secret 分离"。授权码（code）在浏览器 URL 中传递，任何人都可能看到（浏览器历史记录、HTTP 日志），但它本身没有直接价值——没有 Client Secret，光有 code 换不到 token。Client Secret 只在后端服务器和微信之间传递，浏览器完全接触不到。

这个设计创造了一个安全的分界线：浏览器只接触"短命且不可单独使用"的 code，真正的凭证（Access Token + Client Secret）只在后端服务器之间传输。

对比一下另外三种模式，你就能理解为什么它们已经不被推荐：**隐式模式**直接在前端返回 Access Token（没有 code 换 token 那一步），token 暴露在浏览器中，安全风险极高。**密码模式**要求用户把用户名密码直接交给第三方应用——这等于让用户教会所有人"钓鱼"两个字怎么写。**客户端凭证模式**适用于机器对机器的通信（如服务器定时任务），不涉及用户授权。

### 用 Python 模拟一个极简 OAuth 授权服务器

理解 OAuth 最好的方式是自己跑一遍。下面这段代码模拟了一个极简的 OAuth 授权服务器和第三方客户端——不是生产级代码，但涵盖完整的授权码流程：

```python
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import RedirectResponse
from uuid import uuid4

app = FastAPI()

# --- 模拟微信/Google 等授权平台 ---
CLIENTS = {"my_app_client_id": {"secret": "my_secret", "redirect_uri": "http://localhost:8001/callback"}}
auth_codes = {}  # 临时授权码存储

@app.get("/oauth/authorize")
def authorize(client_id: str, redirect_uri: str, response_type: str = "code"):
    if client_id not in CLIENTS:
        raise HTTPException(400, "无效的 client_id")
    # 生成授权码（实际项目要先验证用户身份、让用户确认授权）
    code = uuid4().hex[:16]
    auth_codes[code] = {"client_id": client_id, "user": "test_user@example.com"}
    return RedirectResponse(f"{redirect_uri}?code={code}")

@app.post("/oauth/token")
async def token(request: Request):
    data = await request.form()
    code = data.get("code")
    client_id = data.get("client_id")
    client_secret = data.get("client_secret")

    if code not in auth_codes:
        raise HTTPException(400, "授权码无效或已过期")
    client = CLIENTS.get(client_id)
    if not client or client["secret"] != client_secret:
        raise HTTPException(401, "client_id 或 secret 错误")

    session = auth_codes.pop(code)  # 授权码一次性使用，用完即删
    return {"access_token": f"at_{uuid4().hex}", "token_type": "bearer", "user": session["user"]}

print("授权服务器启动在 http://localhost:8000")
# 运行: uvicorn <此文件>:app --port 8000
```

这个模拟让你看到 OAuth 最本质的四个接口：`/authorize`（浏览器跳转，返回 code）、`/token`（后端调用，用 code 换 token）。知道这两个接口的参数和流程，你就理解了 90% 的 OAuth 授权码模式。

## 总结

1. OAuth 2.0 的核心价值：第三方应用在不知道用户密码的情况下，安全访问用户在平台上的受保护资源。
2. 授权码模式是最安全的 OAuth 模式，关键设计在于 code（浏览器可见）和 secret（仅后端持有）的分离。
3. 完整流程分两段：前半段在浏览器中获取授权码（code），后半段在后端用 code 换 Access Token。
4. 隐式模式和密码模式已不被推荐，新项目应一律使用授权码模式 + PKCE 扩展。
5. Client Secret 绝对不能出现在前端代码中，所有 token 交换操作必须在后端完成。

## 注意事项

1. 授权码（code）有过期时间（通常 10 分钟），且只能使用一次——用过后立即失效，防止重放攻击。
2. 回调地址（redirect_uri）必须在 OAuth 平台提前注册白名单，不能随意指定——这是防止钓鱼攻击的关键措施。
3. OAuth 不等于认证（AuthN）——OAuth 本质是授权协议，但被广泛"借用"来做认证（因为授权后第三方可以获取用户信息，间接实现登录）。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| Client Secret 写在前端代码里 | 任何人都可以通过浏览器 DevTools 或查看源码拿到你的密钥，然后伪造你的应用获取用户授权 | OAuth 安全模型建立在 secret 保密的基础上 | Client Secret 只存在于后端环境变量中，token 交换逻辑写在后端 API 里 |
| redirect_uri 配置不一致 | 授权后报错"redirect_uri mismatch"，用户看到白屏 | OAuth 平台要求回调 URL 必须精确匹配——"http"vs"https"、尾部斜杠有无都算不匹配 | 在 OAuth 平台注册时精确配置回调 URL，开发环境和生产环境各配一个 |
| 用授权码模式但把 Access Token 返回给前端 | token 暴露在浏览器中，攻击者可通过浏览器历史或 XSS 获取 | 开发者不理解"前端只拿 code，后端换 token"的设计意图 | 将 token 交换操作放在后端，返回给前端的是自己签发的 session/JWT，而非 OAuth 平台的原生 token |
| 不验证 state 参数 | 被 CSRF 攻击，攻击者诱导用户授权后绑定到攻击者自己的账号 | OAuth 流程缺少 CSRF 防护，没有验证授权请求和回调是否来自同一个会话 | 发起授权请求时生成随机 state，回调时验证 state 是否匹配 |
