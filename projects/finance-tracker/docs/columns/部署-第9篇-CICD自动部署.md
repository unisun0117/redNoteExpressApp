# 部署从0到1 第9篇：CI/CD — 代码 push 自动部署

> 手动部署 = 改代码 → build → 上传 → 重启。CI/CD = git push，喝杯咖啡回来已上线。

---

## 📌 阿珊故事：我的第一个“CI/CD”就是个 bash 脚本

先给你讲个丢人的事。

我刚学会写后端那会儿，部署流程是这样的：

```bash
# 1. 本地改完代码，手动打包
npm run build

# 2. 用 FileZilla 把 dist/ 文件夹拖到服务器上
# 3. SSH 登录服务器
ssh root@my-server

# 4. 手动重启 Nginx
sudo systemctl restart nginx

# 5. 祈祷一切正常
```

问题来了——我经常忘掉某一步。有时候改了代码 build 了但忘了上传，有时候上传了忘了重启。最离谱的一次，我把一个老版本的 dist 文件夹覆盖了线上版本，然后 debug 了两小时才发现——线上跑的根本不是最新代码。

后来我学聪明了，写了个 bash 脚本：

```bash
#!/bin/bash
# deploy.sh —— 阿珊的自动化脚本
echo "开始部署..."
ssh root@my-server << 'EOF'
  cd /var/www/my-app
  git pull origin main
  npm install --production
  npm run build
  systemctl restart nginx
  echo "部署完成！"
EOF
```

从此我就只用跑 `bash deploy.sh` 一条命令了。这就是我的第一个“CI/CD”。

直到后来我用上了 Vercel 和 Render，才发现——卧槽，原来他们做的事本质上就跟我的 deploy.sh 一样：**git push 之后自动执行一串命令**。只不过他们把这个脚本写成了标准化的配置，并且把执行环境、日志、回滚都给你配好了。

所以今天这篇，我们就来揭开 CI/CD 的神秘面纱。

### 前端类比：CI = ESLint，CD = npm run build

如果你是个前端，你一定用过这两个东西：

- **ESLint**：每次 commit 之前自动检查代码有没有未使用的变量、格式对不对。写代码的时候就会发现错误，而不是等上线后用户来报 bug。
- **npm run build / vite build**：写好代码，一键把源码编译成能直接丢给浏览器的 HTML + JS + CSS。

现在把这两个概念搬到部署的世界：

| 前端开发 | 部署世界 |
|---------|---------|
| ESLint 自动检查代码 | **CI（持续集成）**——每次 push 自动跑测试、检查类型、lint |
| npm run build 打包产物 | **CD（持续部署）**——自动把代码部署到服务器上 |
| git commit 提交代码 | **git push** 触发整套流程 |

一句话概括：

> **CI 管“对不对”，CD 管“上没上”。**

---

## 🔍 核心原理：一台帮你干活的小电脑

### GitHub Actions —— 白送的自动化小机器

Vercel 和 Render 背后其实是同一套逻辑——你 push 代码到 GitHub，GitHub 就通知他们：“嘿，代码更新了。”然后他们各自在自己的服务器上跑一个脚本，做完该做的事。

但 GitHub 自己就提供这个能力，叫 **GitHub Actions**。你不用 Vercel 也能用。

它的本质就是：**GitHub 免费送你一台临时的小虚拟电脑，让你在上面跑脚本。** 这台电脑叫 runner，跑完就销毁，下次 push 再给你一台新的。

配置文件长这样，放在你项目里的 `.github/workflows/deploy.yml`：

```yaml
name: 自动部署
on:
  push:
    branches: [main]  # main 分支有 push 就触发

jobs:
  deploy:
    runs-on: ubuntu-latest  # GitHub 给你一台 Ubuntu 虚拟机
    steps:
      - name: 拉取代码
        uses: actions/checkout@v4

      - name: 安装 Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: 安装依赖
        run: npm install

      - name: 跑测试
        run: npm test

      - name: 构建
        run: npm run build

      - name: 部署到服务器
        run: |
          scp -r dist/* user@server:/var/www/html/
```

看懂了吗？每一步就是一个 `run`，就是一句你平时手敲的命令。GitHub Actions 只不过帮你把它们串起来，在你 push 的时候自动跑一遍。

### 来看 Vercel 的后台在干嘛

Vercel 的配置比写 yaml 还简单——你什么都不用写，连上 GitHub 仓库它就自动做了这些事：

```
git push → Vercel 检测到 main 分支有更新
         → 检测项目类型（package.json → 前端项目）
         → 自动执行 npm install
         → 自动执行 npx vite build（或 npm run build）
         → 把构建产物（dist/）上传到全球 CDN
         → 发一条通知：部署完成 ✅
```

你用金橘记账这个项目去 Vercel 的后台 Dashboard，点进部署记录，你能看到每一步的日志：

