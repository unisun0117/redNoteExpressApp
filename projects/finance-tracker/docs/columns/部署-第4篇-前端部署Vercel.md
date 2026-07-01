# 第4篇：前端部署 — Vercel 一键上线 React 应用

> Vercel 部署比你想象的简单：连上 GitHub 仓库，push 代码，自动上线。比 `npm run dev` 还省事。

## 📌 前端为什么要学这个？

我第一个项目用的是阿里云 ECS 手动部署。流程是这样的：本地 `npm run build`，打开 FileZilla，把 dist 文件夹拖到服务器上，SSH 连上去改 Nginx 配置，`nginx -s reload`。每次改个文案都要重新来一遍——拖文件、改配置、重启，三个步骤来回倒腾。改一次文案五分钟，改十次就是一小时，生产效率低到令人发指。

后来我发现了 Vercel。连上 GitHub 仓库，`git push` 一下，它自动帮你 build、自动部署、自动配 CDN、自动给 HTTPS 证书。全程不用操作服务器，甚至不用打开终端——直接在 GitHub 网页上改个文案，保存，30 秒后线上就更新了。

第一次用的时候感觉像从绿皮火车换成了高铁。你只需要写代码，部署这件事——交给 Vercel。

> **阿珊说：前端部署的最高境界，不是"我会配 Nginx"，而是"我根本不用配"。Vercel 把部署简化到了跟 `npm run dev` 一样轻松的地步。**

## 🔍 核心原理

### Vercel 到底做了什么？

别被"自动化部署"这个词吓到。Vercel 本质上就是一个**把源代码变成静态文件，然后分发到全球 CDN 的流水线**。拆开来看就四步：

```
git push → Vercel 检测到变更
                ↓
        检测框架（React/Vue/Next...）
                ↓
        npm install + npm run build
                ↓
        产出静态文件（dist/ 目录）
                ↓
        分发到全球 CDN 节点
                ↓
        返回一个 xxx.vercel.app 域名
```

用前端的视角来看，Vercel 做的事情跟你本地 `npm run dev` 之后的流程一样——只不过它多做了三步：

| 你本地做什么 | Vercel 替你做什么 | 前端类比 |
|-------------|------------------|---------|
| `npm install` | 自动装依赖，而且有缓存，第二次秒装 | 跟本地一样 |
| `npm run build` | 执行你配的 Build Command | 跟本地一样 |
| 手动打开 `dist/index.html` | 自动部署到 CDN，全球可访问 | 像把 `dist/` 丢到一个全球共享文件夹 |
| 本地 `localhost:5173` | 给你一个 `xxx.vercel.app` 域名 | 从 localhost 变成公网 URL |
| — | 自动申请 HTTPS 证书 | 别人帮你配好了 SSL |
| 改代码要手动刷新 | git push 自动触发重新部署 | 像 Webpack HMR 但作用域是整个生产环境 |

> **前端类比：Vercel 就像把你的 `dist/` 文件夹复制粘贴到了世界各地的服务器上，用户访问时自动取离他最近的那一份——你只负责产出 `dist/`，剩下的它全包了。**

### CDN 是什么？

你前端写 `import { Button } from 'antd-mobile'`，antd-mobile 的包从 npm 下载到你的 `node_modules`。如果你的用户在中国广州，服务器在美国加州，每次请求跨半个地球传输文件——能不慢吗？

CDN（Content Delivery Network）做的事情就是：把你的静态文件（HTML、CSS、JS、图片、字体）复制到全球几百个边缘节点。用户在广州访问，就从香港或广州的节点取文件；用户在伦敦访问，就从伦敦的节点取。物理距离近，速度快。

```
没有 CDN：用户(广州) ──── 跨越太平洋 ──── 服务器(加州) ── 延迟 300ms
有 CDN：  用户(广州) ──── 本地 ──── CDN节点(香港) ── 延迟 30ms
```

Vercel 部署时自动把你的文件推送到它的全球 CDN（背后是 AWS CloudFront），你不用做任何配置。

## 🛠 动手试试

### 金橘记账 Vercel 部署实战

