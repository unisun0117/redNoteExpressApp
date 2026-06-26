# Lab 02：FastAPI 后端启动 — 从零看到第一个 API

> **预计用时：** 30-35 分钟
> **难度：** ⭐
> **前置 Lab：** Lab 01（环境必须已配好）

---

## 📌 前言

我第一次打开 FastAPI 的 `/docs` 页面时，真的被震撼到了——所有 API 接口自动生成一份网页文档，可以直接在页面上填参数、点执行、看返回结果。甲方就是看了这个页面，当场确认了需求。

以前写后端，接口文档得单独维护。前端问你"这个接口参数是什么"，你要么翻代码，要么翻 Wiki。FastAPI 把这个痛点直接干掉了——**代码即文档**，写完接口，文档自动生成。

这一 Lab，我们不写一行新代码，只把项目现有的后端跑起来，先感受一下 FastAPI 到底有多爽。

---

## 📚 基础知识储备

学这个 Lab 之前，你需要了解：

- **什么是 API** — Application Programming Interface，翻译成人话就是"程序之间通信的约定"。前端说"给我生成一篇文章"，后端说"好的，传图片和关键词来，我给你文章"——这个约定就是 API
- **什么是 REST API** — 一种 API 设计风格，用 HTTP 方法（GET/POST/PUT/DELETE）表示操作类型
- **什么是 Swagger** — 自动生成 API 文档的工具，FastAPI 内置了。你访问 `/docs` 就能看到
- **什么是 uvicorn** — 一个 ASGI 服务器，用来跑 FastAPI 应用。你可以理解为"后端的启动器"

> 💡 概念不用背，跑起来就懂了。

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 用一行命令启动 FastAPI 后端服务
2. 在浏览器里看到自动生成的 API 文档（Swagger）
3. 直接在网页上测试 API 接口，看到返回的 JSON 数据
4. 理解 `main.py` 和后端目录结构的关系

---

## 🛠 动手实战

### 步骤 1：激活虚拟环境

**做什么：** 每次启动后端前，必须先激活 venv（让终端用项目自己的 Python 环境）

**操作：**
```bash
cd E:\AI_Workspace\redNoteExpressApp\backend

# 激活虚拟环境
venv\Scripts\activate

# 确认激活成功：终端前面应该出现 (venv) 标识
```

**验证：** 终端提示符前面出现 `(venv)` ✅

---

### 步骤 2：启动后端服务

**做什么：** 用 uvicorn 启动 FastAPI 应用

> ⚠️ **必须用 venv 里的 uvicorn，不能直接用系统全局的！**

**操作：**
```bash
# 确保在 backend 目录下，且 venv 已激活
.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000
```

你会看到类似这样的输出：
```
INFO:     Will watch for changes in these directories: ['E:\\AI_Workspace\\redNoteExpressApp\\backend']
INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
INFO:     Started reloader process
INFO:     Started server process
INFO:     Waiting for application startup.
```

**参数解释：**
- `app.main:app` → "去 `app/main.py` 里找那个叫 `app` 的 FastAPI 实例"
- `--reload` → 代码改了自动重启（开发必备）
- `--port 8000` → 监听 8000 端口

**验证：** 看到 `Uvicorn running on http://127.0.0.1:8000` ✅

---

### 步骤 3：访问 API 文档页面

**做什么：** 打开浏览器看 Swagger 自动生成的 API 文档

**操作：**
1. 打开浏览器，访问：http://localhost:8000/docs
2. 你会看到一个漂亮的 API 文档页面

**页面上你能看到：**
- **auth** 分组：注册、登录、刷新 token、查看个人信息
- **generate** 分组：生成文章
- **styles** 分组：获取风格模板和赛道列表
- **viral** 分组：分析爆款文案
- **billing** 分组：充值、订阅
- **batch** 分组：批量生成

每个接口点开能看到参数说明、请求示例、返回格式。

**验证：** 浏览器打开了 Swagger 页面，能看到上面的接口列表 ✅

---

