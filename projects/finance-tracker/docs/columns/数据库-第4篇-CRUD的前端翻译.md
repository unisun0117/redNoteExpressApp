# 第4篇：CRUD的前端翻译 — 增删改查就是你每天写的代码

> array.push = INSERT, array.filter = SELECT, obj.xxx = yyy = UPDATE, array.splice = DELETE。CRUD没有新东西，你写前端的时候每天都在做，只是换了个语法。

## 📌 前端为什么要学这个？

我第一次学SQL的时候，看着教程上大写字母的 `SELECT`、`FROM`、`WHERE`，感觉像在读法律文件。什么"投影"、"选择"、"聚合"，每个词都认识，连在一起就是天书。

那段时间我特别抗拒SQL——我一个前端，写写 React 不好吗？为什么非要去背这些看起来像上世纪70年代发明的语法？

转折发生在一个加班写报表的晚上。

产品经理要我出一张"本月餐饮消费超过500元的用户列表"。我习惯性地打开 Chrome DevTools，准备写一大段 JavaScript 来处理后端返回的数据。写了一半突然卡住了——后端接口根本不支持这个筛选条件，它只返回所有数据。

我在那个瞬间突然想：如果能把 JavaScript 的数组操作翻译成SQL，是不是就不用改后端代码了？

我打开数据库客户端，试着把我脑子里想好的 JavaScript 逻辑一句一句"翻译"成 SQL。神奇的事情发生了——30秒的查询，一行 SQL 就出来了，而且运行只需要 50 毫秒。

从那之后，SQL再也不是天书了。**它就是我每天都在写的数组操作，只是换了个语法。**

## 🔍 核心原理

### CRUD 就是四条数组操作

你写前端的时候，每天都在做四件事：往列表里加数据、从列表里查数据、改某一条数据的值、删掉某一条数据。这四件事在数据库的世界里有一个缩写——**CRUD**。

| 操作 | 中文 | SQL 关键字 | JavaScript 等价操作 | 你每天在哪写 |
|------|------|-----------|-------------------|------------|
| **C**reate | 增 | `INSERT` | `array.push(item)` | 用户注册、发表评论、新建账单 |
| **R**ead | 查 | `SELECT` | `array.filter(item => ...)` | 列表页、搜索框、下拉筛选 |
| **U**pdate | 改 | `UPDATE` | `obj.name = "新值"` | 编辑资料、修改昵称、标记已读 |
| **D**elete | 删 | `DELETE` | `array.splice(idx, 1)` | 删除评论、取消订单、注销账户 |

说白了，**CRUD 就是你每天都在写的增删改查，只是代码运行的地方从浏览器变成了服务器，操作的对象从前端数组变成了数据库表。**

下面我逐个对照，保证你看完就能直接把 JavaScript 翻译成 SQL。

### 1. CREATE — 往数组里 push 一条记录

```javascript
// 前端：往列表里加一条
const bills = [];
const newBill = { type: "expense", amount: 3500, category: "餐饮", note: "午餐", date: "2025-06-29" };
bills.push(newBill);
// bills.length 变成了 1
```

等价的 SQL：

```sql
INSERT INTO transactions (type, amount, category, note, tx_date)
VALUES ('expense', 3500, '餐饮', '午餐', '2025-06-29');
```

同一个意思，只是写法不同。`push` 不需要你指定"数组名"，因为你在代码里已经写了 `bills.push`。SQL 需要你显式写出 `INSERT INTO transactions`，因为数据库里可能有几百张表，它不知道你要插到哪一张。

金橘记账的真实后端代码是这样的：

```python
# backend/app/routers/transactions.py — CREATE
tx = Transaction(
    user_id=user.id,
    type=req.type,
    amount=req.amount,
    category=req.category,
    note=req.note,
    tx_date=req.tx_date,
)
db.add(tx)      # 等价于 bills.push(newBill)
db.commit()     # 等价于"保存文件"——不commit数据不会真正写入
db.refresh(tx)  # 刷新拿到数据库生成的id和创建时间
```

### 2. READ — 用 filter 查数据

```javascript
// 前端：查本月餐饮支出
const diningThisMonth = bills.filter(item =>
    item.type === "expense" &&
    item.category === "餐饮" &&
    item.date.startsWith("2025-06")
);
console.log(diningThisMonth);  // [{...}, {...}]
```

等价的 SQL：

```sql
SELECT * FROM transactions
WHERE type = 'expense'
  AND category = '餐饮'
  AND tx_date LIKE '2025-06%';
```

