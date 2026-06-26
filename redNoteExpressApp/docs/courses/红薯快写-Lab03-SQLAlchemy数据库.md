# Lab 03：SQLAlchemy 数据库 — 用户表建起来

> **预计用时：** 35-40 分钟
> **难度：** ⭐⭐
> **前置 Lab：** Lab 02（后端必须能启动）

---

## 📌 前言

建数据库这件事，我第一个项目就搞砸过。当时不用 ORM，手写 SQL 语句：`CREATE TABLE users (id VARCHAR, email VARCHAR...)`。结果需求一变，要加字段、改类型——我对着 SQL 改了半天，还改漏了一个地方，线上直接炸了。

后来用了 SQLAlchemy ORM，感觉世界都清爽了——**改数据库只需要改 Python 类，一个属性对应一个字段。** 建表、改表、查数据，全部用 Python 代码完成，不用写一行 SQL。

这一 Lab，我们给红薯快写 App 创建第一张表——用户表。后面注册登录、点数扣减、VIP 管理，全围绕这张表展开。

---

## 📚 基础知识储备

- **数据库** — 存数据的地方。我们用的是 SQLite（一个轻量级数据库，不需要安装，一个文件就是数据库）
- **表（Table）** — 数据库里的一张"Excel 表格"。比如"用户表"存所有用户信息
- **字段（Column）** — 表的一列。比如"用户表"有 id、email、password_hash、credits_remaining 等字段
- **ORM** — Object Relational Mapping，对象关系映射。翻译成人话：**用 Python 类代替 SQL 语句操作数据库**
- **SQLAlchemy** — Python 最流行的 ORM 库

> 💡 如果你之前只会前端，数据库这块是最容易卡住的地方。别急，跟着敲就行。

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 理解 SQLAlchemy 的三个核心文件：`config.py`、`database.py`、`models/user.py`
2. 看懂 User 模型的字段定义
3. 理解后端启动时自动建表的机制
4. 用命令行验证数据库文件是否正确创建

---

## 🛠 动手实战

### 步骤 1：读懂配置文件 `config.py`

**做什么：** 所有配置项都在这里，包括数据库地址

**打开文件：** `backend/app/config.py`

```python
import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # 数据库连接地址
    DATABASE_URL: str = "sqlite:///./rednote.db"
    #                      ↑      ↑     ↑
    #                      协议   路径   数据库文件名

    # ... 其他配置后面会用到
    JWT_SECRET: str = "dev-secret-change-in-production"
    OPENAI_API_KEY: str = ""
    LLM_MODEL: str = "deepseek-chat"
    LLM_BASE_URL: str = "https://api.deepseek.com"
    FREE_CREDITS: int = 10
    ALLOWED_ORIGINS: str = "http://localhost:5173"

    class Config:
        env_file = ".env"  # 可以从 .env 文件读取配置

settings = Settings()  # 创建一个全局配置实例
```

**关键理解：**
- `DATABASE_URL = "sqlite:///./rednote.db"` — 三个斜杠！表示 SQLite 数据库文件在当前目录下的 `rednote.db`
- `pydantic-settings` 能自动从环境变量或 `.env` 文件读取配置，覆盖默认值
- `settings` 是全局单例，整个项目通过 `from app.config import settings` 使用配置

**验证：** 能找到 `DATABASE_URL` 和 `settings = Settings()` ✅

---

### 步骤 2：读懂数据库连接 `database.py`

**做什么：** 创建数据库引擎和会话工厂

**打开文件：** `backend/app/database.py`

```python
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase
from app.config import settings

# 1. 创建引擎 —— 理解为"数据库连接池"
engine = create_engine(
    settings.DATABASE_URL,
    # SQLite 特殊参数：允许多线程访问
    connect_args={"check_same_thread": False} if "sqlite" in settings.DATABASE_URL else {},
)

# 2. 创建会话工厂 —— 每次请求拿一个会话，用完归还
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 3. 声明基类 —— 所有数据模型都继承它
class Base(DeclarativeBase):
    pass

# 4. 获取数据库会话的依赖函数（FastAPI 用）
def get_db():
    db = SessionLocal()
    try:
        yield db  # 给接口用
    finally:
        db.close()  # 用完关掉
```

**三句话理解：**
- `engine` = 数据库连接，启动时创建一次
- `SessionLocal` = 每次请求拿一个新的会话（相当于一个"事务"）
- `Base` = 所有表模型都要继承它

**验证：** 找到 `engine`、`SessionLocal`、`Base`、`get_db` 四个东西 ✅

---

### 步骤 3：读懂用户模型 `models/user.py`

