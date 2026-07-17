# 用户认证从0到1 第7篇：OAuth 实战：接入 GitHub 登录

@[TOC](目录)

## 摘要
> 上一篇文章讲了 OAuth 的概念，这篇带你从头到尾接入 GitHub OAuth——注册应用、编写回调、换取 token、创建本地用户，每一步都有可运行的代码。

## 引言

理论学得再多，不如亲手跑一遍。GitHub 的 OAuth 2.0 实现是业界最规范、文档最清晰的之一，非常适合作为学习 OAuth 的第一个实战目标。整个过程涉及三个角色：你的浏览器、你的后端服务、GitHub 的 OAuth 服务器。

接入 GitHub 登录的实际意义不仅限于学习。很多面向开发者的产品（如文档平台、开源工具）首选 GitHub 登录，因为目标用户 100% 拥有 GitHub 账号。你做完这个实战后，接入任何其他 OAuth 平台（微信、Google、Twitter）的流程几乎一模一样，只是 API 地址和参数名略有不同。

本文将完整展示从注册 OAuth App 到用户成功登录的全过程。所有代码在本地即可运行，不需要服务器部署。

## 基础知识储备

1. **OAuth 2.0 授权码流程**：本文完全基于前一篇文章的理论基础，需要理解 code 换 token 的步骤。
2. **FastAPI 基础**：会用 FastAPI 定义路由、处理请求参数、返回 JSON/RedirectResponse。
3. **requests 库**：会用 Python 的 `requests` 发 HTTP POST 请求，理解 `headers` 和 `data` 参数。
4. **环境变量管理**：能创建 `.env` 文件，用 `python-dotenv` 或手动读取环境变量。
5. **GitHub 账号**：需要有一个 GitHub 账号用于注册 OAuth App（不需要特殊权限）。

## 正文

### 第一步：注册 GitHub OAuth App

前往 GitHub → Settings → Developer settings → OAuth Apps → New OAuth App。填写以下信息：

- **Application name**：你的应用名称，如"My Blog"（用户授权页会显示这个名称）
- **Homepage URL**：`http://localhost:8000`（本地开发用）
- **Authorization callback URL**：`http://localhost:8000/api/auth/github/callback`（这是最关键的配置，必须和代码中的回调路由完全一致）

注册成功后你会拿到 Client ID 和 Client Secret。点"Generate a new client secret"生成。务必把这两个值保存到 `.env` 文件中，不要让它们出现在代码仓库里。

```
# .env
GITHUB_CLIENT_ID=your_client_id_here
GITHUB_CLIENT_SECRET=your_client_secret_here
```

### 第二步：编写后端回调逻辑

整个流程需要两个路由。`/login/github` 负责重定向到 GitHub 授权页，`/callback` 负责处理回调并使用 code 换取 token。

```python
import os
import requests
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import RedirectResponse
from urllib.parse import urlencode

app = FastAPI()

GITHUB_CLIENT_ID = os.getenv("GITHUB_CLIENT_ID", "")
GITHUB_CLIENT_SECRET = os.getenv("GITHUB_CLIENT_SECRET", "")
GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize"
GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token"
GITHUB_USER_API = "https://api.github.com/user"
REDIRECT_URI = "http://localhost:8000/api/auth/github/callback"

@app.get("/api/auth/github/login")
def github_login():
    """第一步：重定向用户到 GitHub 授权页"""
    params = {
        "client_id": GITHUB_CLIENT_ID,
        "redirect_uri": REDIRECT_URI,
        "scope": "user:email",  # 请求读取用户邮箱权限
    }
    auth_url = f"{GITHUB_AUTH_URL}?{urlencode(params)}"
    return RedirectResponse(auth_url)

@app.get("/api/auth/github/callback")
def github_callback(code: str, request: Request):
    """第二步：用授权码换 Access Token，然后获取用户信息"""
    # 用 code 换 access_token
    token_resp = requests.post(
        GITHUB_TOKEN_URL,
        data={
            "client_id": GITHUB_CLIENT_ID,
            "client_secret": GITHUB_CLIENT_SECRET,
            "code": code,
            "redirect_uri": REDIRECT_URI,
        },
        headers={"Accept": "application/json"},
    )
    if token_resp.status_code != 200:
        raise HTTPException(status_code=400, detail="换 token 失败")

    access_token = token_resp.json().get("access_token")
    if not access_token:
        raise HTTPException(status_code=400, detail="未获取到 access_token")

    # 用 access_token 获取用户信息
    user_resp = requests.get(
        GITHUB_USER_API,
        headers={"Authorization": f"Bearer {access_token}"},
    )
    if user_resp.status_code != 200:
        raise HTTPException(status_code=400, detail="获取用户信息失败")

    github_user = user_resp.json()
    user_info = {
        "github_id": github_user["id"],
        "username": github_user["login"],
        "avatar": github_user["avatar_url"],
        "name": github_user.get("name", github_user["login"]),
    }

    # 第三步：在自己的数据库创建或查找用户，签发你自己系统的 JWT
    # local_user = find_or_create_user(user_info)
    # token = create_access_token(local_user.id)

    return {"message": "登录成功", "user": user_info}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, port=8000)
```

