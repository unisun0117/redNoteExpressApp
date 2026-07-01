# 第3篇：域名与DNS — 从 IP 地址到好看网址

> DNS 就是互联网的电话簿。你输入 jiujin.com，DNS 告诉你服务器 IP 是 76.76.21.21。没有 DNS，你得上百度搜"Vercel 服务器 IP 是多少"然后复制粘贴到地址栏——那互联网就太蠢了。

## 📌 前端为什么要学这个？

我第一次买域名的时候，以为买完就能用了。兴冲冲在阿里云付了 39 块钱，拿到 `jinqiu.finance`，以为这就是我的了——在浏览器里一输，白屏。

然后我开始百度。一堆听不懂的词：A 记录、CNAME、@、www、TTL、DNS 解析。搞了两天才明白：买域名只是在工商局注册了个公司名，DNS 解析才是告诉全世界"这个公司开在哪条街上"。你注册了名字，但没写地址，别人当然找不到你。

前端开发者天天跟域名打交道，但你可能是"知其然不知其所以然"的状态——知道 `fetch("https://api.xxx.com")` 能请求到数据，但不知道 `xxx.com` 是怎么变成一台服务器的。这篇文章就带你把这层窗户纸捅破。

而且 DNS 有一个你非常熟悉的前端类比——`import` 路径解析。你写 `import { TabBar } from '@/components/TabBar'`，Vite 帮你把 `@` 翻译成 `src/`，定位到实际文件。DNS 干的事一模一样——把你写的 `jinqiu.finance` 翻译成 `76.76.21.21`，只不过翻译过程跨了半个地球。

## 🔍 核心原理

### 一个 DNS 查询的全过程

你在浏览器输入 `https://jinqiu.finance`，回车。从这一秒开始，你的电脑要经历下面这串查询才能知道"到底该往哪发请求"：

```
你敲下 jiujin.finance 回车
    │
    ▼
① 浏览器 DNS 缓存
   "我刚才查过 jiujin.finance 吗？"
   ├── 有 → 直接返回 IP，结束（通常缓存 1 分钟）
   └── 没有 → 进入下一步
    │
    ▼
② 操作系统 hosts 文件
   "C:\Windows\System32\drivers\etc\hosts 里有没有写死？"
   ├── 有 → 直接返回 IP，结束
   └── 没有 → 进入下一步
    │
    ▼
③ 本地 DNS 服务器（你的路由器 or 宽带运营商）
   "我缓存里有 jiujin.finance 吗？"
   ├── 有 → 返回给浏览器，结束
   └── 没有 → 我帮你出去问
    │
    ▼
④ 根 DNS 服务器（全球 13 组）
   "jiujin.finance 我不知道，但我知道谁管 .finance —— 你去问他们"
    │
    ▼
⑤ 顶级域 DNS 服务器（管理 .finance 的）
   "jiujin.finance 我不知道具体 IP，但我知道它的权威 DNS 在哪 —— 你去问他们"
    │
    ▼
⑥ 权威 DNS 服务器（你在域名服务商那里配的）
   "jiujin.finance → 76.76.21.21，拿去吧"
    │
    ▼
⑦ 结果层层返回 → 浏览器拿到 IP → 发起 HTTP 请求
```

这个查询过程，7 步里只有最后一步是"真正有答案的"。前面 6 步全是"我不知道，但我知道谁可能知道"——每一层只知道自己管辖范围内的信息，找不到就往下一层问。

### 前端类比：import 路径解析

你每天都在写这种代码：

```typescript
import { authFetch } from '@/services/api';
import { TabBar } from '@/components/TabBar';
import antdMobile from 'antd-mobile';
```

当你写下 `import { authFetch } from '@/services/api'` 时，Vite/Node.js 要进行多少次查找？

```
import '@/services/api'
    │
    ▼
是不是相对路径？不是（以 @ 开头）→ 查别名配置
vite.config.ts 里 resolve.alias: { '@': '/src' }
    │
    ▼
解析成绝对路径：src/services/api
    │
    ▼
找文件：
  src/services/api.ts    → 存在！返回文件内容 ✅
```

如果是 `import antdMobile from 'antd-mobile'`（裸 specifier）：

