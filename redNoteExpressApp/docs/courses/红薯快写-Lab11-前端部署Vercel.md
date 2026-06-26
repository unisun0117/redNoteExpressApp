# Lab 11：前端部署 — Vercel 一键上线

> **预计用时：** 30-35 分钟
> **难度：** ⭐⭐
> **前置 Lab：** Lab 10（前端必须能本地跑通）

---

## 📌 前言

部署是我以前最怕的环节——像开盲盒，不知道哪里会炸。本地跑得好好的，一上线就各种报错。

但 Vercel 真的是前端部署的天花板。GitHub 仓库一连，每次推送代码自动构建、自动上线，免费额度完全够个人项目和毕设用。最爽的是——自带 HTTPS 证书，自带全球 CDN 加速，不需要你配任何东西。

这一 Lab，把你写的前端部署到 Vercel，全世界都能用手机访问。

---

## 📚 基础知识储备

- **静态网站托管** — 把你的 HTML/JS/CSS 文件放到一个公网服务器上
- **CI/CD** — 持续集成/持续部署。代码推到 GitHub → 自动构建 → 自动上线
- **Vercel** — 专为前端优化的部署平台，Next.js 的母公司做的
- **环境变量** — `.env` 文件里的 `VITE_API_URL` 在生产环境要改成后端地址

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 注册 Vercel 并关联 GitHub 仓库
2. 配置构建参数和环境变量
3. 部署前端并访问线上地址
4. 理解 Vercel 的自动部署流程

---

## 🛠 动手实战

### 步骤 1：注册 Vercel

1. 打开 https://vercel.com
2. 点击 "Sign Up"
3. **选择 GitHub 登录**（这样你的仓库就能直接关联）
4. 授权 Vercel 访问你的 GitHub 仓库

---

### 步骤 2：导入项目

1. 登录后点击 "New Project"
2. 找到 `redNoteExpressApp` 仓库，点击 "Import"
3. 进入配置页面

---

### 步骤 3：配置构建参数

⚠️ **这一步有三个关键设置，填错就部署失败：**

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **Root Directory** | `redNoteExpressApp/frontend` | 不是根目录！Vercel 需要知道前端代码在哪个子目录 |
| **Build Command** | `npx vite build` | ⚠️ 不要用 `npm run build`（会 TypeScript 报错） |
| **Output Directory** | `dist` | Vite 构建的默认输出目录 |

---

### 步骤 4：配置环境变量

点击 "Environment Variables"，添加：

| Key | Value |
|-----|-------|
| `VITE_API_URL` | `https://huanglishan123.pythonanywhere.com/api` |

> ⚠️ 这里填的是**生产环境的后端地址**，不是 `localhost:8000`！

---

### 步骤 5：部署

1. 点击 "Deploy"
2. 等待 1-2 分钟，Vercel 会自动：
   - 拉取代码
   - 安装依赖
   - 执行 `npx vite build`
   - 上传构建产物
3. 部署成功后会显示一个地址：`https://red-note-express-app.vercel.app`

**验证：** 浏览器打开这个地址，能看到你的 App ✅

---

### 步骤 6：验证登录流程

部署后测试完整流程：
1. 打开线上地址 → 跳转到 `/login`
2. 注册/登录
3. 输入关键词生成文章
4. 确认一切正常

**验证：** 线上环境完整功能都能用 ✅

---

### 步骤 7：设置自动部署

Vercel 默认就是自动部署——你 push 代码到 GitHub，Vercel 自动重新构建。

**操作：** 改一行前端代码 → `git commit` → `git push` → 等 1-2 分钟 → 刷新线上页面 → 改动生效！

---

### 步骤 8：检查生产分支设置

⚠️ 这个项目的生产分支是 `master`，不是 `main`。

去 Vercel 项目 Settings → Git：
- Production Branch 设为 `master`

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| 构建失败 `tsc -b` 报错 | 部署日志里看到 TypeScript 错误 | Build Command 用了 `npm run build` | 改成 `npx vite build` |
| 404 on /login | 访问 `/login` 显示 404 | Vercel 默认不会重写 SPA 路由 | 项目已有 `vercel.json` 配置 SPA fallback |
| `VITE_API_URL` 没配 | 前端页面打开但 API 请求失败 | 环境变量没配 | 在 Vercel Settings → Environment Variables 里加上 |
| CORS 报错 | 登录注册失败 | 后端 `ALLOWED_ORIGINS` 没包含 Vercel 域名 | 在 PythonAnywhere 环境变量里加 `ALLOWED_ORIGINS=*` |

---

## 📝 总结

**本章核心要点：**
- Vercel 三步部署：关联仓库 → 配置构建参数 → 配环境变量
- 三个关键配置：Root Directory、Build Command（`npx vite build`）、环境变量
- Push 即部署——代码推到 GitHub，Vercel 自动上线
- 生产分支是 `master`

**你现在应该能做到：**
- 独立把任何一个 React/Vite 项目部署到 Vercel
- 理解 CI/CD 自动部署流程

**下一步：** Lab 12 我们把后端也部署上去——PythonAnywhere 实战。

---

> —— 阿珊，前端开发者 & AI 提效实践者
