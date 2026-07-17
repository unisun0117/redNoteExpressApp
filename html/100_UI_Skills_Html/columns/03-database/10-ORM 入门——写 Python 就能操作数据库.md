# 数据库设计从0到1 第10篇：ORM 入门——写Python就能操作数据库
@[TOC](目录)
## 摘要
> 不想写SQL？ORM让你用Python对象操作数据库，就像操作普通类一样。用React状态管理的类比引入，详解SQLAlchemy核心用法，包含完整项目实战代码。

## 引言

前九篇文章你一直在写SQL——CREATE TABLE、INSERT、SELECT、JOIN。但到了真实项目中，特别是在前后端分离架构里，后端代码全用原始SQL会有几个痛点：

1. **字符串拼接的SQL容易出错**——拼错一个逗号，调试半天。
2. **数据库切换成本高**——SQLite的SQL和PostgreSQL的SQL不完全一样，切换意味着大量重写。
3. **代码和数据库脱节**——Python里改了User类的字段，数据库里还得手动ALTER TABLE。
4. **SQL注入风险**——虽然有参数化查询，但总有新人或者不小心的时候忘记用。

这就是ORM（Object-Relational Mapping，对象关系映射）的价值。它在你熟悉的Python对象和不熟悉的SQL字符串之间架了一座桥。

```python
# 原始SQL写法
cursor.execute("SELECT * FROM users WHERE age > ?", (18,))

# ORM写法 —— 用Python表达查询意图
users = session.query(User).filter(User.age > 18).all()
```

本文是本专栏的最后一篇，我们用SQLAlchemy这个Python最流行的ORM框架，把前面9篇文章学到的SQL知识"翻译"成Python代码。读完你会发现：原来操作数据库可以这么Pythonic。

## 基础知识储备

1. **掌握Python类与对象**：知道`class`、`__init__`、实例方法是什么。
2. **熟悉本专栏前9篇的SQL知识**：CRUD、JOIN、事务、表关系——本文是这些知识的ORM"翻译版"。
3. **用过TypeScript的类型注解**（加分项）：ORM定义模型和TypeScript定义接口非常相似。
4. **安装SQLAlchemy**：`pip install sqlalchemy` 即可，不需要额外服务。

## 正文

### 一、什么是ORM？—— 翻译官的角色

ORM的职责一句话概括：**把Python类的操作翻译成SQL语句。**

```python
# 你写的Python操作
user = User(name="张三", email="zhangsan@example.com")
session.add(user)
session.commit()

# ORM帮你翻译成的SQL
INSERT INTO users (name, email) VALUES ('张三', 'zhangsan@example.com');
```

**ORM三层映射关系：**

| Python概念 | 映射为 | SQL概念 |
|-----------|--------|---------|
| 类（class） | → | 表（table） |
| 实例属性（self.name） | → | 列（column） |
| 实例（instance） | → | 行（row） |

这个映射思想和前端的"组件化思维"是一脉相承的：React组件≈数据库表，Props≈列，组件实例≈行数据。

### 二、SQLAlchemy 快速上手

先来看一个完整的最小示例——定义模型、建表、CRUD：

```python
from sqlalchemy import create_engine, Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import declarative_base, Session, relationship

# 1. 创建引擎和基类
engine = create_engine('sqlite:///orm_demo.db', echo=False)
Base = declarative_base()

# 2. 定义模型（Python类 = 数据库表）
class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True, autoincrement=True)
    username = Column(String(50), nullable=False, unique=True)
    email = Column(String(100), nullable=False)
    age = Column(Integer)

    # 关系：一个用户有多篇文章
    posts = relationship('Post', back_populates='author')

    def __repr__(self):
        return f"<User(id={self.id}, username='{self.username}')>"

class Post(Base):
    __tablename__ = 'posts'

    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(200), nullable=False)
    content = Column(String)
    user_id = Column(Integer, ForeignKey('users.id'))

    # 关系：一篇文章属于一个用户
    author = relationship('User', back_populates='posts')

    def __repr__(self):
        return f"<Post(id={self.id}, title='{self.title}')>"

# 3. 建表
Base.metadata.create_all(engine)

# 4. 操作数据
session = Session(engine)

# --- Create ---
user = User(username='Alice', email='alice@example.com', age=25)
session.add(user)
session.commit()
print(f"创建用户：{user}")

# --- Read ---
users = session.query(User).filter(User.age > 20).all()
print(f"年龄大于20的用户：{users}")

# --- Update ---
user = session.query(User).filter(User.username == 'Alice').first()
user.age = 26
session.commit()
print(f"更新年龄：{user} 新年龄：{user.age}")

# --- Delete ---
session.query(User).filter(User.username == 'Alice').delete()
session.commit()
print("删除Alice")

session.close()
```