```
import 'antd-mobile'
    │
    ▼
是不是相对路径/别名？不是 → 去 node_modules 查
    │
    ▼
找 node_modules/antd-mobile/package.json
    │
    ▼
读 package.json 里的 "main" 或 "module" 或 "exports" 字段
    │
    ▼
"module": "./es/index.js" → 定位到 node_modules/antd-mobile/es/index.js ✅
```

发现了吗？DNS 的递归查询和 import 路径解析是**同一种模式**：

| import 路径解析 | DNS 查询 | 共同模式 |
|----------------|---------|---------|
| `@/services/api` — 先查 vite.config.ts 别名配置 | 浏览器先查自己的 DNS 缓存 | 先看本地有没有，没有再往上问 |
| `node_modules/antd-mobile/package.json` — 读 "main"/"exports" 字段 | 根 DNS 告诉你去问 .finance 的管理者 | 每一层只知道"谁可能知道答案" |
| `/es/index.js` — 定位到实际文件 | 权威 DNS 返回 IP `76.76.21.21` | 最后一站才给出最终地址 |

```typescript
// 前端的 import 解析过程，就是 DNS 的翻版
// 你在写：
import { TabBar } from '@/components/TabBar';
// Vite 在背后做的递归查询，跟 DNS 一模一样。
```

> **阿珊说：DNS 就像前端的 import 路径解析——你写 `./utils`，Vite/Node 从 node_modules 找到 package.json 再找到实际文件。DNS 是你写 `jiujin.com`，互联网从根服务器找到 .com 管理者再找到你的服务器。本质是同一套"别名找实体"的递归算法。**

### DNS 记录类型 — 不止 A 记录

DNS 记录不是只有"域名→IP"这一种。你在配置域名时会遇到下面几种记录类型：

| 记录类型 | 全称 | 干什么的 | 例子 | 前端类比 |
|---------|------|---------|------|---------|
| **A** | Address | 域名 → IPv4 地址 | `jiujin.finance → 76.76.21.21` | `const SERVER_IP = "76.76.21.21"` — 直接赋值 |
| **AAAA** | Address v6 | 域名 → IPv6 地址 | `jiujin.finance → 2606:4700::...` | 同上，但地址更长 |
| **CNAME** | Canonical Name | 域名 → 另一个域名（别名） | `www.jiujin.finance → jiujin.finance` | `const WWW = MAIN_DOMAIN` — 引用另一个变量 |
| **MX** | Mail Exchange | 指定邮箱服务器 | `jiujin.finance → mail.google.com` | 指定某个请求走特殊处理 |
| **TXT** | Text | 存任意文本（验证、SPF等） | `"google-site-verification=abc123"` | `.env` 里的配置字符串 |
| **NS** | Name Server | 指定用哪个 DNS 服务器 | 一般域名服务商帮你设 | 指定用哪个 resolver |

最常用的两个是 **A 记录** 和 **CNAME**，区别是一个指 IP，一个指另一个域名：

```
A 记录：    jinqiu.finance  →  76.76.21.21        （指向 IP 地址）
CNAME 记录：www.jinqiu.finance  →  jinqiu.finance   （指向另一个域名）
```

那什么时候用 A 记录，什么时候用 CNAME？

- **服务器有固定 IP** → 用 A 记录。比如你在阿里云买了一台 ECS 服务器，IP 是固定的 `1.2.3.4`。
- **部署在 Vercel/Render 等平台** → 用 CNAME。因为 Vercel 的 IP 可能会变，但 `jinqiu.vercel.app` 这个域名不会变。你写 CNAME 指向 `jinqiu.vercel.app`，不管 Vercel 的 IP 怎么变，你的域名永远跟着走。

> 金句：**A 记录是"地址"，CNAME 是"别名"。当你不确定对方 IP 会不会变的时候，用 CNAME 指向一个不变的域名。**

## 🛠 动手试试

### 实验一：nslookup — 看一个域名到底解析到哪个 IP

打开终端（Windows 用 PowerShell，Mac/Linux 用 Terminal），跑：