**做什么：** 定义用户表长什么样

**打开文件：** `backend/app/models/user.py`

```python
import uuid
from datetime import datetime
from sqlalchemy import String, Integer, DateTime, Enum as SAEnum
from sqlalchemy.orm import Mapped, mapped_column
from app.database import Base
import enum

# 用户等级枚举
class UserTier(str, enum.Enum):
    FREE = "free"
    VIP = "vip"

# 用户表模型
class User(Base):                    # ← 继承 Base
    __tablename__ = "users"          # ← 表名叫 "users"

    id: Mapped[str] = mapped_column(
        String(36), primary_key=True,
        default=lambda: str(uuid.uuid4())  # 自动生成 UUID
    )
    email: Mapped[str | None] = mapped_column(
        String(255), unique=True, nullable=True
    )
    wechat_openid: Mapped[str | None] = mapped_column(
        String(128), unique=True, nullable=True
    )
    password_hash: Mapped[str | None] = mapped_column(
        String(255), nullable=True
    )
    tier: Mapped[UserTier] = mapped_column(
        SAEnum(UserTier), default=UserTier.FREE
    )
    credits_remaining: Mapped[int] = mapped_column(
        Integer, default=0
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime, default=datetime.utcnow
    )
```

**字段解读：**

| 字段 | 类型 | 含义 |
|------|------|------|
| `id` | String(36) | 用户唯一 ID，UUID 自动生成 |
| `email` | String(255) | 邮箱（可选，唯一） |
| `wechat_openid` | String(128) | 微信 OpenID（可选，唯一） |
| `password_hash` | String(255) | 密码哈希（不是明文！） |
| `tier` | Enum | 用户等级：free 或 vip |
| `credits_remaining` | Integer | 剩余生成次数 |
| `created_at` | DateTime | 注册时间 |

**验证：** 能说出至少 4 个字段的含义 ✅

---

### 步骤 4：理解自动建表机制

**做什么：** 看 `main.py` 里怎么自动创建表的

**打开 `main.py` 第 10 行：**
```python
Base.metadata.create_all(bind=engine)
```

**这一行做了什么？**
1. `Base.metadata` — 收集所有继承自 `Base` 的模型（目前只有 User）
2. `.create_all(bind=engine)` — 在 engine 连接的数据库里，创建所有还没存在的表
3. **如果表已存在，不会重复创建**（所以不会丢数据）

这就是为什么你第一次启动后端时，`rednote.db` 文件会自动生成。

**验证：** 能说出 `Base.metadata.create_all` 的作用 ✅

---

### 步骤 5：启动后端，验证数据库文件生成

**操作：**
```bash
cd E:\AI_Workspace\redNoteExpressApp\backend
venv\Scripts\activate
.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8000
```

启动成功后，检查 `backend` 目录下是否出现了 `rednote.db` 文件：
```bash
dir *.db
# 应该看到 rednote.db
```

**验证：** `backend` 目录下有了 `rednote.db` 文件 ✅

---

### 步骤 6（可选）：用 DB Browser 查看表结构

如果你想把数据库打开看看里面有什么：

1. 下载 DB Browser for SQLite：https://sqlitebrowser.org/dl/
2. 打开 `rednote.db`
3. 切到"数据库结构"标签
4. 你会看到 `users` 表，展开能看到所有字段

**验证：** 能看到 `users` 表及其字段 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| SQLite 路径不对 | `OperationalError: unable to open database file` | 路径没权限或不存在 | 确保 `DATABASE_URL` 用相对路径 `sqlite:///./rednote.db` |
| 字段改了但表没变 | 改了模型但数据库还是旧的 | `create_all` 不会修改已存在的表 | 删掉 `rednote.db` 重新启动（开发阶段可以这样，生产不行） |
| 导包报错 | `ImportError: cannot import name` | 循环导入 | 按现有 import 方式写，别乱改导入顺序 |

---

## 📝 总结

**本章核心要点：**
- SQLAlchemy 三件套：`engine`（连接）、`SessionLocal`（会话）、`Base`（基类）
- 一个模型 = 一张表，一个属性 = 一个字段
- `Base.metadata.create_all(bind=engine)` 自动建表
- SQLite 的数据库就是一个 `.db` 文件，不需要额外安装

**你现在应该能做到：**
- 看懂 `config.py` → `database.py` → `models/user.py` 的调用链
- 说出 User 表有哪些字段
- 启动后端并验证 `rednote.db` 文件生成

**下一步：** Lab 04 我们给用户表加上注册和登录功能——让前端能真正创建用户。

---

> —— 阿珊，前端开发者 & AI 提效实践者