### 三、ORM中的"表关系"

ORM最让人舒服的地方就是处理表关系——不需要手写JOIN，用属性访问就行了。

**一对多关系：**

```python
# 创建用户和文章
user = User(username='Bob', email='bob@example.com')
post1 = Post(title='第一篇文章', content='Hello World')
post2 = Post(title='第二篇文章', content='ORM真方便')

# 通过relationship关联——不需要手动设user_id
user.posts = [post1, post2]
session.add(user)
session.commit()

# 查询——通过属性导航，不用写JOIN！
user = session.query(User).filter(User.username == 'Bob').first()
print(f"{user.username} 的文章：")
for post in user.posts:   # 直接.user.posts，ORM自动帮你JOIN
    print(f"  - {post.title}")
```

**多对多关系（需要中间表）：**

```python
from sqlalchemy import Table

# 中间表定义
post_tags = Table('post_tags', Base.metadata,
    Column('post_id', Integer, ForeignKey('posts.id'), primary_key=True),
    Column('tag_id', Integer, ForeignKey('tags.id'), primary_key=True)
)

class Tag(Base):
    __tablename__ = 'tags'
    id = Column(Integer, primary_key=True)
    name = Column(String(50), unique=True, nullable=False)
    posts = relationship('Post', secondary=post_tags, back_populates='tags')

# Post类里补充tags关系
Post.tags = relationship('Tag', secondary=post_tags, back_populates='posts')

# 使用——同样通过属性访问
tag = Tag(name='Python')
post = Post(title='ORM进阶', content='...')
post.tags.append(tag)
session.add(post)
session.commit()

# 查询某篇文章的所有标签
post = session.query(Post).filter(Post.id == 1).first()
print([tag.name for tag in post.tags])  # ['Python']
```

### 四、实际项目结构 —— 博客系统的ORM版

回顾第8篇博客系统的表设计，用SQLAlchemy重新实现：

```python
# models.py —— 所有模型定义
from sqlalchemy import (
    create_engine, Column, Integer, String, Float, Text,
    ForeignKey, Table, DateTime,
)
from sqlalchemy.orm import declarative_base, relationship
from datetime import datetime

engine = create_engine('sqlite:///blog.db')
Base = declarative_base()

# 关注表（自引用多对多）
follows = Table('follows', Base.metadata,
    Column('follower_id', Integer, ForeignKey('users.id'), primary_key=True),
    Column('following_id', Integer, ForeignKey('users.id'), primary_key=True)
)

# 点赞表
likes = Table('likes', Base.metadata,
    Column('user_id', Integer, ForeignKey('users.id'), primary_key=True),
    Column('post_id', Integer, ForeignKey('posts.id'), primary_key=True)
)

# 文章-标签关联表
post_tags = Table('post_tags', Base.metadata,
    Column('post_id', Integer, ForeignKey('posts.id'), primary_key=True),
    Column('tag_id', Integer, ForeignKey('tags.id'), primary_key=True)
)

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    username = Column(String(50), unique=True, nullable=False)
    email = Column(String(100), unique=True, nullable=False)
    bio = Column(Text, default='')
    created_at = Column(DateTime, default=datetime.utcnow)

    posts = relationship('Post', back_populates='author', cascade='all, delete-orphan')
    comments = relationship('Comment', back_populates='author', cascade='all, delete-orphan')
    # 自引用多对多
    following = relationship('User', secondary=follows,
        primaryjoin=(follows.c.follower_id == id),
        secondaryjoin=(follows.c.following_id == id),
        backref='followers')

class Post(Base):
    __tablename__ = 'posts'
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'), nullable=False)
    title = Column(String(200), nullable=False)
    content = Column(Text, default='')
    status = Column(String(20), default='draft')
    created_at = Column(DateTime, default=datetime.utcnow)

    author = relationship('User', back_populates='posts')
    comments = relationship('Comment', back_populates='post', cascade='all, delete-orphan')
    tags = relationship('Tag', secondary=post_tags, back_populates='posts')

class Comment(Base):
    __tablename__ = 'comments'
    id = Column(Integer, primary_key=True)
    post_id = Column(Integer, ForeignKey('posts.id'), nullable=False)
    user_id = Column(Integer, ForeignKey('users.id'), nullable=False)
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    post = relationship('Post', back_populates='comments')
    author = relationship('User', back_populates='comments')

class Tag(Base):
    __tablename__ = 'tags'
    id = Column(Integer, primary_key=True)
    name = Column(String(50), unique=True, nullable=False)
    posts = relationship('Post', secondary=post_tags, back_populates='tags')

# 创建所有表
Base.metadata.create_all(engine)
```