```
[14:23:01] Cloning repository: yuanqidong/arashi-finance-tracker
[14:23:08] Detected framework: Vite
[14:23:10] Installing dependencies...
[14:23:45] ✓ 43 packages installed
[14:23:46] Running build: npx vite build
[14:24:02] ✓ Build complete: dist/
[14:24:05] Deploying to CDN...
[14:24:08] ✓ Deployment live at arashi-finance-tracker.vercel.app
```

这不就是我的 deploy.sh 的豪华升级版吗？而且它还多做了很多事：

- **每次部署生成一个唯一的 URL**，你可以预览这个版本再决定要不要上线
- **构建失败自动回滚**，线上继续跑上一个成功版本
- **日志永久保留**，出问题可以回看

### 再看 Render 的后台在干嘛

Render 做后端部署也是同一套逻辑：

```
git push → Render 检测到 main 分支有更新
         → 进入 backend/ 目录
         → 执行 pip install -r requirements.txt
         → 用 uvicorn 启动服务
         → 健康检查通过 → 切流量到新实例
         → 旧实例销毁
```

Render 比 Vercel 多了一个“健康检查”步骤——它会先开一个新实例，确认它正常启动（比如访问 `/api/health` 返回 200），然后才把流量切过去。这保证了**部署过程中用户不会看到白屏或 500**。

> **核心认知：Vercel 和 Render 不是魔法，它们就是把你手动做的事情自动化了。你完全可以自己用 GitHub Actions 写到同样效果，只是它们帮你配好了默认值。**

---

## 🛠 动手试试

### 1. 看一次 Vercel 的完整构建日志

打开你的 Vercel Dashboard，找到金橘记账项目，点进最近一次部署记录。

你会看到一个类似终端的界面，里面一行一行地打印着构建过程。重点看这几行：

```
[XX:XX:XX] Detected framework: Vite
[XX:XX:XX] Running build: npx vite build
```

这两行告诉你：Vercel 自动识别出你用的是 Vite 框架，然后用 Vite 的命令去构建。如果你用的是 Create React App，它会自动识别成 `react-scripts build`。

**关键发现：** 你看到那行 `cd frontend/` 了吗？这就是为什么 Vercel 项目设置里要填 **Root Directory = `frontend`** ——告诉它在哪个子目录里找 `package.json`。Monorepo 项目（一个仓库里前后端都放）如果不配这个，Vercel 会在根目录找 package.json 找不到，构建就挂了。

### 2. 看一次 Render 的部署日志

切到 Render Dashboard，点进金橘记账后端的部署记录。你会看到不同的阶段：

```
Build phase:
  → pip install -r requirements.txt

Start phase:
  → uvicorn app.main:app --host 0.0.0.0 --port $PORT
```

注意 Render 的 `$PORT` 不是默认的 8000——Render 会自己分配一个端口号，放在环境变量里。你代码里必须用 `$PORT` 而不是写死 `8000`。这就是一个典型的环境差异坑，等下我们会细讲。

### 3. 手写一个最简单的 GitHub Action

在你的项目里创建 `.github/workflows/lint.yml`：

```yaml
name: 代码检查
on: [push]  # 每次 push 都跑
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 20
      - run: npm install
      - run: npm run lint
```

push 这段代码上去，然后去 GitHub 仓库的 Actions 页面，你就能看到这台“临时小电脑”在帮你跑 lint 了。绿色的勾 = 通过，红色叉 = 代码有问题。

---

## ⚠️ 坑点：构建环境不是你本地

这是 CI/CD 最容易踩的坑——**GitHub Actions 的 Ubuntu 虚拟机、Vercel 的构建环境，跟你的 MacBook/Windows 不是一回事。**

### 坑1：本地能跑，构建环境跑不了

你的本地 `node_modules/` 里有某个包，但 `package.json` 里没写进去（或者写成了 devDependencies）。本地能用是因为你之前 `npm install -g` 装过，或者 node_modules 里恰好有。但 CI 环境是从零开始装的——缺一个依赖就崩。

**解决方案：** 永远用 `npm install --include=dev` 或确保所有依赖都在 `package.json` 里。

### 坑2：环境变量没配

这也太常见了。本地 `.env` 里有 `VITE_API_URL=http://localhost:8000/api`，构建的时候能读到。但 Vercel 的构建环境里没有这个文件（`.env` 不上传 git，你肯定 `.gitignore` 了对吧？）。

Vercel 构建出来的前端，`VITE_API_URL` 可能是 `undefined`。然后所有 API 请求都发到 `undefined/api/login`，你盯着浏览器 Network 面板看半天才发现。

**解决方案：** 在 Vercel Dashboard → Settings → Environment Variables 里，把你在 `.env` 里定义的变量重填一遍。Render 同理。

金橘记账的环境变量清单：

