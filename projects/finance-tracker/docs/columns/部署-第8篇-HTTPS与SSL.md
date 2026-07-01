# 部署从0到1 第8篇：HTTPS 与 SSL —— 浏览器的小锁怎么来的

> HTTP 是明信片，HTTPS 是密封信封。浏览器不允许 HTTPS 页面请求 HTTP 接口。

---

## 📌 前端为什么要学

那天晚上，我的红薯快写 App 上线 Vercel，兴冲冲打开一看——白屏。

打开控制台，一片红：

```
Mixed Content: The page at 'https://red-note-express-app.vercel.app' 
was loaded over HTTPS, but requested an insecure XMLHttpRequest 
endpoint 'http://huanglishan123.pythonanywhere.com/api/health'. 
This request has been blocked; the content must be served over HTTPS.
```

我懵了。后端在 PythonAnywhere 跑得好好的，Postman 能调通，怎么浏览器就不行？

查了才知道：**Vercel 默认给你 HTTPS，浏览器发现 HTTPS 页面里混了 HTTP 请求，全给你拦了。**

这就是 Mixed Content。浏览器的安全策略：**HTTPS 页面不能请求 HTTP 资源。** 要么全 HTTPS，要么全不。

你们前端工程师迟早会遇到这个问题——项目上线那一刻，浏览器不给面子。不是因为代码写错了，是因为你的后端还没穿上 SSL 这件衣服。

最后怎么解决的？我把后端从 PythonAnywhere 迁到了 Render。Render 免费版自动给 `.onrender.com` 域名配好 SSL 证书，零配置，一部署，小锁亮了。

但这个过程让我意识到：**前端不能不懂 HTTPS。** 你不懂，就不知道为什么被拦；不知道为什么被拦，你就只能瞎折腾。

---

## 🔍 核心原理

### HTTP vs HTTPS：明信片 vs 密封信封

想象你给朋友寄一封信：

- **HTTP**：你把信写在明信片上。邮递员能看到内容，中转站能看到内容，任何一个环节都能看、能改。
- **HTTPS**：你把信放进密封信封。邮递员只看到收件地址，看不到内容，也改不了——改了收件人收到就会发现。

技术上说：

| | HTTP | HTTPS |
|---|---|---|
| 传输内容 | 明文 | 加密 |
| 端口 | 80 | 443 |
| 身份验证 | 无 | CA 证书验证 |
| 数据完整性 | 无保障 | 防篡改 |
| URL 前缀 | `http://` | `https://` |

HTTP 的请求体、请求头、URL 参数全都是明文在网络上跑。你在咖啡店连 WiFi，隔壁桌的人抓个包就能看到你发了什么、密码是什么。

HTTPS 做了什么？它在 TCP 和 HTTP 之间加了一层 **TLS（Transport Layer Security）**，以前的版本叫 SSL。这一层负责加密、身份验证、完整性校验。

```
HTTP 栈:                    HTTPS 栈:
┌──────────┐               ┌──────────┐
│   HTTP   │               │   HTTP   │
├──────────┤               ├──────────┤
│   TCP    │               │ TLS/SSL  │
├──────────┤               ├──────────┤
│    IP    │               │   TCP    │
├──────────┤               ├──────────┤
│   ...    │               │    IP    │
└──────────┘               └──────────┘
```

### TLS 握手：四次"打招呼"建立加密通道

浏览器和服务器在传数据之前，先要握个手，商量好怎么加密。这个过程叫 **TLS 握手**。

我用一个比喻来讲。假设你是浏览器，你要跟一个陌生服务器建立加密通信：

**第一步：ClientHello（"你好，我会这些加密方式"）**

浏览器发一个消息，告诉服务器：

- 我支持的 TLS 版本（1.2, 1.3）
- 我支持的加密套件（AES、ChaCha20 等）
- 一个随机数（client random）

就像你跟陌生人说："你好，我会中文、英文，你挑一个。"

**第二步：ServerHello + 证书（"我用这个，这是我的身份证"）**

服务器回复：

- 选定的加密套件
- 自己的 SSL 证书（里面包含公钥）
- 另一个随机数（server random）

证书是什么？就是服务器去CA（证书颁发机构）那里办的"身份证"。上面写着"我叫 api.example.com"，还盖了 CA 的章。

你的浏览器内置了主流 CA 的根证书，拿到服务器的证书一验，章是真的，身份就对上了。

**第三步：密钥交换（"只有咱俩知道的密码"）**

浏览器用服务器证书里的公钥，加密一个"预主密钥"（pre-master secret），发给服务器。只有持有对应私钥的服务器才能解开。

现在双方都有了三样东西：client random + server random + pre-master secret。它们用这三样东西，通过同样的算法，各自算出相同的**会话密钥**（session key）。

**第四步：加密通信（"开始说悄悄话"）**