看到了吗？`filter` 的回调函数里的每个条件，对应的就是 SQL 里 `WHERE` 后面的每个 `AND`。`item.date.startsWith("2025-06")` 就是 `tx_date LIKE '2025-06%'`。

金橘记账的真实查询代码（带分页、排序）：

```python
# backend/app/routers/transactions.py — READ（列表+筛选+分页）
query = db.query(Transaction).filter(Transaction.user_id == user.id)

if month:
    query = query.filter(Transaction.tx_date.like(f"{month}%"))
if category:
    query = query.filter(Transaction.category == category)
if type:
    query = query.filter(Transaction.type == type)

items = (
    query.order_by(Transaction.tx_date.desc())
    .offset((page - 1) * page_size)
    .limit(page_size)
    .all()
)
```

翻译成 JavaScript 就是：

```javascript
// 等价前端逻辑
let filtered = bills;

if (month) {
    filtered = filtered.filter(item => item.date.startsWith(month));
}
if (category) {
    filtered = filtered.filter(item => item.category === category);
}
if (type) {
    filtered = filtered.filter(item => item.type === type);
}

const page = 1, pageSize = 50;
const result = filtered
    .sort((a, b) => b.date.localeCompare(a.date))  // ORDER BY tx_date DESC
    .slice((page - 1) * pageSize, page * pageSize); // LIMIT + OFFSET
```

### 3. UPDATE — 修改某个对象的值

```javascript
// 前端：修改第3条记录
if (bills.length >= 3) {
    bills[2].amount = 5000;
    bills[2].note = "编辑后的备注";
}
```

等价的 SQL：

```sql
UPDATE transactions
SET amount = 5000, note = '编辑后的备注'
WHERE id = 'abc-123';
```

注意一个关键区别：JavaScript 里你直接改数组某个位置的值，SQL 里你需要用 `WHERE` 精确指定改哪一条。**如果你忘了写 WHERE，SQL 会把整张表的所有记录都改了** ——这个坑我们后面会讲。

金橘记账的真实更新代码：

```python
# backend/app/routers/transactions.py — UPDATE
tx = db.query(Transaction).filter(
    Transaction.id == tx_id,
    Transaction.user_id == user.id  # 安全校验：只能改自己的
).first()
if not tx:
    raise HTTPException(status_code=404, detail="Transaction not found")

tx.amount = req.amount
tx.note = req.note
tx.category = req.category
db.commit()  # 提交修改
```

### 4. DELETE — 从数组里删掉一条

```javascript
// 前端：删除id匹配的那条
const idx = bills.findIndex(item => item.id === "abc-123");
if (idx !== -1) {
    bills.splice(idx, 1);
}

// 或者用 filter 生成一个新数组（更React的方式）
const newBills = bills.filter(item => item.id !== "abc-123");
```

等价的 SQL：

```sql
DELETE FROM transactions
WHERE id = 'abc-123';
```

`splice` 根据位置删除，SQL 的 `DELETE` 根据条件删除。用 `filter` 的方式跟我理解更接近——"保留所有 id 不等于 xxx 的记录"。

金橘记账的真实删除代码：

```python
# backend/app/routers/transactions.py — DELETE
tx = db.query(Transaction).filter(
    Transaction.id == tx_id,
    Transaction.user_id == user.id
).first()
if not tx:
    raise HTTPException(status_code=404, detail="Transaction not found")
db.delete(tx)
db.commit()
```

### 一张总览对照表

| 操作 | SQL（原生） | JavaScript（数组操作） | 金橘记账 ORM（Python） |
|------|-----------|----------------------|---------------------|
| 增 | `INSERT INTO tx VALUES (...)` | `arr.push({...})` | `db.add(Tx(...))` + `db.commit()` |
| 查 | `SELECT * FROM tx WHERE ...` | `arr.filter(item => ...)` | `db.query(Tx).filter(...).all()` |
| 改 | `UPDATE tx SET ... WHERE id=?` | `arr[i].field = newVal` | `tx.field = val` + `db.commit()` |
| 删 | `DELETE FROM tx WHERE id=?` | `arr.splice(idx, 1)` | `db.delete(tx)` + `db.commit()` |

> 阿珊说：学SQL最快的办法不是背语法，而是把你脑子里想好的JavaScript数组操作一句一句翻译过去。你会写filter就会写WHERE，你会push就会INSERT，你早就会CRUD了，你只是不知道它叫CRUD。

## 🛠 动手试试

打开金橘记账项目的数据库，跟我一起做：

### 准备：连接数据库

```bash
# 进到金橘记账项目目录
cd E:\AI_Workspace\projects\finance-tracker\backend

# 用 SQLite 命令行打开数据库
sqlite3 finance.db
```