```bash
# 查百度的 IP
nslookup baidu.com

# 你会看到类似这样的输出：
# Non-authoritative answer:
# Name:    baidu.com
# Addresses:  110.242.68.66
#             39.156.66.10
```

两个 IP 地址——这就是百度的服务器地址。你在浏览器里输 `baidu.com`，背后 DNS 查出来的是这两个 IP 之一。

再试一个：

```bash
# 查金橘记账部署到 Vercel 之后会用的 IP
nslookup vercel.com

# Non-authoritative answer:
# Name:    vercel.com
# Address:  76.76.21.21
```

看到了吗？`vercel.com` → `76.76.21.21`。这就是第 2 篇文章里提到的那台 Vercel 服务器的真实 IP。你把金橘记账部署到 Vercel 之后，DNS 解析最终指向的就是这个 IP 范围。

```bash
# 查看更详细的 DNS 查询过程
nslookup -debug baidu.com
```

这条命令会打印 DNS 查询的详细过程——发了什么包、收回了什么结果、每一层的 DNS 服务器是谁。你可以看到 DNS 的递归查询不是抽象概念，是真的一条条网络请求。

### 实验二：改 hosts 文件，把任意域名指向 localhost

`hosts` 文件是一个本地 DNS 映射表，它的优先级**高过**任何公网 DNS 服务器。你在这个文件里写什么，操作系统就信什么。

它在这些位置：

```
Windows: C:\Windows\System32\drivers\etc\hosts
Mac:     /etc/hosts
Linux:   /etc/hosts
```

以 Windows 为例，用管理员身份打开记事本，打开 `C:\Windows\System32\drivers\etc\hosts`，在末尾加一行：

```
127.0.0.1    myjinqiu.com
```

保存。然后在浏览器里输入 `http://myjinqiu.com:5174`——你会看到你的本地前端项目。`myjinqiu.com` 这个域名根本就没注册，别人电脑上打不开，但你的电脑上能打开——因为 hosts 文件优先于一切 DNS 查询。

**这个实验为什么重要？** 因为它揭示了 DNS 的本质：域名和 IP 之间**没有天然的绑定关系**。`baidu.com` 指向 `110.242.68.66` 不是物理定律，只是 DNS 系统里约定的记录。你本地改一下 hosts，同一个域名就能指向完全不同的地址。

这也是为什么 CDN 和负载均衡能工作——它们不停切换域名指向的 IP，用户无感知，因为"域名指向哪里"说到底就是一条可以随时修改的记录。

### 实验三：金橘记账如果用自定义域名，DNS 要配什么？

回到你正在做的金橘记账项目。假设你已经把前端部署到了 Vercel（得到一个 `jinqiu.vercel.app` 的地址），后端部署到了 Render（得到一个 `jinqiu-api.onrender.com` 的地址）。现在你买了一个域名 `jinqiu.finance`，想绑上去。

下面是 DNS 配置清单：

```
; 金橘记账 DNS 配置示例（阿里云/Cloudflare 都是这个逻辑）

; 主域名指向 Vercel（用户访问 jinqiu.finance → 打开前端页面）
类型:  A
主机记录:  @
记录值:    76.76.21.21      （Vercel 的 IP）
TTL:       600

; 或者用 CNAME（推荐，因为 Vercel 的 IP 可能会变）
类型:  CNAME
主机记录:  @
记录值:    jinqiu.vercel.app
TTL:       600

; www 子域名也指向前端
类型:  CNAME
主机记录:  www
记录值:    jinqiu.finance
TTL:       600

; API 子域名指向后端
类型:  CNAME
主机记录:  api
记录值:    jinqiu-api.onrender.com
TTL:       600
```

配完之后的效果：

```
jinqiu.finance        →  Vercel（前端页面）
www.jinqiu.finance    →  同上（自动跳转）
api.jinqiu.finance    →  Render（后端 API）
```

你的前端代码里，请求地址从 `https://jinqiu-api.onrender.com/api` 变成 `https://api.jinqiu.finance/api`——看起来专业了一百倍。

> 配完要等多久？DNS 传播延迟通常是几分钟到 48 小时。一般 10 分钟内就生效了。如果半天还没生效——去检查你配错了没有。

## ⚠️ 常见坑点

