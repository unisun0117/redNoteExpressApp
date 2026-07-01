# 第6篇：数据库上云 — Neon 免费 PostgreSQL

> SQLite 是本地文件，部署到 Render 上每次重启就没了。Neon 给你一个免费的云 PostgreSQL——数据永远在，跟你本地 SQLite 一样的用法，只是连接字符串从 `sqlite:///` 变成了 `postgresql://`。

## 📌 前端为什么要学这个？

金橘记账开发的时候，我用 SQLite 爽得很——一个 `finance.db` 文件放在 backend 目录下，不用安装、不用配置、不用注册账号。SQLAlchemy 帮你建表、帮你增删改查，跟写 Python 代码一样自然。

部署到 Render 之后的第二天，我打开 App 一看——数据全没了。

我慌了。用户注册了、记账记了一堆，重启一下 Render 服务就归零了。我才意识到一个事：**Render 免费版的硬盘不是持久化的**。你的 SQLite 文件存在 Render 的临时文件系统里，每次重启都会清空。这就像是你的 React 应用把用户数据存在了 `useState` 里——一刷新页面就没了。

我以为要花钱买云数据库了。AWS RDS 最便宜的实例一个月 15 美元，换算成人民币 100 多块——个人项目花这个钱，肉疼。

然后我发现了 **Neon**。

Neon 是一个免费的 Serverless PostgreSQL 数据库，给 0.5GB 存储空间，足够个人项目用好几年。对前端转全栈的人来说，这就是数据库界的 Vercel——注册一个账号，拿一条连接字符串，改一行代码，完事。

> 阿珊说：开发用 SQLite 是图方便，上线用 PostgreSQL 是负责任。就像你写 demo 用 localhost，上线得买个域名——不是技术限制，是态度问题。

## 🔍 核心原理

### SQLite vs PostgreSQL：文件数据库 vs 服务器数据库

SQLite 和 PostgreSQL 的本质区别，用前端概念来理解就是：

| 对比维度 | SQLite | PostgreSQL |
|---------|--------|------------|
| **本质** | 一个文件（`.db`） | 一个独立运行的服务器进程 |
| **前端类比** | `localStorage` | IndexedDB / 云数据库 |
| **存储位置** | 跟你的代码放在一起 | 远程服务器上，通过 TCP 连接 |
| **并发能力** | 同一时间只能一个连接写 | 支持成百上千并发连接 |
| **安装** | Python 自带，零配置 | 需要安装并启动一个服务 |
| **数据类型** | 只有 5 种（INTEGER, TEXT, REAL, BLOB, NULL） | 丰富的类型（JSON, ARRAY, UUID, TIMESTAMP 等） |
| **SQL 标准** | 宽松，很多语法"能用"但不标准 | 严格遵循 SQL 标准 |
| **适用场景** | 本地开发、移动端 App 嵌入、单机小工具 | 任何有多个用户同时在线的 Web 应用 |

理解这个区别最关键的一点：**SQLite 是一个库，不是服务器**。

用前端的话说——`localStorage` 是一个 API，你在浏览器里直接调用，数据存在用户电脑上。IndexedDB 也是存在用户电脑上，但容量更大、查询更强大。而云数据库是一个**远程服务**——你发 HTTP 请求过去，它帮你存数据，不管你换了几台电脑、清了多少次缓存，数据都在。

SQLite 就是前端的 `localStorage`：简单、零配置、一刷新 Render 就没了。PostgreSQL 就是云数据库：需要注册账号、需要连接字符串、但数据永远在。

### Neon 是什么？

Neon 是一个 **Serverless PostgreSQL**。

"Serverless"在这的意思不是说没有服务器——而是说**你不用管服务器**。Neon 帮你管理 PostgreSQL 实例的启动、扩缩容、备份、升级。你只要拿到一条连接字符串，往代码里一贴就行。

Neon 的免费套餐包含：
- **0.5 GB 存储**：金橘记账这种个人项目，500M 够用几年
- **100 小时活跃计算时间/月**：不访问的时候自动休眠，不消耗时间
- **1 个数据库项目**：够你放所有表
- **自动备份**：每天自动备份，保留 7 天