### 第三步：理解整个流程的数据流转

当用户在浏览器访问 `http://localhost:8000/api/auth/github/login` 时：

1. 浏览器收到 302 重定向，跳转到 `github.com/login/oauth/authorize?...`，URL 里带你的 Client ID 和回调地址。
2. 用户（如果没登录 GitHub）输入 GitHub 密码登录，然后看到授权页面："My Blog 请求访问你的个人资料和邮箱，是否授权？"
3. 用户点"Authorize"，GitHub 将浏览器重定向回 `http://localhost:8000/api/auth/github/callback?code=xxx`。
4. 你的后端从 URL 参数拿到 `code`，向 GitHub 的 `/access_token` 接口发 POST 请求，附上 code + client_id + client_secret。
5. GitHub 验证通过，返回 `access_token`。
6. 你的后端用这个 `access_token` 请求 GitHub 的 `/user` API，拿到用户的 GitHub ID、用户名、头像。
7. 你的后端根据 GitHub ID 在自己的数据库查找或创建用户记录，签发你自己系统的 JWT（或 Session），用户在你的系统里"登录成功"。

### 为什么 Client Secret 绝不能在浏览器中出现？

从上面的流程可以看到，只有第 4 步用到了 Client Secret，而这一步发生在你的后端服务器和 GitHub 服务器之间。浏览器只参与了第 1、2、3 步——跳转、授权、接收 code。

如果有人拿到了你的 Client Secret，他就可以冒充你的应用去 GitHub 请求 token。用户看到授权页面上写的是"My Blog"，但实际上授权码被攻击者的服务器截走了。这种攻击叫做"Client Secret 泄露导致的身份冒充"，是所有 OAuth 实现中最常见的安全事故。

把 Client Secret 写到前端代码里等于把它公开发布——因为任何人打开浏览器 DevTools、读源码、甚至看 `bundle.js`，都能找到它。

## 总结

1. 接入 GitHub OAuth 需要三个步骤：注册 OAuth App 获取 Client ID/Secret，编写登录路由（重定向到 GitHub），编写回调路由（code 换 token）。
2. 整个 OAuth 流程中，浏览器只接触 code（短命、一次性），Client Secret 和 Access Token 只在后端服务器之间传输。
3. 拿到 GitHub 用户信息后，要在自己的系统中"绑定"用户——根据 GitHub ID 查找或创建本地用户记录，签发自己系统的凭证。
4. GitHub OAuth 的接入流程是所有 OAuth 平台的通用模板——接入微信、Google、Twitter 唯一的不同是 API 地址和参数名。

## 注意事项

1. GitHub 的 `/access_token` 接口要求设置 `Accept: application/json` 请求头，否则会返回 URL 编码格式的字符串而非 JSON。
2. 回调 URL 必须和 GitHub OAuth App 设置中的完全一致——`http` vs `https`、端口号、路径都要精确匹配。
3. 本地开发时无法使用 `https` 回调，GitHub 允许 `localhost` 使用 `http`，但生产环境必须切换到 `https`。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| Accept 头缺失 | token 接口返回的不是 JSON，解析报错 | GitHub 的 `/access_token` 默认返回 URL 编码格式，需要显式声明 Accept 头 | 请求时设置 `headers={"Accept": "application/json"}` |
| 回调 URL 不匹配 | GitHub 报错"redirect_uri mismatch" | OAuth App 配置中的回调 URL 和代码中的 REDIRECT_URI 不一致——空格、斜杠、端口号都算不匹配 | 在代码和 GitHub 设置中复制粘贴同一个 URL，确保完全一致 |
| 用 access_token 调 user API 时格式错误 | GitHub 返回 401 Bad credentials | Authorization 头格式不对——少了"Bearer "前缀或拼写错误 | 确保 headers 格式为 `{"Authorization": "Bearer " + access_token}`（注意 Bearer 后有一个空格） |
| scope 参数写错 | 获取用户邮箱时返回空或 404 | 没有请求 `user:email` 权限范围 | 在授权 URL 中明确设置 `scope: "user:email"`，如果只需要基本资料则用空 scope |