双方互发一个"我准备好了"的加密消息，验证密钥没问题。之后所有数据都用这个会话密钥加密传输。

如果有人中间截获了前两步的随机数，他能算出会话密钥吗？不能。因为最关键的那个 pre-master secret 是用服务器的公钥加密传输的，只有服务器能解开。

**TLS 1.3 更快：** 现代 TLS 1.3 把握手减到了 1-RTT（一次往返）。面试问到 TLS 1.2 vs 1.3，就记住一点：1.3 减少了往返次数，握手更快。

### 证书链：信任的传递

一张证书不是凭空可信的。它是一条"信任链"：

```
根 CA 证书（浏览器内置信任）
    │
    └── 中间 CA 证书（根 CA 签发）
            │
            └── 你的网站证书（中间 CA 签发）
```

浏览器验证链条：你的证书谁签的？→ 中间 CA 签的。中间 CA 的证书谁签的？→ 根 CA 签的。根 CA 的证书我内置了，可信。链条完整，你的证书可信。

如果中间断了一环（比如中间证书没配置），浏览器就报警："此网站的安全证书不受信任。"

### 前端类比：HTTPS = JWT 签名验证

作为前端开发者，你一定用过 JWT。HS256 签名后，JWT 的后两部分是签名过的，中间人看不懂内容（虽然 base64 解码能看到，但那是编码不是加密），更重要的是——**中间人改不了**。一改，签名就对不上了。

HTTPS 的思路一模一样：

- **JWT**：签名保证"这个 token 是我发的，没被改过"
- **HTTPS**：TLS 加密保证"这个请求是我发的，中间没人看过、没人改过"

都是防篡改。不同的是 JWT 不加密内容（payload 可 base64 解码），而 HTTPS 全程加密。

---

## 🛠 动手试试

### 1. 浏览器点小锁看证书

打开任意一个 HTTPS 网站（比如 `https://github.com`），点地址栏左边的小锁图标：

- Chrome：小锁 → 连接是安全的 → 证书有效
- 能看到：颁发给谁（CN = github.com）、颁发者（DigiCert）、有效期、指纹

你甚至可以导出证书，看到完整的证书链。这是最直观的学习方式——**每个前端每天都在看小锁，但很少有人点进去看过。**

### 2. curl -v 看 TLS 握手

打开终端，跑一行：

```bash
curl -v https://www.baidu.com
```

你会看到 TLS 握手的完整过程：

```
* ALPN: offers h2,http/1.1
* TLSv1.3 (OUT), TLS handshake, Client hello (1):
* TLSv1.3 (IN), TLS handshake, Server hello (2):
* TLSv1.3 (IN), TLS handshake, Encrypted Extensions (8):
* TLSv1.3 (IN), TLS handshake, Certificate (11):
* TLSv1.3 (IN), TLS handshake, CERT verify (15):
* TLSv1.3 (IN), TLS handshake, Finished (20):
* TLSv1.3 (OUT), TLS handshake, Finished (20):
* SSL connection using TLSv1.3 / TLS_AES_256_GCM_SHA384
```

这就是刚才讲的四次"打招呼"在真实的底层日志里长什么样。每一行都能对应上。

再试一个 HTTP 的对比一下：

```bash
curl -v http://www.baidu.com
```

你看——没有任何 TLS 握手日志，直接就是 `GET / HTTP/1.1`。裸奔。

### 3. Vercel + Render：自动 HTTPS，零配置

这是我的真实部署方案，前后端都自动 HTTPS：

**前端 Vercel：**

- 把项目推到 GitHub
- Vercel 导入项目
- 自动分配 `xxx.vercel.app` 域名
- **自动配好 SSL 证书，小锁直接亮**

你什么都不用做。Let's Encrypt 证书自动申请、自动续期。

**后端 Render：**

- 在 Render 创建 Web Service
- 连上你的 GitHub 仓库
- 选好启动命令（比如 `uvicorn app.main:app --host 0.0.0.0 --port $PORT`）
- 部署完成后，`.onrender.com` 域名**自动 HTTPS**

Render 免费版唯一的代价：15 分钟没请求会休眠，下次访问冷启动等几十秒。

**关键点：前后端都 HTTPS 了，Mixed Content 问题自然消失。**

如果你不想换平台，也可以在 PythonAnywhere 上配 Let's Encrypt，但那需要升级付费账户（免费版不支持自定义 SSL）。对个人项目来说，Render 的零配置体验实在太友好了。

---

## ⚠️ 常见坑点

### 坑 1：Mixed Content（混合内容）

**现象：** HTTPS 页面里的 HTTP 图片、接口、脚本全部被拦。

**解决：** 全部上 HTTPS。如果后端暂时没法 HTTPS，开发阶段可以试试——