为什么 Neon 免费还靠谱？因为它是按"计算时间"收费的。个人项目的数据库大部分时间在等请求，真正干活的时间很少。Neon 的商业模式是向企业客户收钱，个人开发者免费额度绰绰有余。

用前端的概念理解：Neon 就像是 Firebase Firestore 的免费套餐——个人项目免费，量大了才付费。区别是 Neon 给你的是**标准 PostgreSQL**，你写的 SQL 语句在哪家 PostgreSQL 都能跑，不会被平台锁定。

### 你这三处代码要改

从 SQLite 迁移到 PostgreSQL，SQLAlchemy 已经帮你扛了 90% 的工作——增删改查的 Python 代码一行不用改。但有三处需要手动处理：

**第一处：连接字符串**

```python
# 改前（SQLite — backend/app/config.py）
DATABASE_URL: str = "sqlite:///./finance.db"

# 改后（Neon PostgreSQL）
DATABASE_URL: str = "postgresql://username:password@ep-xxx.us-east-2.aws.neon.tech/finance?sslmode=require"
```

注意 `sqlite:///` 后面是文件路径，`postgresql://` 后面是 **用户名:密码@主机地址/数据库名**。跟 URL 的格式一样——`https://user:pass@host/path`。

另外 URL 后面必须加 `?sslmode=require`，因为 Neon 强制加密连接。不加这个参数数据库连不上。

**第二处：SQLite 的 `.like()` 在 PostgreSQL 上不兼容**

金橘记账里有一个按月筛选交易的功能，用 SQLite 的 `tx_date.like(f"{month}%")` 来匹配日期字符串：

```python
# backend/app/routers/transactions.py 第73行
# 改前（SQLite — 把 Date 字段当字符串用 like 匹配）
query = query.filter(Transaction.tx_date.like(f"{month}%"))

# 改后（PostgreSQL — cast 转成字符串才能用 like）
from sqlalchemy import cast, String
query = query.filter(cast(Transaction.tx_date, String).like(f"{month}%"))
```

为什么？因为 SQLite 没有真正的 DATE 类型——它把日期存成 TEXT，所以 `.like()` 能直接匹配字符串。PostgreSQL 有严格的 DATE 类型，没转成字符串之前不能直接 `LIKE`。这是 SQLite 转 PostgreSQL 最常见的坑——SQLite 太宽容了，让你养成了"能跑就行"的习惯。

**第三处：`check_same_thread` 参数**

```python
# backend/app/database.py
# 改前
engine = create_engine(
    settings.DATABASE_URL,
    connect_args={"check_same_thread": False} if "sqlite" in settings.DATABASE_URL else {},
)

# 改后
engine = create_engine(settings.DATABASE_URL)
```

`check_same_thread` 是 SQLite 特有的参数——因为 SQLite 是文件数据库，默认不允许跨线程访问，FastAPI 的多线程处理需要关掉这个检查。PostgreSQL 是服务器数据库，天生支持多连接、多线程，不需要这个参数。项目里已经用了条件判断来兼容两种数据库，很不错——但如果你没加这个判断，迁移到 PostgreSQL 的时候会报 `psycopg2` 不支持这个参数的错误。

**不需要改的：金额存分**

金橘记账的金额用 **整型（分）** 存储，不是浮点型（元）。这个设计在 PostgreSQL 上完全不需要改——`amount: int` 存进 PostgreSQL 的 `Integer` 列，精确到分，不会有精度丢失。这是一个好习惯：不管什么数据库，金额永远存整型。存浮点数不管 SQLite 还是 PG 都会丢精度。

## 🛠 动手试试

### 第一步：注册 Neon 并创建数据库