金橘记账是一个 monorepo 项目，前端在 `projects/finance-tracker/frontend` 目录下。Vercel 默认打包根目录，需要额外配置 Root Directory。下面是完整步骤。

#### 第一步：确保项目能本地 build 成功

先别急着连 Vercel。本地先跑一遍 build，确保不报错：

```bash
cd projects/finance-tracker/frontend
npm install
npx vite build
```

如果 build 通过，会在 `frontend/dist/` 下生成静态文件。这是部署的前提——别把报错的项目推给 Vercel。

金橘记账的 `package.json` 里 build 脚本长这样：

```json
{
  "scripts": {
    "dev": "vite",
    "build": "npx vite build",
    "preview": "vite preview"
  }
}
```

之所以用 `npx vite build` 而不是 `tsc -b && vite build`，是因为 TypeScript 6.x 的 `verbatimModuleSyntax` 在 `tsc -b` 时会报错——但 Vite 用 esbuild 编译 TypeScript，不经过 tsc，所以直接用 `npx vite build` 就行。

#### 第二步：连上 GitHub

把代码推到 GitHub 仓库。Vercel 只支持 Git 触发部署——GitHub、GitLab、Bitbucket 都行。金橘记账用的是 GitHub。

#### 第三步：在 Vercel 创建项目