- 浏览器临时允许不安全内容（点地址栏右边的盾牌图标，仅限本地调试）
- 用 `https://cors-anywhere` 之类的代理（不推荐生产使用）
- **正经方案：给后端配 SSL**

### 坑 2：自签名证书

**现象：** 你自己用 openssl 生成了一个证书配到服务器上，浏览器打开显示"您的连接不是私密连接"，红色警告页。

**原因：** 你的证书是自己签的，浏览器不信任你。只有 CA 签发的证书浏览器才认。

**解决：**

- 本地开发可以用 `mkcert` 工具生成本地受信任证书
- 生产环境用 Let's Encrypt 免费证书，或云平台自动配的证书

### 坑 3：证书过期

**现象：** 某天突然网站打不开了，所有用户看到证书过期警告。

**Let's Encrypt 证书有效期只有 90 天。** 一定要配自动续期。

- Vercel / Render：平台自动续，不用管
- 自己服务器上用 Let's Encrypt：配 `certbot` 定时任务
- 买付费证书：有效期一年，到期前记得续

### 坑 4：CORS 配了 HTTP 地址

**现象：** 后端配了 CORS `allow_origins = ["http://localhost:5173"]`，但前端部署后是 `https://xxx.vercel.app`，协议不对，CORS 报错。

**解决：** CORS 配置里协议也要匹配。

```python
# 错误：协议不匹配
ALLOWED_ORIGINS = "http://xxx.vercel.app"

# 正确：协议匹配
ALLOWED_ORIGINS = "https://xxx.vercel.app"
```

或者开发阶段直接配 `["*"]`，不过生产环境最好指定具体域名。

---

## 🎯 面试会问

### Q1：HTTP 和 HTTPS 的区别？

**标准答案：**

1. HTTP 明文传输，HTTPS 加密传输
2. HTTP 端口 80，HTTPS 端口 443
3. HTTPS 需要 SSL/TLS 证书
4. HTTPS 提供身份验证（防止 DNS 劫持到假网站）
5. HTTPS 防篡改（中间人改了数据会检测到）
6. HTTPS 对 SEO 更友好（Google 给 HTTPS 排名加权）

**加分项：** HTTPS 是 HTTP over TLS，在 TCP 和 HTTP 之间插入了 TLS 层。

### Q2：TLS 握手过程？

**面试官想听的答案（简洁版）：**

1. ClientHello：客户端发支持的加密套件 + 随机数
2. ServerHello：服务器选加密套件 + 发证书（含公钥）+ 随机数
3. 客户端验证证书 → 用公钥加密 pre-master secret 发给服务器
4. 双方用三个随机数算出会话密钥 → 后续对称加密通信

**加分项：** TLS 1.3 减到 1-RTT，握手更快；支持 0-RTT 模式（会话恢复）。

### Q3：为什么需要 CA（证书颁发机构）？

中间人攻击场景：

```
你 ←→ [假基站] ←→ 真服务器
```

如果没有 CA，攻击者可以在你和服务器之间架一个假基站，伪装成服务器跟你通信，你的所有数据都被他看到了。

CA 的作用：**权威第三方担保"这个公钥确实属于 api.example.com"。**

你浏览器内置了可信 CA 的根证书，服务器证书上有 CA 的签名，你验签通过，就知道公钥是真的。攻击者的假证书没有可信 CA 签名，浏览器立刻报警。

---

## 📝 小结

HTTPS 不是什么高深的东西。你只需要记住三件事：

1. **HTTP 是明信片，HTTPS 是密封信封。** 上了线就一定要 HTTPS。
2. **Mixed Content 是前端上线最常见的坑。** 当你看到白屏 + 控制台一堆 blocked 请求，第一反应就是检查是不是 HTTPS 页面调了 HTTP 接口。
3. **TLS 握手就是四次"打招呼"。** ClientHello → ServerHello+证书 → 密钥交换 → 加密通信。

对于个人项目，**Vercel + Render = 零配置 HTTPS。** 你只管写代码，SSL 证书平台帮你搞定。这是 2026 年个人开发者的最佳性价比方案。

HTTPS 不是可选项，是底线。用户数据不能在互联网上裸奔——就像你不会把密码写在明信片上寄给别人。

---

## 🎨 生图提示词

> 一只小猫站在邮筒旁边，左手拿着一张明信片（上面写着 HTTP），右手拿着一个密封信封（上面写着 HTTPS），明信片的内容清晰可见（明文），信封密封完好。邮筒旁边有一个小锁图标亮着。风格：扁平插画，暖色调，简洁可爱。用于技术博客配图。

---

> 专栏《部署从0到1》第8篇 完。
>
> 作者：阿珊
> 标签：#部署 #HTTPS #SSL #前端部署 #从0到1
>
> 下一篇预告：第9篇《自定义域名与 DNS —— 给你的 App 一个名字》