进去之后你会看到一个 `sqlite>` 提示符。先看看有哪些表：

```sql
.tables
-- 输出：budgets  transactions  users
```

### 第一步：SELECT = filter，查你本月的支出

```sql
-- 查所有餐饮支出（等价于 bills.filter(item => item.category === '餐饮')）
SELECT * FROM transactions WHERE category = '餐饮';

-- 查本月所有支出，按日期倒序（等价于 filtered.sort().slice()）
SELECT type, amount, category, note, tx_date
FROM transactions
WHERE tx_date LIKE '2025-06%' AND type = 'expense'
ORDER BY tx_date DESC;

-- 查花了多少钱（等价于 filtered.reduce((sum, item) => sum + item.amount, 0)）
SELECT SUM(amount) FROM transactions WHERE type = 'expense';
```

### 第二步：INSERT = push，记一笔账

```sql
-- 等价于 bills.push({...})
INSERT INTO transactions (user_id, type, amount, category, note, tx_date)
VALUES ('your-user-id', 'expense', 3500, '餐饮', '午餐', '2025-06-29');

-- 验证：查一下刚才插入的
SELECT * FROM transactions WHERE note = '午餐';
```

### 第三步：UPDATE = 修改对象属性

```sql
-- 把刚才那笔金额改成 5200（等价于 bill.amount = 5200）
UPDATE transactions
SET amount = 5200, note = '午餐（改价）'
WHERE note = '午餐';

-- 验证
SELECT * FROM transactions WHERE note LIKE '%午餐%';
```

### 第四步：DELETE = splice / filter

```sql
-- 删掉刚才的测试数据（等价于 bills.splice(idx, 1)）
DELETE FROM transactions WHERE note LIKE '%午餐%';

-- 验证
SELECT * FROM transactions WHERE note LIKE '%午餐%';
-- 应该返回空
```

### 前端是怎么调这些接口的？

打开浏览器 DevTools 的 Network 面板，在金橘记账 App 里操作一下，你会看到：

```typescript
// frontend/src/services/api.ts — 四个API调用，完美对应CRUD

// 记一笔（CREATE）→ POST /transactions
api.createTransaction(data)
// → authFetch('/transactions', { method:'POST', body: JSON.stringify(data) })

// 拉账单列表（READ）→ GET /transactions?month=2025-06
api.getTransactions(params)
// → authFetch('/transactions?qs').then(r => r.json())

// 改一笔（UPDATE）→ PUT /transactions/:id
api.updateTransaction(id, data)
// → authFetch('/transactions/'+id, { method:'PUT', body: JSON.stringify(data) })

// 删一笔（DELETE）→ DELETE /transactions/:id
api.deleteTransaction(id)
// → authFetch('/transactions/'+id, { method:'DELETE' })
```

注意这 4 个方法跟 HTTP 方法的完美对应：POST = 增，GET = 查，PUT/PATCH = 改，DELETE = 删。**RESTful API 的设计思路，就是从 CRUD 来的。**

## ⚠️ 常见坑点

| 坑 | 现象 | 前端类比 | 解法 |
|----|------|---------|------|
| **UPDATE 忘写 WHERE** | 所有行的 amount 都变成 5200 了 | `bills.forEach(b => b.amount = 5200)` 而不是 `bills[2].amount = 5200` | 写 UPDATE 时先把 WHERE 写好，再填 SET 的内容 |
| **DELETE 忘写 WHERE** | 整张表被清空 | `bills.length = 0` 而不是 `bills.splice(idx, 1)` | 同样，DELETE 语句先写 WHERE 条件。生产环境建议养成先 SELECT 确认、再 DELETE 的习惯 |
| **数字字符串不匹配** | `WHERE amount = "3500"` 查不出来 | `bills.filter(item => item.amount === "3500")` vs `=== 3500`，类型不匹配 | 数据库里 amount 是 INTEGER，WHERE 条件里不要加引号。跟 JavaScript 的 `===` 一样严格 |
| **commit 忘写** | 数据"存了"但刷新就没了 | 类似你 `setState` 了但忘了调接口保存到后端 | SQLAlchemy 里 `db.add()` 只是标记了"待保存"，`db.commit()` 才真正写入磁盘。没 commit = 没存 |
| **前端用了 DELETE 但接口没做权限** | 用户A可以删用户B的账单 | 你把别人的 DOM 元素删了 | 后端必须校验 `user_id`：`filter(id == tx_id AND user_id == current_user.id)` |

> 阿珊说：UPDATE 和 DELETE 忘写 WHERE 的痛苦，跟你 `git push --force` 到 master 的体验差不多——一瞬间的冲动，两小时的抢救。任何时候写改和删的操作，先写条件，再写内容。