使用这个模型的查询现在变得极其直观：

```python
from sqlalchemy.orm import Session

session = Session(engine)

# 查首页文章列表（带作者和评论数）
posts = session.query(Post).filter(Post.status == 'published')\
    .order_by(Post.created_at.desc()).limit(20).all()
for p in posts:
    print(f"《{p.title}》by {p.author.username}，{len(p.comments)}条评论")

# 用户关注了谁
user = session.query(User).filter(User.username == 'Alice').first()
for f in user.following:
    print(f"关注了：{f.username}")

# 谁关注了这个用户
for f in user.followers:
    print(f"被{f.username}关注")

session.close()
```

### 五、ORM vs 原始SQL —— 理性选择

| 维度 | ORM | 原始SQL |
|------|-----|---------|
| 开发速度 | 快——模型定义清晰，代码提示好 | 慢——字符串拼接容易出错 |
| 学习曲线 | 需要额外学框架 | 只需学SQL |
| 复杂查询 | 相对别扭（多表JOIN、子查询） | 灵活——SQL就是为此设计的 |
| 性能 | 有轻微开销（对象映射） | 最优 |
| 数据库迁移 | 换数据库基本不用改代码 | 可能要重写SQL |
| 调试 | 慢查询不直观（需看生成的SQL） | 直接看到SQL |

**实用建议：**
- 简单的CRUD和常规JOIN：用ORM，爽。
- 复杂报表、聚合、多表嵌套子查询：用原始SQL或者ORM的`text()`功能混合使用。
- 数据库迁移：用ORM + Alembic做版本化管理。

```python
# 混用ORM和原始SQL —— 两全其美
from sqlalchemy import text

# 复杂报表直接写SQL
result = session.execute(text("""
    SELECT u.username, COUNT(p.id) as post_count
    FROM users u
    LEFT JOIN posts p ON u.id = p.user_id
    GROUP BY u.id
    ORDER BY post_count DESC
    LIMIT 10
"""))
for row in result:
    print(f"{row.username}: {row.post_count}篇")
```

## 总结

1. ORM把表映射为类、列映射为属性、行映射为实例——用你熟悉的Python对象操作数据库。
2. SQLAlchemy是目前Python生态最成熟、最强大的ORM——`declarative_base`定义模型，`session`管理事务。
3. `relationship`让表关系用属性导航——不需要手写JOIN，`user.posts`自动加载关联数据。
4. 多对多关系通过中间表+`secondary`参数实现——和原始SQL的逻辑一致，但写法更清爽。
5. ORM和原始SQL不是非此即彼——复杂查询用原始SQL，常规CRUD用ORM，混合使用是最佳实践。

## 注意事项

1. ORM生成的SQL可能不是你想象的那样——尤其是`relationship`默认是lazy loading（懒加载），遍历`user.posts`时会额外发一条查询（N+1问题）。用`joinedload`或`selectinload`一次性预加载。
2. 不要在生产环境使用`Base.metadata.create_all(engine)`自动建表——这应该只在开发环境用。生产环境应该用Alembic做数据库迁移版本管理。
3. session的生命周期管理很重要——用完记得close，或者用`with Session(engine) as session`上下文管理器。不要一个session用整个应用生命周期。

## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| N+1查询问题 | 查10个用户，发了11条SQL（1条查用户+10条查文章） | relationship默认lazy loading | 用`joinedload`或`selectinload`: `session.query(User).options(joinedload(User.posts))` |
| 修改了对象但没commit | 程序退出后修改全丢了 | ORM的修改在内存中，需要commit才写入磁盘 | 修改操作后调用`session.commit()`；或者用`session.flush()`先刷到数据库但不提交 |
| 在Session关闭后访问relationship | 报`DetachedInstanceError` | session关闭后对象脱离了数据库连接，懒加载无法执行 | 在session关闭前完成所有属性访问；或用`expire_on_commit=False` |
| ORM对象比较用`is`不用`==` | 两个相同数据的对象判断不相等 | ORM的`==`比较的是Python对象身份，不是数据库值 | 比较id：`user1.id == user2.id`；或者对非ORM对象的属性直接用`==` |
