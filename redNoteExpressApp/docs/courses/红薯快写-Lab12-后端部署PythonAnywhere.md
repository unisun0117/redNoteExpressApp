# Lab 12：后端部署 — PythonAnywhere 实战

> **预计用时：** 40-45 分钟
> **难度：** ⭐⭐⭐
> **前置 Lab：** Lab 11（前端必须先部署好）

---

## 📌 前言

后端部署比前端麻烦多了——这是事实。

前端一个 `npx vite build`，Vercel 帮你全搞定。后端不一样：你得配服务器、装 Python 环境、处理 WSGI/ASGI 的兼容问题、数据库文件放哪、环境变量怎么设、改完代码怎么生效...

这一 Lab 我把它拆成 6 个步骤，每一步都经过验证。按顺序走，一次就能上线。如果中间卡住了——别急，去最后的"常见坑点"找答案。

---

## 📚 基础知识储备

- **WSGI vs ASGI** — Python Web 服务器的两种协议。FastAPI 用的是 ASGI，但 PythonAnywhere 免费版只支持 WSGI，所以需要用一个叫 `asgiref` 的桥接器做转换
- **PythonAnywhere** — 一个 Python 云托管平台，有免费版，够个人项目用
- **环境变量在服务端的配置** — 不像本地 `.env` 文件，服务端环境变量要在平台的后台管理页面设置

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 注册 PythonAnywhere 并上传后端代码
2. 配置 WSGI 桥接文件
3. 设置环境变量和 SQLite 数据库
4. 让后端在公网正常运行

---

## 🛠 动手实战

### 步骤 1：注册 PythonAnywhere

1. 打开 https://www.pythonanywhere.com
2. 注册免费账号
3. 登录后进入 Dashboard

---

### 步骤 2：上传代码

**方法一：从 GitHub 克隆（推荐）**
1. 打开 PythonAnywhere 的 Bash 终端
2. 执行：
```bash
git clone https://github.com/unisun0117/redNoteExpressApp.git
```

**方法二：手动上传**
1. 在 Files 页面创建目录结构
2. 逐个上传文件（不推荐，太慢）

---

### 步骤 3：安装依赖

在 PythonAnywhere Bash 里：
```bash
cd ~/redNoteExpressApp/backend
pip install -r requirements.txt --user
```

> ⚠️ PythonAnywhere 免费版不能用 `venv`，直接用 `--user` 安装到用户目录。

---

### 步骤 4：配置 WSGI 文件

这是最关键的步骤。因为 PythonAnywhere 免费版不支持 ASGI，需要桥接。

**打开 PythonAnywhere 的 Web 配置页**，修改 WSGI 文件为：

```python
import os
import sys

# 把后端代码目录加到 Python 路径
path = os.path.expanduser("~/redNoteExpressApp/backend")
if path not in sys.path:
    sys.path.insert(0, path)

from asgiref.wsgi import WsgiToAsgi
from app.main import app

# 把 FastAPI（ASGI）包装成 WSGI
application = WsgiToAsgi(app)
```

**为什么这么写？** PythonAnywhere 的 uWSGI 服务器只认 WSGI 协议。`WsgiToAsgi` 这个包装器让 FastAPI 能跑在 WSGI 服务器上。

---

### 步骤 5：配置环境变量

在 PythonAnywhere Web 配置页面，设置以下环境变量：

| 变量 | 值 |
|------|-----|
| `DATABASE_URL` | `sqlite:////home/你的用户名/rednote.db` |
| `JWT_SECRET` | `rednote-super-secret-2026` |
| `OPENAI_API_KEY` | `sk-你的DeepSeek-Key` |
| `LLM_BASE_URL` | `https://api.deepseek.com` |
| `LLM_MODEL` | `deepseek-chat` |
| `ALLOWED_ORIGINS` | `*` |

⚠️ **SQLite 路径要 4 个斜杠！** `sqlite:////home/...` 不是 3 个！

---

### 步骤 6：创建数据库文件

在 PythonAnywhere Bash 里：
```bash
cd ~
python -c "from sqlalchemy import create_engine; engine = create_engine('sqlite:////home/你的用户名/rednote.db'); from app.models.user import Base; Base.metadata.create_all(bind=engine)"
```

这行命令手动执行了 `Base.metadata.create_all`，创建 `users` 表。

---

### 步骤 7：点 Reload！

回到 PythonAnywhere Web 配置页面，点击绿色的 **Reload** 按钮。

然后访问：`https://你的用户名.pythonanywhere.com/api/health`

如果返回 `{"status": "ok"}`，说明部署成功！

**验证：** 访问 `/api/health` 返回 ok ✅
**验证：** 访问 `/docs` 能看到 Swagger 文档 ✅

---

### 步骤 8：更新前端环境变量

后端地址变了，前端 `VITE_API_URL` 要更新。

在 Vercel Settings → Environment Variables，把 `VITE_API_URL` 改成：
```
https://你的用户名.pythonanywhere.com/api
```

改完后 Vercel 会自动重新部署。

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| SQLite 路径 3 个斜杠 | `OperationalError` | PythonAnywhere 要求 4 个斜杠 | `sqlite:////` 不是 `sqlite:///` |
| 改完代码没生效 | 还是旧的 | PythonAnywhere 不会自动重启 | 去 Web 页面点 Reload |
| 免费版不能连外部数据库 | PostgreSQL 连接超时 | 免费版屏蔽外部网络 | 用 SQLite，或者升级付费版 |
| asgiref 没装 | WSGI 报错 | 依赖没装 | `pip install asgiref --user` |
| 错误日志在哪 | 不知道哪里报错 | — | PythonAnywhere Web 页面 → Log files → Error log |

---

## 📝 总结

**本章核心要点：**
- PythonAnywhere 免费版用 WSGI 桥接跑 FastAPI（`asgiref`）
- SQLite 路径必须 4 个斜杠
- 每次改代码要点 Reload
- 环境变量在 Web 配置页面设置
- 错误日志是排查问题的第一入口

**你现在应该能做到：**
- 独立把 FastAPI 项目部署到 PythonAnywhere
- 排查常见的部署问题
- 配置 WSGI 桥接

🎉 **恭喜！前后端都上线了！你的红薯快写 App 现在全世界都能访问！**

**下一步：** Lab 13 我们整理项目里所有坑点，做一份避坑大全。

---

> —— 阿珊，前端开发者 & AI 提效实践者