1. 打开 [vercel.com](https://vercel.com)，用 GitHub 账号登录
2. 点击 **New Project**
3. 选择金橘记账的 GitHub 仓库
4. **关键配置来了**，往下看：

#### 第四步：配置构建参数

因为金橘记账是 monorepo，前端不在根目录，需要配置三个关键字段：

| 配置项 | 值 | 为什么 |
|--------|-----|--------|
| **Root Directory** | `projects/finance-tracker/frontend` | Vercel 默认从根目录找 `package.json`，monorepo 需要指定子目录 |
| **Build Command** | `npx vite build` | 不能写 `npm run build`（如果 package.json 里的 build 脚本包含 `tsc -b` 会报错） |
| **Output Directory** | `dist` | Vite 默认输出目录，Vercel 自动识别大多数框架，但手动指定更稳 |
| **Install Command** | `npm install` | 默认就是这个，可以不改 |

配置截图（文字版）：

```
Root Directory:   projects/finance-tracker/frontend
Build Command:    npx vite build
Output Directory: dist
```

#### 第五步：配环境变量

金橘记账前端需要知道后端 API 地址，这个地址通过环境变量 `VITE_API_URL` 注入。

在 Vercel 项目设置里，找到 **Environment Variables**，添加：

| Key | Value | Environments |
|-----|-------|-------------|
| `VITE_API_URL` | `https://你的后端地址.com/api` | Production, Preview |

> **重点：** Vite 只会在 build 时把 `VITE_` 前缀的环境变量打包进代码。运行时改环境变量不会生效——代码已经编译成静态文件了。所以每次改环境变量后要重新部署（也就是重新 build）。

前端代码里这样读取：

```typescript
// frontend/src/services/api.ts
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8001/api";
```

`import.meta.env.VITE_API_URL` 在 build 时会被替换成真正的字符串值。本地开发如果没设这个环境变量，就 fallback 到 `localhost:8001`。

#### 第六步：处理 SPA 路由（vercel.json）

金橘记账用 React Router，路由都在前端处理（`/list`、`/form`、`/budget` 等）。但 Vercel 默认只认物理文件——你访问 `/list`，服务器去找 `dist/list/index.html`，找不到就返回 404。

这需要配置 SPA rewrites：所有路由都返回 `index.html`，让 React Router 在前端处理。

在 `frontend/` 目录下创建 `vercel.json`：

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

这句话的意思：不管用户访问什么路径（`/list`、`/form`、`/budget/123`），都返回 `index.html`。React Router 在浏览器里读取当前路径，渲染对应的页面组件。

> **前端类比：这就跟你 `npm run dev` 时 Vite dev server 做的事情一样——不管访问什么路径，都返回 `index.html`，让前端路由接管。**

#### 第七步：部署！

点击 **Deploy**。Vercel 会：

1. Clone 你的仓库
2. 进入 `projects/finance-tracker/frontend` 目录
3. 执行 `npm install`
4. 执行 `npx vite build`
5. 把 `dist/` 部署到 CDN
6. 返回一个 `xxx.vercel.app` 域名

第一次部署大约 2-3 分钟。之后每次 `git push` 自动重新部署，大约 30-60 秒。你可以在 Vercel Dashboard 看到实时构建日志，跟本地终端输出一模一样。

#### 最终的项目文件结构

```
projects/finance-tracker/frontend/
├── package.json          # 依赖 + scripts
├── vercel.json           # SPA rewrites 配置
├── index.html            # Vite 入口 HTML
├── vite.config.ts        # Vite 配置（可选）
├── src/
│   ├── main.tsx
│   ├── App.tsx           # React Router 路由
│   ├── services/api.ts   # API 封装（读取 VITE_API_URL）
│   ├── hooks/useAuth.tsx
│   ├── pages/
│   └── components/
└── dist/                 # build 产物（Vercel 自动生成后部署）
```

## ⚠️ 常见坑点

| 坑 | 现象 | 解法 |
|----|------|------|
| **Root Directory 没设** | 构建失败，报 "No package.json found" | 在 Vercel 项目设置里把 Root Directory 设为 `projects/finance-tracker/frontend` |
| **环境变量忘配** | 前端请求发到 `localhost:8001`，线上 404 | 在 Vercel Dashboard → Settings → Environment Variables 添加 `VITE_API_URL`，然后重新部署 |
| **Build Command 用 `npm run build`** | TypeScript `verbatimModuleSyntax` 导致 `tsc -b` 报错 | Build Command 直接用 `npx vite build`（esbuild 编译 TS，不经过 tsc） |
| **SPA 路由刷新 404** | 访问 `/list` 直接 404，但从首页点进去正常 | 创建 `vercel.json` 配 rewrites，所有路由指向 `index.html` |
| **改环境变量不生效** | 在 Vercel 改了 `VITE_API_URL`，但前端还是旧值 | Vite 在 build 时把环境变量内联到代码里。改环境变量后必须重新部署（触发一次新的 build） |
| **CORS 报错** | 前端 Vercel 域名访问后端 API 被浏览器拦截 | 后端要配 CORS，把 Vercel 域名加到 `ALLOWED_ORIGINS`。或者用 Vercel 的 rewrites 做 API 代理（进阶用法） |
| **免费额度超额** | 带宽用超了，部署被暂停 | Vercel 免费版每月 100GB 带宽，个人项目完全够用。但如果被恶意刷流量，去 Dashboard 加密码保护或者配 Rate Limit |

### 坑点详解：SPA 路由 404

这个坑值得多写几句。金橘记账有 6 个页面路由：

```typescript
// App.tsx
<Route path="/" element={<DashboardPage />} />
<Route path="/list" element={<ListPage />} />
<Route path="/form" element={<FormPage />} />
<Route path="/budget" element={<BudgetPage />} />
<Route path="/profile" element={<ProfilePage />} />
```

用户从首页 `/` 点进 `/list`，其实是 React Router 在内存里切换组件，浏览器没有真的向服务器发请求——所以不会 404。

但如果用户**直接刷新** `/list` 页面，浏览器会向服务器发起 `GET /list` 的请求。服务器（Vercel）去找 `/list` 这个物理文件，找不到——返回 404。

`vercel.json` 的 rewrites 解决了这个问题：

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

它的原理：**不管请求什么路径，都返回 `index.html`。React Router 在浏览器加载 `index.html` 后，读取 `window.location.pathname`，自己决定渲染哪个组件。** 服务器只负责把"入口文件"送过去，路由的事全交给前端。

## 🎯 面试会问

**Q：Vercel 部署和传统服务器部署（Nginx + ECS）有什么区别？**

**答：** 传统部署需要手动管理服务器——买 ECS、装 Nginx、配 SSL、手动上传 build 产物、配反向代理。Vercel 是 Serverless 平台，你不需要碰服务器，只需要连 GitHub 仓库，push 代码自动构建部署。核心区别在于**运维负担**：传统部署你要管 OS 更新、Nginx 配置、磁盘空间、HTTPS 证书续期；Vercel 这些全自动。但 Vercel 的灵活性不如自建服务器——比如你不能装自定义的 Nginx 模块，运维黑盒化。

**Q：CDN 是什么？为什么前端部署需要 CDN？**

**答：** CDN 把静态文件缓存到全球边缘节点，用户从最近的节点取文件，物理延迟降到最低。前端项目 build 后的产物本质是静态文件（HTML、CSS、JS、图片），天然适合 CDN 分发。没有 CDN，用户在美国访问部署在新加坡的网站延迟可能 300ms+；有 CDN，延迟降到 30ms 以内。Vercel 默认自带 CDN。

**Q：为什么 SPA 应用部署需要配 rewrites？**

**答：** SPA 只有一个入口 HTML 文件（`index.html`），所有路由由前端 JavaScript 处理。但服务器（或 CDN）不知道哪些路径是前端路由——它只认物理文件。用户直接访问 `/list`，服务器找不到 `list.html`，返回 404。rewrites 规则把**所有请求都转发到 `index.html`**，让 React Router 在前端接管路由。类比：就像 Vite dev server 的 fallback 机制。

**Q：Vercel 的环境变量和 `.env` 文件有什么区别？**

**答：** `.env` 文件在本地开发时使用，一般不会提交到 Git。Vercel 的环境变量在 Dashboard 配置，存储在 Vercel 的服务器上，安全隔离，且可以在不同环境（Production / Preview / Development）设置不同值。对于 Vite 项目，`VITE_` 前缀的变量会在 build 时内联到代码——所以改环境变量后需要重新触发部署。

## 📝 小结

今天我们完成了金橘记账前端的 Vercel 部署，顺带搞懂了三个东西：

1. **Vercel 原理**：git push → 检测框架 → install → build → CDN 分发。本质是把 `dist/` 文件夹丢到全球 CDN。
2. **Monorepo 配置**：Root Directory + Build Command + Output Directory + 环境变量 + vercel.json rewrites。五个配置，缺一不可。
3. **SPA 路由 404**：所有路径都返回 `index.html`，前端接管路由。vercel.json 一句配置搞定。

下一期我们聊后端部署——怎么把 FastAPI 项目部署到 Render。前后端都上线后，金橘记账才算真正"活"在互联网上。

**部署前端的终极体验不是 `npm run dev`，而是 `git push` 之后自动刷新浏览器——那一刻你觉得自己的代码真的在互联网上活着。**

---

## 🎨 本文配图提示词

**封面图（16:9）：**

```
A side-by-side illustration. Left side: a frustrated developer dragging files with FTP software, configuring Nginx, terminal windows everywhere, looking exhausted. Right side: the same developer relaxed, just typing "git push" and watching a Vercel dashboard with green "Deployed" checkmark. A high-speed train on the right vs an old steam train on the left. Warm color palette (#F59E0B amber primary, #FFF8F0 warm white background, #1F2937 dark text), flat illustration style, clean tech article composition. --ar 16:9 --style digital --v 6
```

**文中配图（4:3）：**

```
An illustration showing the Vercel deployment pipeline as a factory assembly line. Step 1: GitHub icon with "git push" label. Step 2: A gear icon labeled "npm install + build". Step 3: A folder icon labeled "dist/". Step 4: Multiple server nodes around a globe labeled "CDN". Step 5: A phone and laptop showing the live website. Arrows connecting each step. Warm tones, flat illustration, clean tech style. --ar 4:3 --style digital --v 6
```

**中文版（DALL-E 可用）：**

```
一张左右对比的插画。左边是一个疲惫的开发者用FTP软件拖文件、配置Nginx、终端窗口满天飞；右边是同一个开发者轻松地敲下git push，Vercel控制台显示绿色的"已部署"。左边是一辆老式绿皮火车，右边是一辆高铁。暖色调扁平风格，适合技术专栏文章配图。
```

—— 阿珊，前端开发者 & 全栈学习者
