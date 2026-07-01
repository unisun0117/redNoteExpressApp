# 第5篇：后端部署 — Render 跑 FastAPI

> Render 部署 FastAPI 比你想象的简单——不需要配 Nginx，不需要 SSH 登录服务器。把代码推上去，它帮你搞定一切。

## 📌 前端为什么要学这个？

我第一次部署后端的时候，感觉像是进入了另一个世界。

SSH 登录服务器，黑底白字的终端，手敲命令装 Python、装依赖、配 Nginx 反向代理、用 screen 让进程后台运行。每次代码更新了，要杀进程、重新启动、再祈祷别出错。那段时间我特别抗拒部署——我一个前端，写写 React 不好吗？为什么非要学这些运维的黑话？

转折发生在我发现了 Render。

Render 是什么？把它理解成**Vercel 的后端版**就对了。Vercel 帮你部署前端——你 push 代码，它自动 `npm install` + `npm run build` + 分配域名 + HTTPS。Render 帮你部署后端——你 push 代码，它自动读 `requirements.txt`、创建虚拟环境、`pip install`、`uvicorn` 启动、分配域名 + HTTPS。

一模一样的东西，只是一个管前端，一个管后端。

那次我终于在后端部署成功之后，打开 Postman 发了一个 `GET /api/health`，看到返回 `{"status": "ok"}`——那种感觉，跟你第一次 `npm run dev` 看到页面出来是同一个心情。

> 阿珊说：后端部署最爽的时刻不是部署成功，是前端终于连上了、数据终于从数据库查出来了——那一刻你才真的相信，你写的是一个"活着的"应用。

## 🔍 核心原理

### Render 在你 push 代码之后做了什么？

你把代码推到 GitHub，Render 检测到变更，自动执行以下步骤：

```
1. 检测项目类型 → 发现 requirements.txt → 判定为 Python 项目
2. 创建虚拟环境（相当于你本地跑 python -m venv venv）
3. pip install -r requirements.txt（安装所有依赖）
4. 执行你配置的 Start Command：uvicorn app.main:app --host 0.0.0.0 --port $PORT
5. 分配一个域名：https://你的服务名.onrender.com
6. 自动配置 HTTPS 证书
```

把这段流程翻译成前端概念，就是：

| Render 做的事 | 前端类比 |
|-------------|---------|
| 检测 `requirements.txt` | Vercel 检测 `package.json` |
| `pip install` | `npm install` |
| `uvicorn app.main:app` | `npm run dev` 或 `vite` |
| 分配域名 + HTTPS | Vercel 分配 `xxx.vercel.app` |
| 环境变量面板 | Vercel 的 Environment Variables 设置页 |
| 日志面板 | Vercel 的 Deployments → Build Logs |
| 自动部署（git push 触发） | 一模一样 |

说白了，Render 就是一个**自动化的运维脚本**。以前你需要手动 SSH 上去敲的那十几条命令，Render 帮你一条条自动执行了。

### 为什么不是 Vercel 部署后端？

你可能会问：Vercel 也能跑 Serverless Function，为什么不用 Vercel 部署后端？

Vercel 的 Serverless Function 本质是**无状态的短时函数**——最长执行 10 秒、不能保持 WebSocket 连接、每次调用都可能在不同机器上执行。对于 FastAPI 这种需要长时间运行的 Web 框架，Serverless 不是最佳选择。

Render 的 Web Service 是**长期运行的进程**——它可以保持数据库连接池、可以做 WebSocket、进程不会因为没人访问就"缩容到 0"（不过免费版会休眠，后面讲）。

| 特性 | Vercel Serverless | Render Web Service |
|------|------------------|-------------------|
| 运行方式 | 按需启动函数 | 持续运行的进程 |
| 执行时长 | 最长 10-60 秒 | 无限制 |
| WebSocket | 不完全支持 | 完全支持 |
| 数据库连接池 | 每次新建连接 | 可以复用 |
| 适合场景 | API 路由、SSR | 完整后端服务 |

### FastAPI 入口：`app.main:app` 是什么意思？

Render 的启动命令里有一个关键参数：`uvicorn app.main:app`。这个 `app.main:app` 是什么意思？

```
uvicorn app.main:app
        ^^^^^^^^ ^^^
           |      |
           |      +-- FastAPI 实例的变量名（在 main.py 里 app = FastAPI()）
           |
           +-- Python 模块路径（app/main.py）
```

在金橘记账的项目里，这就是：

```python
# backend/app/main.py
app = FastAPI(title="金橘记账 API", version="0.1.0")
#   ^^^
#   这个变量名
```

所以 `app.main:app` 的意思是：去 `app` 包下面找 `main.py`，用里面的 `app` 这个变量（FastAPI 实例）来启动。