1. 打开 [neon.tech](https://neon.tech)，用 GitHub 账号注册
2. 登录后会看到 Dashboard，点击 **Create a project**
3. 填写：
   - **Name**: `finance-tracker`（随便填）
   - **Region**: 选离你最近的机房（亚洲用户选 `Singapore` 或 `Tokyo`）
   - **PostgreSQL version**: 选最新的（默认就行）
4. 点击 **Create project**

等 30 秒，Neon 会给你准备好一个全新的 PostgreSQL 实例。

### 第二步：获取连接字符串

创建完成后，Neon 会弹出一个面板显示连接信息：

```
postgresql://finance_owner:xxxxxxxxxxxx@ep-cool-tree-a1b2c3d4.ap-southeast-1.aws.neon.tech/finance?sslmode=require
```

这条 URL 拆开来看：

```
postgresql://  finance_owner  :  xxxxxxxxxxxx  @  ep-cool-tree-xxx.xxx.aws.neon.tech  /finance  ?sslmode=require
   ^^^             ^^^              ^^^            ^^^                                  ^^^            ^^^
   协议           用户名             密码            主机地址                              数据库名        SSL强制
```

保存这条连接字符串，等会要用。

Neon 的项目里有一个 **SQL Editor** 页签，你可以在网页上直接执行 SQL 语句。点进去试一下：

```sql
SELECT version();
-- 返回：PostgreSQL 16.x on x86_64-pc-linux-gnu...

SELECT now();
-- 返回当前时间戳。数据已经在云上了！
```

### 第三步：改金橘记账的配置

打开 `backend/app/config.py`，把 DATABASE_URL 的默认值改成 Neon 连接字符串：

```python
# backend/app/config.py
class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql://finance_owner:xxxx@ep-xxx.ap-southeast-1.aws.neon.tech/finance?sslmode=require"
    JWT_SECRET: str = "finance-dev-secret-2026"
    # ... 其他配置不变
```

**不要把密码硬编码在代码里！** 更好的做法是用环境变量：

```python
# backend/app/config.py — 推荐写法
class Settings(BaseSettings):
    DATABASE_URL: str = "sqlite:///./finance.db"  # 本地开发默认用 SQLite

    class Config:
        env_file = ".env"
```

然后在项目根目录创建 `.env`，这文件不要提交到 git：

```env
# backend/.env
DATABASE_URL=postgresql://finance_owner:xxxx@ep-xxx.ap-southeast-1.aws.neon.tech/finance?sslmode=require
```

本地开发的时候用 SQLite（默认值），部署到 Render 的时候用环境变量覆盖成 Neon——这是标准做法。

### 第四步：安装 PostgreSQL 驱动

SQLAlchemy 连接 PostgreSQL 需要 `psycopg2` 驱动。在 `requirements.txt` 里加上：

```txt
# backend/requirements.txt — 新增
psycopg2-binary>=2.9.0
```

然后安装：

```bash
cd backend
.\venv\Scripts\pip.exe install psycopg2-binary
```

重启后端：

```bash
.\venv\Scripts\uvicorn.exe app.main:app --reload --port 8001
```

SQLAlchemy 会在启动时自动检测到 `postgresql://` 开头的连接字符串，切换成 PostgreSQL 模式——建表、查询、所有 ORM 操作自动适配。你不需要改任何业务代码。

### 第五步：验证

启动后端后，调一下注册接口：

```bash
curl -X POST http://localhost:8001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

返回注册成功之后，去 Neon 的 SQL Editor 执行：

```sql
SELECT * FROM users;
```

看到你刚注册的用户数据——恭喜，金橘记账的数据库已经从你电脑上的 SQLite 文件变成了云端的 PostgreSQL。

### 第六步：部署到 Render 时配环境变量

在 Render 的 Environment 页，把本地 `.env` 里的值搬过去：

| Key | Value |
|-----|-------|
| `DATABASE_URL` | `postgresql://...`（Neon 连接字符串） |
| `JWT_SECRET` | 你的生产密钥 |

提交代码、推到 GitHub，Render 会自动重新部署。部署完成后打开线上版本，注册一个用户、记一笔账、然后去 Neon 的 SQL Editor 查一下——数据在云上，Render 重启也不会丢了。

## ⚠️ 常见坑点

| 坑 | 现象 | 解法 |
|----|------|------|
| **忘了加 `?sslmode=require`** | 后端启动报错：`connection refused` 或 SSL 相关错误 | Neon 强制 SSL 加密连接，URL 末尾必须加 `?sslmode=require`。少这一个参数就连不上 |
| **SQLite 的 `like()` 用在 Date 列** | PG 报错：`operator does not exist: date ~~ unknown`，意思是 Date 类型不支持 LIKE | 用 `cast(Transaction.tx_date, String).like(...)` 把 Date 先转成字符串。参看上文第二处代码修改 |
| **`check_same_thread` 参数报错** | `TypeError: 'check_same_thread' is an invalid keyword argument` | PostgreSQL 不需要这个参数，只在 SQLite 连接时传。database.py 里加条件判断：`if "sqlite" in url` |
| **免费版连接数限制** | 多人同时访问时部分请求报错 `too many connections` | Neon 免费版限制 1 个数据库最多 5 个同时连接。个人项目够用，但如果你的应用并发很高，用 SQLAlchemy 连接池控制：`pool_size=3, max_overflow=2` |
| **自动休眠** | 长时间不访问，第一个请求要等 3-5 秒 | Neon 免费版也有休眠机制（和 Render 类似），用 UptimeRobot 或 Render 的 `/api/health` 的副作用去"唤醒"它 |
| **amount 字段从 int 变成了 bigint** | 迁移时如果用了 `Integer` 类型，PG 默认创建 `INTEGER`（4 字节），金额存分够用但要注意范围 | 金橘记账的 Transaction 模型已经用了 `mapped_column(Integer)`，PG 上对应 `INTEGER` 类型，范围 ±21 亿（21 亿分 = 2.1 亿元），完全够用 |

## 🎯 面试会问

**Q1：SQLite 和 PostgreSQL 有什么区别？什么时候用哪个？**

**答：**

我一般分三个层面来回答这个问题。

第一个层面是本质区别：SQLite 是一个嵌入式数据库引擎，编译进你的应用进程里，数据存在一个文件里。PostgreSQL 是一个独立的数据库服务器进程，你的应用通过 TCP 连接跟它通信。

第二个层面是实际表现：SQLite 并发写能力弱（同时只有一个进程能写），PostgreSQL 支持高并发读写。SQLite 没有用户权限系统，PostgreSQL 有完整的角色和权限管理。SQLite 的类型系统很宽松（你可以把字符串存进 Integer 列），PostgreSQL 类型检查严格。

第三个层面是选型建议：本地开发、移动端 App（每个用户有自己的 SQLite 文件）、单机小工具、嵌入式设备——用 SQLite。任何 Web 应用、多用户系统、生产环境——用 PostgreSQL。

面试如果追问："那能不能在生产环境用 SQLite？" 可以这样答：技术上可以，如果你只有几十个用户、没有并发写需求，SQLite 完全撑得住。SQLite 官方的说法是：每天 10 万次请求以下的网站，SQLite 不一定比客户端-服务器数据库慢。但问题是——大部分团队选择在开发环境用 SQLite、生产环境用 PostgreSQL，这不是性能问题，是工程实践问题。生产数据库需要备份、监控、主从复制，SQLite 只有一个文件，这些运维操作不好做。

**Q2：什么是 Serverless 数据库？跟传统数据库有什么区别？**

**答：**

Serverless 数据库（比如 Neon、PlanetScale、AWS Aurora Serverless）的核心特点是：**你不需要管理数据库服务器**。

传统数据库：你需要选实例规格（CPU、内存、存储）、配置备份策略、升级大版本、监控磁盘 IO——即使你只是一个人开发者。就跟以前部署前端需要自己买服务器、装 Nginx 一样。

Serverless 数据库：你创建一个项目、拿一条连接字符串、往代码里一贴。平台负责自动扩缩容、自动备份、自动升级。就跟 Vercel 部署前端一样——你没见过 Vercel 的服务器长什么样子，但你的网站就在线上跑着。

Neon 更进一步——你可以像 Git 一样创建数据库的"分支"。比如你要测试一个新的 schema 迁移，可以先从主数据库拉一个分支出来，在这个分支上做修改、测试，确认没问题了再合并回主数据库。这个功能传统数据库需要自己搭一套 staging 环境才能做到。

缺点：Serverless 数据库通常有"冷启动"问题——没人访问的时候进入休眠，第一个请求会有几秒延迟。免费版也都有存储、连接数、计算时间的限制。但对个人项目来说，免费的比没有强一万倍。

**Q3：连接字符串里的 `sslmode=require` 是干嘛的？**

**答：**

`sslmode=require` 的意思是：客户端和数据库之间的通信必须走 SSL 加密通道。

不加密的情况：你发的 SQL 语句、数据库返回的数据都在网络传输中**明文可见**。如果有人在网络中间截获数据包，直接能看到你数据库里的内容——跟 HTTP 一样不安全。

加了 SSL：数据传输过程加密，跟 HTTPS 一个道理。你在咖啡店公共 WiFi 下用金橘记账，没人能看到你的财务数据。

Neon 强制开启 SSL——这是一件好事。生产环境的数据库不加密传输，就像把你的银行卡密码写在明信片上寄出去。

## 📝 小结

这篇文章的核心就三件事：

1. **SQLite 的数据库文件在 Render 重启时会丢失**——不是 bug，是 Render 免费版的设计。就像 localStorage 清缓存就没了，你需要一个真正的云数据库。
2. **Neon 是数据库界的 Vercel**——免费、注册就有一个 PostgreSQL 实例、拿连接字符串往代码里一贴就行。0.5GB 存储，个人项目够用几年。
3. **代码改动很小**——改连接字符串 + `cast` 一下 Date 字段 + 去掉 `check_same_thread` + 装一个 `psycopg2`。SQLAlchemy 帮你扛掉了 90% 的迁移工作。

金橘记账现在后端跑在 Render 上，数据库跑在 Neon 上——前端 Vercel、后端 Render、数据库 Neon，全是免费的，而且重启不丢数据。这曾经是每个月要花几百块的架构，现在一个前端开发者不花一分钱就搭起来了。

下一篇是 **第7篇：常见坑点 — 部署踩过的 10 个坑**。前端部署、后端部署、数据库迁移、域名绑定——我把每步踩过的坑汇成一张表格，让你部署的时候少走我走过的弯路。

—— 阿珊，前端开发者 & 全栈学习者

## 🎨 本文配图提示词

**封面图（16:9）：**

```
An illustration showing SQLite vs PostgreSQL contrast. Left side: a white SQLite database file icon sitting on a Render cloud that's visibly crumbling and fading away, with confused frontend developer looking at empty data screen. Right side: a glowing blue PostgreSQL elephant logo (Neon branding) on a solid cloud that stays permanent, data rows floating proudly around it, the same developer now smiling. A central arrow from left to right labeled "sqlite:/// → postgresql://". Below, a small badge "0.5GB Free Forever". Warm amber (#F59E0B) accent colors, clean flat illustration style, tech article cover feel. --ar 16:9 --style digital --v 6
```

**文中配图（4:3）：**

```
A flow illustration of "SQLite to PostgreSQL Migration - 3 Things to Change". Three cards arranged horizontally, each with a code snippet and title: Card 1 "Connection String" showing sqlite:/// transforming to postgresql:// with a Neon logo, Card 2 ".like() on Date Column" showing before/after code with cast(String) highlighted, Card 3 "Remove check_same_thread" with a strikethrough. Below the cards, a big checkmark "SQLAlchemy handles the rest - 0 lines of business logic changed". Clean tech diagram style, warm background (#FFF8F0), code editor color scheme. --ar 4:3 --style digital --v 6
```

**中文版（DALL-E 可用）：**

```
一张SQLite vs PostgreSQL的对比插画。左边是一个白色的SQLite数据库文件图标，放在正在崩塌消失的Render云上，一个困惑的开发者看着空无一物的数据页面。右边是发光的蓝色PostgreSQL大象logo（Neon品牌风格），稳稳放在一朵持久存在的云上，数据行漂浮在周围，同一个开发者露出了笑容。中间一个大箭头标注"sqlite:/// → postgresql://"。下方一个小徽章写"0.5GB 永久免费"。暖色调琥珀色点缀，干净扁平插画风格，适合技术专栏封面。
```