| 坑 | 现象 | 解法 |
|----|------|------|
| **买了域名没配 DNS** | 浏览器显示 "ERR_NAME_NOT_RESOLVED" — 域名存在但没有解析记录，就像注册了公司名但没写地址 | 去域名服务商后台加 A 记录或 CNAME 记录。最起码加一条 A 记录指向你的服务器 IP，或者 CNAME 指向部署平台给的域名 |
| **DNS 缓存没刷新** | 你刚改了 DNS 记录，别人能访问了新地址，你自己还是老地址 | 本地 DNS 缓存作怪。Windows 跑 `ipconfig /flushdns`，Mac 跑 `sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder`，Chrome 里访问 `chrome://net-internals/#dns` 清浏览器 DNS 缓存 |
| **A 记录和 CNAME 搞混** | 根域名（@）配了 CNAME 同时又配了 A 记录，解析混乱 | **根域名用 A 记录或 CNAME，不能同时配两条**。而且 CNAME 和 MX 记录不能共存于同一主机记录——这是 DNS 协议规定。如果根域名需要邮箱服务，根域名只能用 A + MX，不能用 CNAME |
| **www 和非 www 只有一个能打开** | `jiujin.com` 能打开，`www.jiujin.com` 打不开 | 两个都要配 DNS 记录。通常做法是：根域名配 A 记录指向服务器，www 配 CNAME 指向根域名。然后在服务器/Nginx/Vercel 上配 301 重定向，把所有 `www` 流量转到非 `www`（或者反过来） |
| **TTL 设太长** | 你换服务器改了 DNS，但用户 24 小时之后才能访问新 IP | TTL（Time To Live）是 DNS 记录在各级缓存里的存活时间，单位秒。`TTL=86400`（24 小时）意味着改了 DNS 之后，别人可能要等一天才能看到新地址。**迁移服务器前先把 TTL 降到 300（5 分钟）**，等迁移完确认没问题再调回 3600 |
| **子域名没配 DNS 就用** | 你给前端配了 `api.jinqiu.finance` 的 CNAME，后端代码里也用这个地址，但根本没在 DNS 后台加这条记录 | 每加一个子域名（`api.xxx.com`、`cdn.xxx.com`、`admin.xxx.com`），都要在 DNS 后台加一条对应记录。不是注册了主域名就会自动有全部子域名 |
| **HTTPS 证书跟域名绑定，换域名忘了重新申请** | 从 `jinqiu.vercel.app` 换成 `jinqiu.finance`，浏览器显示"证书无效" | SSL/TLS 证书是为**特定域名**签发的。`jinqiu.vercel.app` 的证书不能用在 `jinqiu.finance` 上。Vercel/Render 会自动为新域名申请证书，但如果是自建服务器，要用 `certbot --nginx -d jinqiu.finance -d www.jinqiu.finance` 重新申请 |

### 坑点详解：DNS 缓存 — 为什么你改完了"看不到效果"

这个坑值得多写几句，因为太常见了。

你改了一条 DNS 记录——比如把金橘记账的域名从旧 Vercel 项目指向新 Vercel 项目。你 ping 一下，发现还是老的 IP。你刷新网页，还是老的页面。你开始怀疑人生——我改了吗？我改对了吗？

DNS 的"不可见性"就在这里。你以为改了一个配置，全世界马上就变。但 DNS 记录在每一层都可能被缓存：

```
你的浏览器 DNS 缓存     ← 缓存几十秒到几分钟
操作系统 DNS 缓存       ← 缓存 TTL 时间
路由器 DNS 缓存         ← 缓存 TTL 时间
运营商 DNS 服务器缓存   ← 缓存 TTL 时间
别的运营商 DNS 服务器   ← 各自缓存，独立刷新
```

你改了权威 DNS 上的记录，但每一层缓存都还保留着旧值——在 TTL 到期之前，它们不会来问你这个新值。

**解决链路：**