## 🎯 面试会问

**Q1：请用一句话解释什么是CRUD？**

**答：** CRUD 就是增删改查的英文缩写——Create（增）、Read（查）、Update（改）、Delete（删）。这四种操作覆盖了 90% 的业务需求。你可以这么理解：用户在页面上做的所有操作，最终都会落在数据库的 CRUD 里——注册是 INSERT，浏览列表是 SELECT，修改资料是 UPDATE，注销账户是 DELETE。面试官听你讲出"前端操作最终落在数据库CRUD"这个理解，会比单纯背定义加分很多。

**Q2：HTTP 方法和 CRUD 是怎么对应的？为什么要这样设计？**

**答：**
- GET → READ（查）
- POST → CREATE（增）
- PUT/PATCH → UPDATE（改）
- DELETE → DELETE（删）

这样设计的好处是**语义化**。你不用在 URL 里写 `/createUser`、`/deleteUser`、`/updateUser`，统一用 `/users` 这个资源地址，通过 HTTP 方法区分的操作类型。这就是 RESTful API 的核心思想——URL 定位资源，HTTP 方法描述操作。

实际项目中我写金橘记账的时候，四个接口就是这样的：
```
GET    /api/transactions       → 查列表
POST   /api/transactions       → 记一笔
PUT    /api/transactions/:id   → 改一笔
DELETE /api/transactions/:id   → 删一笔
```

四个 URL，对应四个数据库操作，前端用 `authFetch` 统一封装，一目了然。

**Q3：前端数组操作和数据库 CRUD 最大的区别是什么？**

**答：** 最大区别有两个。一是**持久化**：JavaScript 数组刷新页面就没了，数据库的数据写到磁盘上，服务器重启也不会丢。二是**并发**：前端数组只有你一个人在操作，数据库同时可能有成百上千个用户在读写。所以数据库多了事务、锁、权限这些机制——等你学到第7篇就会知道，这些全是为了解决"很多人同时操作同一份数据"的问题。

## 📝 小结

这篇文章的核心就一句话：**CRUD 不是新知识，是你已经会的 JavaScript 数组操作换了种写法。**

- `INSERT` = `array.push()`
- `SELECT` = `array.filter()`
- `UPDATE` = `obj.field = newValue`
- `DELETE` = `array.splice()` 或 `array.filter()`

理解了这层对应关系，SQL 就不再是"天书"了——它只是你每天写的前端逻辑在数据库里的表达方式。

下一篇是专栏的重头戏——**第5篇：三张表教会你数据库设计**。我会用金橘记账的真实三张表（User、Transaction、Budget），带你学会主键、外键、一对多关系——数据库设计最重要的三个概念，一次讲透。

—— 阿珊，前端开发者 & 全栈学习者

## 🎨 本文配图提示词

**封面图（16:9）：**

```
A translation comparison illustration: "JavaScript vs SQL — CRUD Operations". Left side shows JavaScript code blocks (array.push, array.filter, arr[i].xxx = yyy, array.splice) in a code editor style with dark background. Right side shows equivalent SQL statements (INSERT INTO, SELECT FROM WHERE, UPDATE SET WHERE, DELETE FROM WHERE) in another code panel. Four rows connected by glowing arrows showing the mapping: push→INSERT, filter→SELECT, assignment→UPDATE, splice→DELETE. Title at top: "You Already Know CRUD". Warm color palette with amber (#F59E0B) accents, flat illustration style, clean tech article cover. --ar 16:9 --style digital --v 6
```

**文中配图（4:3）：**

```
A side-by-side code comparison illustration. Left panel labeled "Frontend (JavaScript)" showing: const dining = bills.filter(item => item.category === '餐饮'). Right panel labeled "Database (SQL)" showing: SELECT * FROM transactions WHERE category = '餐饮'. The two panels connected by an equals sign in the middle, emphasizing they do the same thing. A small developer character looking enlightened between them. Warm background (#FFF8F0), code in dark panels, amber highlights for keywords. --ar 4:3 --style digital --v 6
```

**中文版（DALL-E 可用）：**

```
一张"JavaScript vs SQL"的对照翻译插画。左边是JavaScript代码块（array.push、array.filter、array.splice），右边是等价的SQL语句（INSERT INTO、SELECT WHERE、DELETE FROM），中间用箭头连接。每一行展示一对CRUD操作的对应关系，共四行。标题："你早就会CRUD了"。暖色调，代码编辑器风格配色，扁平插画风格，适合技术专栏封面。
```
