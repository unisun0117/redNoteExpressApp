# Lab 01：项目介绍与环境搭建 — Node.js + Python 双环境配置

> **预计用时：** 30-40 分钟
> **难度：** ⭐
> **前置 Lab：** 无

---

## 📌 前言

这个项目的起因很简单——我接了个外包，甲方要一个小红书文案生成器。需求大概是这样：用户上传一张美食图片，输入几个关键词，点击生成，AI 自动吐出一篇带 emoji、带小标题、带店铺信息的小红书文案。

我想了想：与其手写 10 篇文案，不如让 AI 来写。于是我花了两周，用 React + FastAPI + DeepSeek 把这个 App 做了出来。结果它不仅交付了甲方，还成了我的课程素材、专栏文章、外包 Demo——一个项目，赚了四次钱。

**今天，你从零开始复刻它。** 学完这门课，你不仅学会全栈开发，还能把它改成你自己的毕设、作品集、或者接外包的 Demo。

别怕，我从环境配置开始，一步一步带你走。

---

## 📚 基础知识储备

学这个 Lab 之前，你只需要知道：

- **命令行基础** — 会 `cd`（切换目录）、`mkdir`（新建文件夹）、`dir`（查看文件列表）就够了
- **什么是前端** — 你在浏览器里看到的页面，就是前端。我们用的是 React（一个 Facebook 开源的前端框架）
- **什么是后端** — 你点"生成"按钮后，数据发给服务器处理，那个服务器就是后端。我们用的是 FastAPI（一个 Python 写的后端框架）
- **什么是 AI API** — 我们不会自己训练 AI，而是调用 DeepSeek 提供的接口（API），把图片和关键词发给它，它返回生成好的文章

> 💡 如果上面有不太懂的概念，别慌。跟着做就行，理解会在动手过程中慢慢建立。

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 在 Windows 电脑上安装好 Node.js 和 Python 3.10
2. 把项目代码克隆到本地，搞清楚每个文件夹是干嘛的
3. 成功安装前端和后端的所有依赖
4. 对整个项目的技术架构有一个全局认知

---

## 🛠 动手实战

### 步骤 1：安装 Node.js

**做什么：** 安装 Node.js 运行环境（前端需要它）

**操作：**
1. 打开浏览器，访问 https://nodejs.org
2. 下载 **LTS 版本**（长期支持版，更稳定），推荐 v20.x 或以上
3. 双击安装包，一路点"下一步"，全部默认选项即可
4. 安装完成后，打开终端验证：

```bash
node --version
# 应该输出类似：v20.11.0

npm --version
# 应该输出类似：10.2.4
```

**验证：** 两个命令都输出版本号，说明 Node.js 安装成功 ✅

---

### 步骤 2：安装 Python 3.10

**做什么：** 安装 Python 运行环境（后端需要它）

**操作：**
1. 打开浏览器，访问 https://www.python.org/downloads/
2. 下载 **Python 3.10.x**（注意：推荐 3.10，3.11+ 也行，但 3.10 最稳）
3. ⚠️ **安装时一定要勾选 "Add Python to PATH"**（把 Python 加到环境变量）
4. 安装完成后，打开终端验证：

```bash
python --version
# 应该输出类似：Python 3.10.11
```

**验证：** 输出版本号，说明 Python 安装成功 ✅

---

### 步骤 3：克隆项目代码

**做什么：** 把 GitHub 上的项目代码下载到本地

**操作：**
```bash
# 进入你的工作目录（自己选一个地方）
cd E:\

# 创建工作区文件夹
mkdir AI_Workspace
cd AI_Workspace

# 克隆项目
git clone https://github.com/unisun0117/redNoteExpressApp.git
```

如果你没装 Git，去 https://git-scm.com/download/win 下载安装。

**验证：** 执行 `dir redNoteExpressApp` 能看到 `backend` 和 `frontend` 两个文件夹 ✅

---

### 步骤 4：理解项目目录结构

**做什么：** 搞清楚每个文件夹是干嘛的