## 🛠 动手试试

下面我带着你把金橘记账部署到 Render。

### 第一步：确认项目结构

Render 要求项目根目录下有 `requirements.txt`。金橘记账的项目已经有这个文件了：

```txt
# backend/requirements.txt
fastapi>=0.110.0
uvicorn[standard]>=0.27.0
sqlalchemy>=2.0.0
pydantic>=2.0.0
pydantic-settings>=2.0.0
python-jose[cryptography]>=3.3.0
passlib[bcrypt]>=1.7.0
python-multipart>=0.0.9
bcrypt==4.0.1
```

注意 `uvicorn[standard]` 这个写法。方括号里的 `standard` 表示安装 uvicorn 的"标准扩展包"——包含了一些性能优化和 WebSocket 支持的额外依赖。如果你只写 `uvicorn`，生产环境可能会缺少必要的组件。

### 第二步：创建 Render Web Service

1. 登录 [render.com](https://render.com)，用 GitHub 账号注册。
2. 点击右上角 **New** → **Web Service**。
3. 授权 Render 访问你的 GitHub 仓库，选择 `finance-tracker`。
4. 填写配置：

| 配置项 | 值 | 说明 |
|-------|----|------|
| **Name** | `finance-tracker-api` | 会生成域名 `finance-tracker-api.onrender.com` |
| **Region** | `Singapore` | 离国内最近的机房 |
| **Branch** | `master` | 推这个分支自动触发部署 |
| **Root Directory** | `backend` | **重要！**因为 requirements.txt 在 backend/ 下 |
| **Runtime** | `Python 3` | Render 自动检测 |
| **Build Command** | `pip install -r requirements.txt` | 也可以用默认值 |
| **Start Command** | `uvicorn app.main:app --host 0.0.0.0 --port $PORT` | **$PORT 不能改！** |

启动命令里有两个关键点：

```bash
uvicorn app.main:app --host 0.0.0.0 --port $PORT
#                    ^^^^^^^^^^^^^^^^   ^^^^^^^^^^^
#                    监听所有网络接口     Render动态分配的端口
```

- `--host 0.0.0.0`：让服务监听所有网络接口。如果写成 `127.0.0.1`，外部访问不了。
- `--port $PORT`：**必须用环境变量 `$PORT`**。Render 会动态分配一个端口号（比如 10000），你不能写死 8000。写了 8000 部署不会报错，但健康检查会一直失败。

### 第三步：配环境变量

在 Render 的 **Environment** 标签页添加：

| Key | Value | 说明 |
|-----|-------|------|
| `DATABASE_URL` | `sqlite:///./finance.db` | SQLite 文件路径（注意是 3 个斜杠） |
| `JWT_SECRET` | `你的随机字符串` | 生产环境不要用 dev-secret |
| `ALLOWED_ORIGINS` | `https://你的前端域名.vercel.app` | 允许前端跨域访问 |
| `PYTHON_VERSION` | `3.10.0` | 锁定 Python 版本 |

配置完环境变量，点 **Deploy Web Service**。

### 第四步：等待部署 + 验证

部署过程大概 3-5 分钟。你可以在 Logs 面板看到实时日志：

```
==> Build started
==> Installing dependencies with pip...
==> Build successful
==> Deploying...
==> Your service is live at https://finance-tracker-api.onrender.com
```

拿到 URL 之后，马上测试：

```bash
# 测试健康检查
curl https://finance-tracker-api.onrender.com/api/health
# 返回："{"status":"ok"}"

# 测试一个业务接口
curl https://finance-tracker-api.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

健康检查端点非常重要——它是 Render 判断你的服务是否"活着"的依据。金橘记账里已经有了：

```python
# backend/app/main.py
@app.get("/api/health")
def health():
    return {"status": "ok"}
```

### 第五步：更新前端的 API 地址

后端 URL 变了，前端要更新环境变量：

```bash
# Vercel 环境变量
VITE_API_URL=https://finance-tracker-api.onrender.com/api
```

或者在本地 `.env` 里：

```env
# frontend/.env
VITE_API_URL=https://finance-tracker-api.onrender.com/api
```

改完之后重启前端，打开浏览器，操作一下——你应该能看到数据从 Render 上的后端查出来了。

## ⚠️ 常见坑点

| 坑 | 现象 | 前端类比 | 解法 |
|----|------|---------|------|
| **端口写死 8000** | 日志显示启动成功，但健康检查一直失败 | 像你 `fetch('localhost:5173')` 但 Vite 实际跑在 5174 | `--port $PORT`，必须用环境变量，数字由 Render 决定 |
| **Root Directory 没设** | 构建失败：`requirements.txt not found` | Vercel 的 Root Directory 设错了，找不到 package.json | 在 Render 设置里把 Root Directory 设为 `backend` |
| **requirements.txt 缺依赖** | 部署成功但一调接口就 500 | `npm install` 漏了包，跑起来才发现报错 | 本地用 `pip freeze > requirements.txt` 抓全所有依赖。但不要全盘复制，手动检查去掉没用到的 |
| **15 分钟休眠** | 长时间不访问，第一次请求要等 1-3 分钟 | SPA 懒加载——第一次打开路由要等一下 | 用 UptimeRobot（免费）每 10 分钟访问一次 `/api/health` 保活 |
| **CORS 没更新** | 前端调接口报 `Access-Control-Allow-Origin` 错误 | 后端 CORS 白名单没加 Render 域名 | 把 Render 域名加到 `ALLOWED_ORIGINS` 里：`https://finance-tracker-api.onrender.com` |
| **SQLite 路径问题** | 重启后数据全丢了 | localStorage.clear() 的效果 | 注意：Render 免费版的文件系统不是持久化的。SQLite 数据在重启后会丢失。生产环境建议换成 PostgreSQL（Render 自带免费的 PostgreSQL 服务） |
| **环境变量忘了配** | `JWT_SECRET` 用了默认值，部署后 token 验证失败 | 你本地 `.env` 有值，线上没配 | Render 的 Environment 页逐个检查。建议装一个浏览器插件截图保存配置 |
| **bcrypt 版本冲突** | `passlib` 报错 `bcrypt` 找不到 | `npm install react@19` 但代码用了 v18 的 API | `requirements.txt` 里锁定 `bcrypt==4.0.1`，不要装 5.x |

### 休眠问题的完整解释

Render 免费版有一个最重要的限制：**15 分钟内没有收到任何 HTTP 请求，服务会进入休眠状态**。下次有请求来的时候，Render 会"唤醒"服务——这个过程需要 1-3 分钟。

前端用户点了按钮，等了 2 分钟才返回——这体验当然不行。

解决方案：UptimeRobot。它是一个免费的服务监控工具，可以设置每 10 分钟访问一次你的 `/api/health` 端点。这样服务永远不会连续 15 分钟无请求，也就不会休眠了。

1. 注册 [uptimerobot.com](https://uptimerobot.com)
2. 添加 Monitor → HTTP(s)
3. URL 填 `https://你的服务.onrender.com/api/health`
4. 监控间隔选 **10 分钟**
5. 保存

免费的，一分钟配好，一劳永逸。

> 阿珊说：部署最怕的不是第一次部署失败，是你改了代码、推上去、发现线上挂了、又找不出原因。所以每次部署前先确认三件事：环境变量配齐了没、CORS 域名加了没、`/api/health` 返回 ok 了没——这三步验证，比任何调试都管用。

## 🎯 面试会问

**Q1：PaaS 和 IaaS 有什么区别？Render 属于哪种？**

**答：** 

IaaS（Infrastructure as a Service）是给你一台裸机——你需要自己装操作系统、装运行时、配网络。AWS EC2 就是典型的 IaaS。你租了一台云服务器，但上面什么都没有，一切从零开始。

PaaS（Platform as a Service）是给你一个平台——你只管上传代码，平台帮你搞定运行环境。Render、Vercel、Heroku 都是 PaaS。

打个前端的比方：IaaS 像你买了一块地，自己盖房子；PaaS 像你租了一间装修好的公寓，拎包入住。

Render 属于 PaaS。你 push 代码，它自动检测 Python 项目、自动 `pip install`、自动 `uvicorn` 启动、自动配置 HTTPS——你不需要知道服务器长什么样。

实际面试中，你可以补充一句项目经验："金橘记账的后端我就是部署在 Render 上的。免费版有 15 分钟休眠限制，我用 UptimeRobot 每 10 分钟发一次健康检查请求来保活。这个解决方案在面试官看来说明你不是只部署过，还处理过生产环境的问题。"

**Q2：什么是"无服务器"？Serverless 真的没有服务器吗？**

**答：**

Serverless（无服务器架构）并不是真的没有服务器——只是你**不需要管理服务器**。服务器仍然存在，但由云平台全权管理。你只上传函数代码，平台负责在请求来的时候启动函数、执行、返回结果、然后销毁。

Vercel 的 Serverless Function 就是典型的 Serverless。每次请求触发一个独立函数执行，函数执行完就销毁，中间的状态不留存。

与 PaaS 的区别：PaaS 是一个**持续运行的进程**（比如 Render 上你的 FastAPI 进程一直在跑），Serverless 是**按需启动的函数**（来了请求才启动，处理完就释放）。

面试如果追问"那 Serverless 的优势是什么"：
- 节省成本：没人访问的时候不收费（Render 免费版的休眠也是这个思路）
- 自动扩缩：100 个并发请求平台自动开 100 个函数实例，你不需要手动配置负载均衡
- 零运维：服务器操作系统更新、安全补丁全部由平台负责

**Q3：反向代理是干嘛的？为什么部署后端通常要配 Nginx？**

**答：**

反向代理是部署领域最常被问到的一个概念。

正向代理：你（客户端）通过代理访问外部的服务器。比如公司 VPN——你访问 Google，请求先经过公司的代理服务器。

反向代理：外部用户请求先到代理服务器，代理再转发给后端的应用服务器。Nginx 就是做这个的。

为什么需要反向代理？
1. **负载均衡**：把请求分发到多台服务器
2. **静态文件服务**：让 Nginx 直接返回静态文件，不用经过应用服务器
3. **SSL 终止**：HTTPS 加密在 Nginx 层处理，后端应用只需要处理 HTTP
4. **安全隔离**：外部用户永远不知道后端跑了几个进程、在哪个端口

前端类比：Vite dev server 的 proxy 配置就是你最熟悉的反向代理。

```typescript
// vite.config.ts — 前端开发中最常见的反向代理
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8001',  // 反向代理的目标
        changeOrigin: true,
      }
    }
  }
})
```

浏览器请求 `/api/users` → Vite 代理收到 → 转发到 `localhost:8001/api/users` → 返回给浏览器。这就是反向代理——用户不知道数据真正来自 8001 端口。

**但是**，Render 会自动处理反向代理和 SSL，你不需要手动配 Nginx。这也是为什么标题说"不需要配 Nginx"——PaaS 平台帮你做了这件事。

## 📝 小结

这篇文章的核心就三句话：

1. **Render 是 Vercel 的后端版**——你 push 代码，它自动安装依赖、启动服务、分配域名、配置 HTTPS。
2. **端口必须用 `$PORT`，绝对不能写死**——这是 Render 部署最经典的坑。
3. **免费版 15 分钟休眠用 UptimeRobot 解决**——花一分钟配置，省去所有用户等待。

金橘记账的后端现在就跑在 Render 上。前端连上去，数据从数据库查出来，在页面上一条一条渲染出来——这就是一个完整的全栈应用了。从前端到后端，从写出第一行代码到部署上线被人用起来，这条路你走通了。

下一篇是 **第6篇：前端部署 — Vercel + 自定义域名**。我会带你把金橘记账的前端部署到 Vercel，绑上你自己的域名，再加上 HTTPS 全链路打通——前后端一起上线，才算一个完整的项目。

—— 阿珊，前端开发者 & 全栈学习者

## 🎨 本文配图提示词

**封面图（16:9）：**

```
An illustration showing the contrast between old-school and modern backend deployment. Left side: a stressed developer SSH-ing into a terminal with black screen, typing complex commands, configuring Nginx config files, looking exhausted. Right side: the same developer relaxed, just doing "git push" from VS Code, and a Render dashboard showing green "Deploy Successful" status. A big arrow between them labeled "From 2 hours to 2 minutes". The Render side shows a glowing FastAPI logo and a URL "xxx.onrender.com" with HTTPS lock icon. Warm color palette with amber (#F59E0B) accents, flat illustration style, clean tech article cover. --ar 16:9 --style digital --v 6
```

**文中配图（4:3）：**

```
A "What Render Does After Git Push" step-by-step flow illustration. 6 steps in horizontal sequence, each as a card with icon and text: Step 1 "Detect requirements.txt" (document icon), Step 2 "Create venv" (Python logo in a box), Step 3 "pip install" (downloading arrow), Step 4 "uvicorn start" (rocket launching), Step 5 "Assign domain .onrender.com" (globe with link), Step 6 "HTTPS auto" (lock shield). Below the flow, a comparison row: "Just like Vercel does npm install + npm run build + deploy" with Vercel-style icons. Warm background (#FFF8F0), clean tech diagram style. Title: "What Happens After You git push". --ar 4:3 --style digital --v 6
```

**中文版（DALL-E 可用）：**

```
一张传统部署 vs 现代部署的对比插画。左边是一个前端开发者面对黑底白字的终端，手敲SSH命令、配Nginx、表情痛苦。右边是同一个开发者轻松地 git push 代码，屏幕上Render面板显示"部署成功"，一个绿色的勾。中间一个大箭头标注"从2小时到2分钟"。右边画面里有FastAPI的logo，一个带着HTTPS锁的域名URL。暖色调，琥珀色点缀，扁平插画风格，适合技术专栏封面。
```
