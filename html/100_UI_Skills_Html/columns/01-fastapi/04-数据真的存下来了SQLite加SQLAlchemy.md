# FastAPI 从0到1 第4篇：数据存下来：SQLite + SQLAlchemy 入门

@[TOC](目录)

## 摘要
> 本文教你把 API 的数据真正存到数据库里，使用 SQLite + SQLAlchemy 实现完整的数据持久化。适合已经会写接口但数据还停留在内存里的开发者，学完你就能用 ORM 操作数据库。

## 引言

前面几篇文章写的接口都有一个硬伤——服务一重启，数据全没了。因为数据存在 Python 变量里，进程结束内存就释放了。真正上线的系统，数据必须存到磁盘上，服务重启也不丢。

前端同学可能用过 `localStorage` 来做数据持久化，但后端的"数据持久化"指的是关系型数据库，比如 MySQL、PostgreSQL、SQLite。它们能存百万级数据，还能做复杂的查询和关联。

本文选 SQLite 是因为它零配置——不需要装数据库服务，一个文件就是数据库。结合 SQLAlchemy ORM，你写 Python 代码就能操作数据库，不用手写 SQL。等你要上生产环境了，把 SQLite 换成 PostgreSQL，代码几乎不用改。

## 基础知识储备

- **数据库基础**：知道什么是表、行、列、主键、外键。和 Excel 表格类比——表就是 sheet，行就是一条记录，列就是字段。
- **SQL 基本概念**：SELECT（查）、INSERT（增）、UPDATE（改）、DELETE（删），知道这四个就够了。
- **ORM 是什么**：Object Relational Mapping，把数据库里的表映射成 Python 类，把一行记录映射成一个对象。写 `db.query(User).filter_by(name="小明")` 比写 `SELECT * FROM users WHERE name='小明'` 直观得多。
- **SQLite 特性**：轻量级、零配置、用一个 .db 文件存储所有数据，非常适合学习和原型开发。

## 正文

### SQLAlchemy 三部曲：引擎、Session、模型

```python
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase

# 1. 创建引擎——SQLite 数据库文件就是 data.db
engine = create_engine("sqlite:///./data.db", echo=True)

# 2. 创建 Session 工厂——每次请求拿一个新 session
SessionLocal = sessionmaker(bind=engine, autoflush=False)

# 3. 定义模型基类
class Base(DeclarativeBase):
    pass
```

这三个概念对应到前端开发经验：
- **引擎（engine）** 就像数据库的连接字符串，告诉程序"数据库在哪"。
- **Session** 就像前端的一个请求周期——打开页面时创建，关闭页面时销毁。每次请求拿一个独立的会话，互不干扰。
- **Base** 是所有模型的父类，相当于定义了一个"所有表都遵循的规矩"。

### 定义第一个模型

```python
from sqlalchemy import Column, Integer, String

class User(Base):
    __tablename__ = "users"  # 数据库里的表名

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(50), nullable=False)
    email = Column(String(100), unique=True, nullable=False)
    age = Column(Integer, default=0)
```

定义一个模型，就是在用一个 Python 类描述数据库中的一张表。这和你用 TypeScript 定义 `interface User { id: number; name: string; ... }` 有异曲同工之妙，只不过 SQLAlchemy 的模型还额外描述了"这个字段是主键、这个字段有唯一约束"等数据库层面的信息。

### FastAPI 集成：依赖注入获取 Session

```python
from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session

app = FastAPI()

# 依赖注入函数——每个请求自动获取一个数据库会话
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/api/users")
def create_user(name: str, email: str, db: Session = Depends(get_db)):
    user = User(name=name, email=email)
    db.add(user)      # 相当于 INSERT
    db.commit()       # 相当于 COMMIT，真正写入磁盘
    db.refresh(user)  # 刷新以获取数据库生成的 id
    return {"id": user.id, "name": user.name, "email": user.email}

@app.get("/api/users")
def list_users(db: Session = Depends(get_db)):
    users = db.query(User).all()  # 相当于 SELECT * FROM users
    return users
```

这里有个关键技巧——`Depends(get_db)`。FastAPI 在每次请求时会调用 `get_db` 函数，把它的返回值（一个 Session 对象）注入到路由函数里。请求结束时自动 close。这个模式能做到"一次请求一个会话"，避免连接泄漏。

### 常用查询速查

```python
# 按主键查
user = db.query(User).filter(User.id == user_id).first()

# 多条件
users = db.query(User).filter(User.age > 18, User.name.like("%明%")).all()

# 排序 + 分页
users = db.query(User).order_by(User.id.desc()).offset(0).limit(10).all()

# 更新
user = db.query(User).filter(User.id == user_id).first()
user.name = "新名字"
db.commit()

# 删除
user = db.query(User).filter(User.id == user_id).first()
db.delete(user)
db.commit()
```

这些查询和前端 Array 的 `filter`、`sort`、`slice` 操作是一个思路，只是换到了数据库层面执行。ORM 的核心价值就是让你用面向对象的方式做这些操作，不用写 SQL。

## 总结

1. SQLAlchemy 是 Python 生态最流行的 ORM，把表映射成类、把行映射成对象，面向对象操作数据库。
2. 引擎（engine）管连接，Session 管事务，Base 管映射——三部曲建立好，后面只管写业务。
3. FastAPI 的 `Depends()` 依赖注入完美适配数据库 Session 管理，一次请求一个会话，自动关闭。
4. SQLite 零配置、一个文件即数据库，适合学习和小型项目。上生产换成 PostgreSQL 即可。
5. CRUD 操作写起来很自然：`db.add`、`db.query`、`db.commit`，不需要手写一行 SQL。

## 注意事项

1. `create_engine` 里的 `echo=True` 会把所有 SQL 语句打印到控制台，调试时很方便，但上线后记得关掉，否则日志会爆炸。
2. 每次 `db.commit()` 后才真正写入磁盘。`db.add()` 只是标记了"要插入"，不 `commit` 就丢了。
3. 数据库文件路径 `.db` 要加进 `.gitignore`，不要把开发数据提交到版本库。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 忘记创建表 | 接口报 `no such table: users` | 定义了模型后没执行 `Base.metadata.create_all(bind=engine)` | 在应用启动时调用 `create_all()`，或上生产用 Alembic 迁移 |
| Session 未关闭 | 数据库连接越来越多，最终报错 | 手动创建 session 后忘了 `.close()` | 用 `try-finally` 或 FastAPI 的依赖注入自动管理 |
| SQLite 并发写入 | 高并发时出现 `database is locked` | SQLite 同一时间只允许一个写操作 | SQLite 适合低并发场景，上生产换 PostgreSQL |
| 查询结果转 JSON 报错 | `Object is not JSON serializable` | SQLAlchemy 对象不能直接 json.dumps() | 用 Pydantic 的 `response_model` 自动序列化 |