```
第 1 步：清浏览器 DNS 缓存（chrome://net-internals/#dns）
第 2 步：清系统 DNS 缓存（ipconfig /flushdns）
第 3 步：用 nslookup 指定公共 DNS 查，绕过运营商缓存：
         nslookup jinqiu.finance 8.8.8.8
         （8.8.8.8 是 Google 公共 DNS，缓存刷新快）
第 4 步：如果 nslookup 返回了新 IP，说明权威 DNS 改对了，其他层在等缓存过期
第 5 步：如果 nslookup 还是老 IP，说明权威 DNS 根本就没改成功，回后台检查
```

大部分时候到第 2 步就解决了。"改完没效果"不是出了 bug，是缓存没清。

## 🎯 面试会问

- **Q：从输入 URL 到页面展示，DNS 查询的具体过程是什么？**
  **答：** DNS 查询是一个**递归 + 迭代**的过程。浏览器首先检查自己的 DNS 缓存（几十秒），没有就查操作系统 hosts 文件，没有就问本地 DNS 服务器（你的路由器或宽带运营商）。本地 DNS 也没有的话，代理你的请求出去问：先问根 DNS 服务器（全球 13 组）——根 DNS 不知道具体 IP，但知道 `.com` 的管理者是谁，返回顶级域 DNS 地址。再问 `.com` 的顶级域 DNS——它不知道 `baidu.com` 的 IP，但知道 `baidu.com` 的权威 DNS 地址。最后问权威 DNS 服务器——它返回真正的 IP 地址。整个查询链是：**浏览器缓存 → hosts → 本地 DNS → 根 DNS → 顶级域 DNS → 权威 DNS**。每一层只做增量查询，谁也不知道全貌。面试加分说法："跟我前端写 `import { TabBar } from '@/components/TabBar'` 时 Vite 的路径解析算法一模一样——`vite.config.ts` alias → `node_modules` → `package.json` main/exports → 实际文件，每一层只做一步翻译。"

- **Q：TCP 和 UDP 的区别是什么？DNS 用哪个？**
  **答：** TCP 是面向连接的可靠传输——三次握手建立连接，有序传输，丢包重传。像打一个正式电话，接通了才开始说。UDP 是无连接的不可靠传输——直接发包，不建立连接，丢包不管。像发一条微信语音，发了就发了，对方收没收到不管。DNS 查询**默认用 UDP**，端口 53。为什么？因为 DNS 查询通常只有一个请求包和一个响应包，数据量很小（几十到几百字节）。用 TCP 要先三次握手再发数据再四次挥手——一次 DNS 查询变成 9 个包，用 UDP 只要 2 个包。面试追问"那为什么不用 UDP？"答：当响应数据超过 512 字节（UDP 单包限制）时，DNS 会自动切换到 TCP。比如 DNSSEC（DNS 安全扩展）的签名信息很大，通常会触发 TCP fallback。所以 DNS 是**优先 UDP，必要时 TCP**。

- **Q：什么是 CDN？CDN 和 DNS 有什么关系？**
  **答：** CDN（Content Delivery Network）是内容分发网络——把静态文件（HTML、CSS、JS、图片）缓存到全球各地的边缘节点，让用户从物理距离最近的节点取文件。CDN 和 DNS 的关系在于：**CDN 依赖 DNS 来实现"就近访问"**。你的域名 `jinqiu.finance` 加了 CDN 之后，权威 DNS 不再返回一个固定的服务器 IP，而是**根据请求来源的 IP 地址**（用户在哪个城市），返回离用户最近的 CDN 节点 IP。广州用户查询 `jinqiu.finance`，DNS 返回香港节点的 IP（延迟 30ms）；伦敦用户查询同一个域名，DNS 返回伦敦节点的 IP（也是 30ms）。这叫"智能 DNS 解析"或"GeoDNS"——同一个域名，不同地方的人解析出不同的 IP。Vercel 内置了这一套机制，你 CNAME 指向 `jinqiu.vercel.app` 之后，Vercel 的 DNS 自动处理地理路由。

