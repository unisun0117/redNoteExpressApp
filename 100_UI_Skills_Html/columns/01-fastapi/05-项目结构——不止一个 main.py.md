# FastAPI 从0到1 第5篇：项目结构：不止一个 main.py

@[TOC](目录)

## 摘要
> 本文教你如何把 FastAPI 项目从单文件拆分成清晰的多模块结构，让代码可维护、可扩展。适合已经写了几个接口但代码全堆在 main.py 里的开发者，学完你就能搭建出专业级的项目骨架。

## 引言

你的第一个 FastAPI 项目是什么样子的？大概率是一个 `main.py` 文件里塞了几百行代码——路由定义、数据库操作、业务逻辑全搅在一起。刚开始还好，加到第 10 个接口的时候，找个 bug 要翻半天。

前端同学应该很熟悉组件化的思路：一个页面拆成多个组件，一个组件一个文件，各管各的。后端项目拆分也是一样的道理——把不同职责的代码分到不同文件里，让每个模块"高内聚、低耦合"。

这篇文章给你展示一个经过实战检验的项目结构。不是网上那种过度设计的分层架构，而是大小刚好、能快速上手、又不会把自己坑进去的实用方案。

## 基础知识储备

- **Python 模块和包**：一个 `.py` 文件就是一个模块，一个有 `__init__.py` 的文件夹就是一个包。FastAPI 项目就是由这些模块和包组成的。
- **单一职责原则**：一个文件只做一件事。router 只管路由，service 只管业务逻辑，model 只管数据定义。
- **依赖注入**：FastAPI 的 `Depends()` 是实现模块解耦的核心机制，前面文章里用过 `Depends(get_db)` 就是这个。
- **前端组件化对比**：`components/Header.tsx` 对应 `routers/users.py`，`hooks/useAuth.ts` 对应 `dependencies/auth.py`——思路完全一样。

## 正文

### 推荐的目录结构

```
app/
├── main.py              # 入口文件，创建 app、注册路由、启动服务
├── config.py            # 所有配置项——数据库 URL、密钥、CORS 等
├── database.py          # SQLAlchemy 引擎、Session、Base 定义
├── models/              # 数据库表模型
│   ├── __init__.py
│   └── user.py
├── schemas/             # Pydantic 请求/响应模型
│   ├── __init__.py
│   └── user.py
├── routers/             # 路由——只管"收到请求 → 调用 service → 返回响应"
│   ├── __init__.py
│   ├── users.py
│   └── posts.py
├── services/            # 业务逻辑——核心计算和数据处理
│   ├── __init__.py
│   └── user_service.py
└── dependencies/        # 可复用的依赖注入
    ├── __init__.py
    └── auth.py
```

这个结构和前端项目的一一对应关系：
- `routers/` = 前端的 `pages/`，每个文件对应一组 URL 路由。
- `services/` = 前端的 `hooks/`（如 `useAuth`、`useFetch`），封装核心逻辑。
- `schemas/` = 前端的 `types/` 或 `interfaces/`，定义数据结构。
- `models/` = 前端的 `api/types`，定义和数据库对应的数据结构。
- `dependencies/` = 前端的 `providers/` 或 Context，提供全局可复用的能力。

### 拆分路由：从 main.py 搬出去

原来的 `main.py`：

```python
@app.get("/api/users")
def get_users(): ...
@app.post("/api/users")
def create_user(): ...
@app.get("/api/posts")
def get_posts(): ...
```

拆成两个路由文件：

```python
# app/routers/users.py
from fastapi import APIRouter

router = APIRouter(prefix="/api/users", tags=["用户"])

@router.get("/")
def get_users():
    return [{"id": 1, "name": "小明"}]

@router.post("/")
def create_user():
    return {"message": "创建成功"}
```