| 平台 | 变量名 | 值 |
|------|-------|-----|
| Vercel | `VITE_API_URL` | `https://arashi-finance-api.onrender.com` |
| Render | `DATABASE_URL` | Neon 提供的连接地址 |
| Render | `JWT_SECRET` | 你自己生成的一串随机字符 |
| Render | `ALLOWED_ORIGINS` | `https://arashi-finance-tracker.vercel.app` |

### 坑3：Monorepo 的根目录问题

金橘记账这个项目，根目录是空的（或者是 monorepo 根配置），真正的前端代码在 `frontend/` 子目录里。Vercel 默认在仓库根目录找 `package.json`——找到了吗？没找到。

**Vercel 的配置方法：**
- Settings → General → Root Directory → 填 `frontend`
- Settings → General → Build Command → 填 `npx vite build`

**Render 的配置方法：**
- Settings → Root Directory → 填 `backend`
- Build Command → `pip install -r requirements.txt`

两个平台都有这个设置项，但名字可能叫法不一样（Root Directory / Project Directory / Base Directory），意思都是同一个事。

---

## 🎯 面试官会怎么问

### Q1：CI/CD 是什么？

**标准回答：**

- **CI（持续集成）**：开发人员频繁地把代码合并到主分支，每次合并自动跑测试、lint、类型检查。目的：尽早发现问题，避免“在我电脑上能跑”的整合灾难。
- **CD（持续部署/交付）**：CI 通过之后，自动把代码部署到生产环境（或预发布环境）。目的：减少人工操作，让部署变得可重复、可回滚。

面试官想听的关键词：**自动化、频繁集成、尽早发现问题、减少人工操作。**

### Q2：git push 之后到网站更新，中间发生了什么？

**以金橘记账为例，结合 Vercel + Render 回答：**

1. 你在本地 `git push` 到 GitHub 的 main 分支
2. Vercel 通过 webhook 检测到 push 事件
3. Vercel 拉取最新代码，检测框架（Vite），进入 `frontend/` 目录
4. 执行 `npm install` → `npx vite build`，生成 `dist/`
5. 把 `dist/` 部署到 CDN，全球节点分发静态文件
6. 同时，Render 也检测到 push 事件
7. Render 进入 `backend/` 目录，配置 Python 环境
8. 执行 `pip install` → 启动新实例 → 健康检查
9. 健康检查通过 → 流量切到新实例 → 旧实例销毁
10. 用户访问网站 → 看到最新版本

**总时间：通常 1-3 分钟。** 你 push 完去倒杯水，回来就上线了。

### Q3：你用过 GitHub Actions 吗？

即使你没正经用过，也可以这样说（基于你已有的理解）：

“我理解 GitHub Actions 的工作原理——它就是在 GitHub 云端给你一台临时虚拟机，按 YAML 配置文件跑一系列步骤。我项目中 Vercel 和 Render 做的事情，本质上就是 GitHub Actions 的封装。如果不用这两个平台，我可以写一个 Action 用 `scp` 把 build 产物传到自己的服务器上，或者用 `ssh` 执行重启命令。”

这回答展示了你理解本质，而不是背答案。

---

## 📝 小结

今天我们从一个 bash 脚本的故事开始，讲清楚了 CI/CD 的本质：

1. **CI/CD 不是什么新概念**——它就是把你要手动跑的命令串起来自动执行
2. **Vercel 和 Render 是 CI/CD 的现成方案**——它们帮你配好了前端/后端项目最常见的那套脚本
3. **GitHub Actions 是更灵活的方案**——你想怎么跑就怎么写 yaml，不局限于某个平台
4. **三个常见坑：** 环境变量没配、monorepo 子目录、构建环境和本地不一致

> **一句话：CI/CD = git push 之后，一台虚拟机帮你跑 `npm install && npm run build`，然后自动传到服务器上。你用 Vercel + Render 只是有人帮你写好了这个脚本。**

下一期，我们来搞定**监控和日志**——代码上线后怎么知道它在正常运行？用户是不是遇到了 500？敬请期待。

---

## 🎨 本期配图

> 一组插画风格示意图：
>
> **图一：** 一个程序员推完代码，端着咖啡离开工位，画面右边一台小机器人正在自动跑流水线——安装依赖 → 构建 → 部署，最后弹出一个绿色的对勾。
> **图二：** CI 和 CD 的对比图，左边 CI 画着放大镜检查代码（lint/test），右边 CD 画着火箭把代码发射到服务器上，中间用 git push 按钮连接。
> **图三：** 金橘记账的 CI/CD 全流程——GitHub 仓库 push 之后分叉成两条线，一条去 Vercel（前端），一条去 Render（后端），最后汇聚到用户手机上显示一个记账页面。

---

*专栏：部署从0到1*
*第9篇：CI/CD —— 代码 push 自动部署*
*作者：阿珊，前端开发者 & 全栈学习者*
*如果这篇文章对你有帮助，欢迎转发给同样在学部署的朋友。*