### 步骤 4：测试第一个 API — 健康检查

**做什么：** 在 Swagger 页面上直接测试 `/api/health` 接口

**操作：**
1. 在 Swagger 页面上找到 `GET /api/health`（应该在最上面）
2. 点它展开
3. 点击 **"Try it out"** 按钮
4. 点击 **"Execute"** 按钮
5. 看返回结果：

```json
{
  "status": "ok"
}
```

**这个接口干嘛的？** 它就是告诉我们"后端还活着"。后面部署上线后，我们会用它来验证后端是否正常运行。

**验证：** 返回了 `{"status": "ok"}` ✅

---

### 步骤 5：测试风格列表接口

**做什么：** 测试 `/api/styles` 接口（这个接口不需要登录）

**操作：**
1. 在 Swagger 页面上找到 `GET /api/styles`
2. 点它 → "Try it out" → "Execute"
3. 看返回：

```json
{
  "templates": [
    {"id": "retro", "name": "复古风", "description": "Vintage, nostalgic tone..."},
    {"id": "minimalist", "name": "简约风", ...},
    ...
  ],
  "tracks": [
    {"id": "food", "name": "美食"},
    {"id": "sports", "name": "运动"},
    ...
  ]
}
```

**验证：** 返回了模板列表和赛道列表 ✅

---

### 步骤 6：理解后端代码是怎么组织起来的

**做什么：** 打开 `main.py`，看懂它怎么把各个模块拼在一起

**打开文件：** `backend/app/main.py`

```python
from app.database import engine, Base
from app.config import settings

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import auth, generate, styles, viral, billing, batch

app = FastAPI(title="RedNote Express API", version="0.1.0")

# 自动建表
Base.metadata.create_all(bind=engine)

# 配置 CORS（允许前端跨域访问）
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册各个路由模块
app.include_router(auth.router, prefix="/api/auth", tags=["auth"])
app.include_router(generate.router, prefix="/api", tags=["generate"])
app.include_router(styles.router, prefix="/api", tags=["styles"])
app.include_router(viral.router, prefix="/api", tags=["viral"])
app.include_router(billing.router, prefix="/api", tags=["billing"])
app.include_router(batch.router, prefix="/api", tags=["batch"])

# 健康检查接口
@app.get("/api/health")
def health():
    return {"status": "ok"}
```

**理解要点：**
- 第 1-4 行：导入依赖
- 第 6 行：`app = FastAPI(...)` 创建应用实例——整个后端就围绕这个 `app` 运转
- 第 12-18 行：CORS 中间件，允许前端跨域请求
- 第 21-26 行：把各个路由模块注册到 app 上，每个模块有独立的 URL 前缀
- 第 29-31 行：手写了一个最简单的接口

**验证：** 能看懂至少 50% 就行，后面每个 Lab 会反复接触 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| 用了系统 uvicorn | 启动报错 `ImportError` 或 `ModuleNotFoundError` | 系统 Python 没装项目依赖 | 必须用 `.\venv\Scripts\uvicorn.exe` |
| 端口被占用 | `Address already in use` | 8000 端口已经在用了 | 换端口：`--port 8001`，或者先关掉占用的程序 |
| venv 没激活 | `uvicorn` 找不到 | 终端没在 venv 里 | 先执行 `venv\Scripts\activate` |
| 改了代码没生效 | 改了代码但接口行为没变 | 没保存文件，或者没启用 `--reload` | 确保加了 `--reload` 参数 |

---

## 📝 总结

**本章核心要点：**
- 启动命令就一行：`.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000`
- Swagger 文档在 `/docs`，可以直接测试所有接口
- `main.py` 是整个后端的组装车间，所有模块在这里注册
- CORS 中间件让前端能跨域访问后端

**你现在应该能做到：**
- 独立启动后端服务
- 在 Swagger 页面上测试接口
- 说出后端代码的大致组织结构

**下一步：** Lab 03 我们给项目装上数据库，创建第一张表——用户表。

---

> —— 阿珊，前端开发者 & AI 提效实践者