```python
# app/routers/posts.py
from fastapi import APIRouter

router = APIRouter(prefix="/api/posts", tags=["文章"])

@router.get("/")
def get_posts():
    return [{"id": 1, "title": "第一篇文章"}]
```

然后在 `main.py` 里注册：

```python
from fastapi import FastAPI
from app.routers import users, posts

app = FastAPI()
app.include_router(users.router)
app.include_router(posts.router)
```

以后加新功能，你只需要：新建一个 router 文件 → 写路由 → 在 main.py 里 `include_router`。不用改任何已有代码。这就是"对扩展开放，对修改关闭"。

### 拆分业务逻辑：services 层

```python
# app/services/user_service.py
from sqlalchemy.orm import Session
from app.models.user import User
from app.schemas.user import UserCreate

def create_user(db: Session, data: UserCreate) -> User:
    # 这里可以加复杂的业务逻辑：密码加密、初始数据生成、发送欢迎邮件等
    user = User(name=data.name, email=data.email)
    db.add(user)
    db.commit()
    db.refresh(user)
    return user

def get_user_by_email(db: Session, email: str) -> User | None:
    return db.query(User).filter(User.email == email).first()
```

```python
# app/routers/users.py（精简后）
from app.services.user_service import create_user as create_user_svc

@router.post("/")
def create_user(data: UserCreate, db: Session = Depends(get_db)):
    return create_user_svc(db, data)
```

之前路由里有十几行数据库操作代码，现在只剩两行——路由只负责"路由"这件事（接收请求、参数校验、调用 service、返回响应），具体的业务逻辑全在 service 里。方便复用，也方便单独测试。

### 配置集中管理

```python
# app/config.py
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    database_url: str = "sqlite:///./data.db"
    jwt_secret: str = "dev-secret-change-me"
    cors_origins: list[str] = ["http://localhost:5173"]

    class Config:
        env_file = ".env"

settings = Settings()
```

所有配置项集中在一个地方：开发环境用 `.env` 文件，生产环境用环境变量。再也不用在代码里到处找 `"http://localhost"` 这种硬编码了。

## 总结

1. 项目拆分的原则是"高内聚、低耦合"——相关代码放一起，不相关的分开放。
2. routers/ 管路由（URL 映射），services/ 管业务逻辑，schemas/ 管数据结构，models/ 管数据库表——各司其职。
3. `APIRouter` 是 FastAPI 拆分路由的核心工具，用 `prefix` 和 `tags` 让 API 文档自动分组。
4. 配置放到 `config.py` 统一管理，用 pydantic-settings 读取环境变量和 .env 文件。
5. 5 个模块以内的项目可以用这个扁平结构。超过 20 个模块时，可以考虑按业务领域再做一层分包。

## 注意事项

1. 别过度设计——如果你的项目只有 3 个接口，全放 main.py 里也没什么问题。项目拆分要跟着复杂度走，不是越大越好。
2. `from app.routers import users` 这种导入需要 Python 能找到 `app` 包。确保你的启动命令是从包含 `app/` 目录的那个路径执行的。
3. 拆分时先拆路由，再拆 service，最后拆配置。这是最容易、收益也最高的顺序。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 循环导入 | 启动报 `ImportError: circular import` | A 导入了 B，B 又导入了 A | 用一个公共模块（如 database.py）做桥梁，或者用 `TYPE_CHECKING` 延迟导入 |
| router 路径写重复 | 访问 `/api/users/api/users/` 404 | prefix 和路由路径重复拼接 | router 的 prefix 只写到最外层前缀，路由函数里只写相对路径 |
| `__init__.py` 忘了新建 | `ImportError: No module named 'app.routers'` | Python 3.3+ 隐式命名空间包可能行为不一致 | 每个子包都手动创建 `__init__.py`（可以是空文件） |
| main.py 放错位置 | `uvicorn` 启动报找不到 app | main.py 不在项目根目录 | main.py 放在 `app/` 目录下，启动命令用 `uvicorn app.main:app` |