- **Q：A 记录和 CNAME 有什么区别？什么时候用什么？**
  **答：** A 记录把域名直接映射到 IPv4 地址（`jinqiu.finance → 76.76.21.21`），CNAME 把域名映射到另一个域名（`www.jinqiu.finance → jinqiu.finance`）。区别在于：A 记录指向的是一个**固定的 IP 地址**，如果服务器 IP 变了，你要手动改 A 记录。CNAME 指向一个**域名别名**，别名指向的 IP 可以随时变，你不需要改。实践建议：**部署在 Vercel/Render 等平台时，永远用 CNAME**，因为平台的 IP 可能会变，但 `xxx.vercel.app` 这个域名永远有效。如果是你自己买的云服务器（固定公网 IP），用 A 记录。语法注意：根域名（`@`）配 CNAME 在某些服务商（如 Cloudflare）是允许的（叫 CNAME flattening），但在标准 DNS 协议里，根域名有 CNAME 就不能有其他记录类型（比如 MX）。如果需要根域名同时有邮箱服务和 CNAME，用 A 记录 + Cloudflare 代理等方式解决。

## 📝 小结

这篇文章我们搞懂了 DNS 怎么把一个好看的域名变成一串 IP 数字：

1. **DNS 查询链**：浏览器缓存 → hosts → 本地 DNS → 根 DNS → 顶级域 DNS → 权威 DNS。7 层递归，每一层只做增量查询。跟你前端的 import 路径解析一模一样。
2. **A 记录 vs CNAME**：A 记录指 IP（`域名 → 76.76.21.21`），CNAME 指另一个域名（`www → 根域名`）。部署在 Vercel 用 CNAME，自建服务器用 A 记录。根域名上别把 CNAME 和 MX 搞一起。
3. **DNS 缓存会骗你**：改完记录没效果？先 `ipconfig /flushdns`，再 `nslookup xxx.com 8.8.8.8` 确认。切换服务器前把 TTL 降到 5 分钟，迁移完了再调回去。

下一期我们聊**第 4 篇：部署上线的第一个实操——前端部署到 Vercel**。从今天讲的理论到明天你手上敲的 `git push`，DNS 加 CNAME 绑定自定义域名，HTTPS 证书自动签发——你把今天学的 DNS 知识用在真正的部署里，你会发现：那个 A 记录、那个 CNAME，不再是文档里的概念，是你亲手填的表单。

---

## 🎨 本文配图提示词

**封面图（16:9，Midjourney）：**

```
A split-screen illustration of DNS resolution. Left side: a person typing "jinqiu.finance" into a browser address bar, looking curious. Right side: the browser address bar transforms into a magnifying glass, emitting a search beam that passes through 6 layers of "phone books" stacked vertically — each labeled Browser Cache, hosts file, Local DNS, Root DNS, TLD DNS, Authoritative DNS — and finally landing on a glowing server rack with an IP address "76.76.21.21" shining above it. The phone books get progressively larger from top to bottom, symbolizing the widening search scope. Warm color palette: amber #F59E0B accent, warm white #FFF8F0 background, dark slate #1F2937 text. Flat illustration style, clean lines, tech article cover art. --ar 16:9 --style digital --v 6
```

**文中配图（4:3，Midjourney）：**

```
A comparison illustration showing DNS query chain vs frontend import resolution side by side. Left column: "DNS Resolution" with 7 stacked cards — Browser Cache → hosts → Local DNS → Root DNS → TLD DNS → Authoritative DNS → IP Address 76.76.21.21. Right column: "Import Resolution" with 4 stacked cards — vite.config.ts alias → node_modules → package.json (main/exports) → ./es/index.js. Arrows connecting each step downward. A developer figure at the bottom of each column receiving the result. Warm color palette, clean diagram style, educational tech content. --ar 4:3 --style digital --v 6
```

**中文版（DALL-E 可用）：**

```
一张DNS解析过程的分屏插画。左边是一个人往浏览器地址栏输入"jinqiu.finance"，表情好奇。右边是地址栏变成一个放大镜，发出一道搜索光束，穿过6层垂直堆叠的"电话簿"——每层分别标注：浏览器缓存、hosts文件、本地DNS、根DNS、顶级域DNS、权威DNS——最后光束落在一台发光的服务器机架上，上方闪耀着IP地址"76.76.21.21"。电话簿从上到下越来越大，象征搜索范围不断扩大。旁边有一个小标注："这跟import路径解析一模一样"。暖色调扁平风格，适合技术专栏封面。16:9比例。
```

—— 阿珊，前端开发者 & 全栈学习者