```
redNoteExpressApp/
├── backend/                  ← 后端代码（Python + FastAPI）
│   ├── app/
│   │   ├── main.py          ← 后端入口，启动服务
│   │   ├── config.py        ← 所有配置（数据库地址、API Key等）
│   │   ├── database.py      ← 数据库连接
│   │   ├── models/          ← 数据库表定义
│   │   ├── routers/         ← API 接口（注册、登录、生成等）
│   │   ├── services/        ← 核心业务逻辑（AI 生成）
│   │   └── middleware/      ← JWT 认证中间件
│   ├── requirements.txt     ← 后端依赖清单
│   └── wsgi.py              ← 部署配置文件
├── frontend/                 ← 前端代码（React + TypeScript）
│   ├── src/
│   │   ├── App.tsx          ← 前端入口 + 路由
│   │   ├── pages/           ← 页面组件
│   │   ├── components/      ← UI 组件
│   │   ├── hooks/           ← 自定义 Hook
│   │   └── services/        ← API 调用封装
│   ├── package.json         ← 前端依赖清单
│   └── vite.config.ts       ← Vite 构建配置
└── docs/                     ← 文档
```

> 🧠 **记不住没关系**，后面每个 Lab 会反复用到这些文件，慢慢就熟了。

---

### 步骤 5：安装前端依赖

**做什么：** 下载前端需要的 npm 包（React、Vite、antd-mobile 等）

**操作：**
```bash
cd E:\AI_Workspace\redNoteExpressApp\frontend
npm install
```

这个过程需要几分钟，取决于网络速度。如果太慢，可以用国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

**验证：** 执行完后，`frontend` 目录下多了一个 `node_modules` 文件夹 ✅

---

### 步骤 6：安装后端依赖

**做什么：** 创建 Python 虚拟环境并安装后端依赖

**操作：**
```bash
# 进入后端目录
cd E:\AI_Workspace\redNoteExpressApp\backend

# 创建虚拟环境（隔离的 Python 环境，不影响系统 Python）
python -m venv venv

# 激活虚拟环境
venv\Scripts\activate

# 你的终端前面应该出现 (venv) 标识
# 现在安装依赖
pip install -r requirements.txt
```

> ⚠️ **关键：** 以后每次启动后端，都要先激活 venv：`venv\Scripts\activate`。如果你的终端前面有 `(venv)`，说明已激活。

**验证：** 执行 `pip list` 能看到 fastapi、uvicorn、sqlalchemy 等包 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| Python 版本太高/太低 | `pip install` 报各种奇怪错误 | 有些包不支持 Python 3.12+ | 装 Python 3.10，最稳 |
| pip 安装超时 | `pip install` 一直卡住或报 timeout | 默认从国外 PyPI 下载，国内慢 | 用国内镜像：`pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple` |
| npm install 报错 | `node-gyp` 相关错误 | 缺少 C++ 编译工具 | 不需要解决——安装时加 `--ignore-scripts` 跳过：`npm install --ignore-scripts` |
| venv 激活失败 | PowerShell 报"无法加载文件" | PowerShell 执行策略限制 | 用 CMD 终端，或者执行：`Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned` |
| Git 没装 | `git clone` 提示"不是内部命令" | 没装 Git | 去 https://git-scm.com/download/win 下载安装 |

---

## 📝 总结

**本章核心要点：**
- Node.js 管前端，Python 管后端，Git 管代码版本
- `node_modules` 是前端依赖，`venv` 是后端虚拟环境
- 项目目录记住三个关键路径：`backend/app/main.py`（后端入口）、`frontend/src/App.tsx`（前端入口）、`requirements.txt`（后端依赖清单）

**你现在应该能做到：**
- 在终端里输入 `node --version` 看到版本号
- 在终端里输入 `python --version` 看到版本号
- `redNoteExpressApp` 目录下有完整的项目代码

**下一步：** Lab 02 我们将启动后端服务，在浏览器里看到第一个 API！

---

> —— 阿珊，前端开发者 & AI 提效实践者
