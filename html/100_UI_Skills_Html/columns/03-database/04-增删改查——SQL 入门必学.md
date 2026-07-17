# 数据库设计从0到1 第4篇：增删改查——SQL 入门必学
@[TOC](目录)
## 摘要
> CRUD是数据库操作的灵魂。本篇覆盖INSERT、SELECT、UPDATE、DELETE四大核心命令，结合Python可运行代码，用前端数组操作类比让你轻松掌握SQL数据操作。

## 引言

前端开发中，你对数据的操作无非四种：创建（push）、读取（map/filter/find）、更新（修改数组元素）、删除（splice/filter）。这正是大名鼎鼎的CRUD——Create、Read、Update、Delete。

```javascript
// 前端数组版CRUD
const items = [];
items.push(newItem);                        // CREATE
const found = items.find(i => i.id === 1);  // READ
items[0].name = 'newName';                  // UPDATE
items.splice(0, 1);                        // DELETE
```

SQL也有完全对应的四条命令，而且更加精炼强大。每条命令都像一个声明——你告诉数据库"我要什么"，而不是"怎么做"。这正是SQL的魅力所在。

本文会让你用起INSERT、SELECT、UPDATE、DELETE像用数组方法一样自然。还会介绍WHERE过滤、ORDER BY排序、LIMIT分页——这些你每天都在前端做的事，在SQL里怎么写。

## 基础知识储备

1. **熟悉JavaScript数组操作**：push、find、filter、map、splice——本文会不断用这些做类比。
2. **理解JSON对象结构**：`{id: 1, name: 'Alice'}` 对应数据库的一条记录。
3. **上一篇的表关系知识**：知道外键是什么，JOIN的基本概念。
4. **Python + SQLite基础**：能用`sqlite3`连接数据库、执行SQL。

## 正文

### 一、CREATE（插入数据）—— 数组的push，但更强大

最基本的插入一行：

```sql
INSERT INTO users (username, email, age)
VALUES ('Alice', 'alice@example.com', 25);
```

一次插入多行（JS里要循环push，SQL一步搞定）：

```sql
INSERT INTO users (username, email, age) VALUES
    ('Bob', 'bob@example.com', 30),
    ('Charlie', 'charlie@example.com', 28),
    ('Diana', 'diana@example.com', 22);
```

Python完整示例：

```python
import sqlite3

conn = sqlite3.connect('crud_demo.db')
cursor = conn.cursor()

# 先建表
cursor.execute('''
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL UNIQUE,
        email TEXT NOT NULL,
        age INTEGER CHECK(age > 0)
    )
''')

# 插入数据
users_data = [
    ('Alice', 'alice@example.com', 25),
    ('Bob', 'bob@example.com', 30),
    ('Charlie', 'charlie@example.com', 28),
]
cursor.executemany(
    'INSERT INTO users (username, email, age) VALUES (?, ?, ?)',
    users_data
)

conn.commit()
print(f"插入了{cursor.rowcount}条数据（executemany返回的是最后一条的rowcount，实际插入了{len(users_data)}条）")
```

注意`?`占位符——这是防止SQL注入的关键，永远不要用字符串拼接。

### 二、SELECT（查询数据）—— filter + map + sort 合一

SELECT是SQL中最复杂也最灵活的命令。一条SELECT语句可以同时完成前端需要多步操作才能做到的事。

**基础查询：**

```sql
SELECT * FROM users;                              -- 查所有列
SELECT username, email FROM users;                 -- 只查指定列（类似map选字段）
SELECT * FROM users WHERE age > 25;                -- 条件过滤（类似filter）
SELECT * FROM users ORDER BY age DESC;             -- 排序（类似sort）
SELECT * FROM users LIMIT 10 OFFSET 0;             -- 分页（类似slice）
```

**组合使用——一条SQL完成过滤+排序+分页+选列：**

```sql
SELECT username, email, age
FROM users
WHERE age >= 20 AND age <= 30
ORDER BY age DESC
LIMIT 5 OFFSET 0;
```

前端要做同样的事，代码长得多：

```javascript
// 同样的逻辑，前端要做4步
users
  .filter(u => u.age >= 20 && u.age <= 30)  // WHERE
  .sort((a, b) => b.age - a.age)             // ORDER BY
  .slice(0, 5)                                // LIMIT + OFFSET
  .map(u => ({ username: u.username, email: u.email, age: u.age })); // SELECT 列
```

**条件查询运算符：**

| SQL运算符 | 含义 | JS等价 |
|-----------|------|--------|
| `=` | 等于 | `===` |
| `!=` 或 `<>` | 不等于 | `!==` |
| `>` `<` `>=` `<=` | 大小比较 | 同JS |
| `BETWEEN x AND y` | 区间 | `>= x && <= y` |
| `IN (a, b, c)` | 在集合中 | `includes` |
| `LIKE '%keyword%'` | 模糊匹配 | `includes` |
| `IS NULL` / `IS NOT NULL` | 空值判断 | `=== null` / `!== null` |

### 三、UPDATE（更新数据）—— 直接改，不用先找再改

前端更新数组元素通常要先找到那个元素：

```javascript
const user = users.find(u => u.id === 1);
if (user) user.email = 'new@example.com';
```

SQL的UPDATE直接一步到位：

```sql
UPDATE users
SET email = 'new_alice@example.com', age = 26
WHERE id = 1;
```

**关键提醒：WHERE条件不能忘！** 如果不写WHERE，整张表所有行都会被更新——这相当于你遍历了整个数组把每个元素的email都改了。这种事情在生产环境是灾难级别的。

```sql
-- 危险操作！会把所有人的年龄加1
UPDATE users SET age = age + 1;

-- 安全操作，只改特定用户
UPDATE users SET age = age + 1 WHERE id = 1;
```

### 四、DELETE（删除数据）—— splice，但要更小心

```sql
DELETE FROM users WHERE id = 1;   -- 安全——删除指定记录
DELETE FROM users;                -- 危险——删除整张表的所有数据！
```

DELETE的WHERE比UPDATE的WHERE更重要——忘了写WHERE，表就空了。有一个好习惯：**写DELETE时先写WHERE，再写DELETE FROM**。

```python
# Python中安全删除的模板
def safe_delete(conn, table, condition, params):
    cursor = conn.cursor()
    # 1. 先查一下会删多少条
    cursor.execute(f"SELECT COUNT(*) FROM {table} WHERE {condition}", params)
    count = cursor.fetchone()[0]
    print(f"将删除 {count} 条记录")

    # 2. 确认后再执行
    if count > 0:
        cursor.execute(f"DELETE FROM {table} WHERE {condition}", params)
        conn.commit()
        print(f"已删除 {count} 条记录")
    return count
```

## 总结

1. SQL的CRUD四命令对应前端的数组四操作：INSERT≈push，SELECT≈filter+map，UPDATE≈直接修改元素，DELETE≈splice。
2. SELECT是最复杂的命令，一条SQL可以同时完成过滤（WHERE）、排序（ORDER BY）、分页（LIMIT）、选列（SELECT字段列表）。
3. WHERE是UPDATE和DELETE的"安全带"——忘了它就等于全表操作，生产事故排名第一。
4. 使用参数化查询（`?`占位符）而不是字符串拼接，是防止SQL注入的最基本安全实践。
5. CRUD不是数据库的全部，但它是80%日常操作的基础，务必熟练到肌肉记忆。

## 注意事项

1. `SELECT *` 在生产代码中不建议使用——它会把所有列都查出来，浪费带宽和内存。明确列出需要的列名，既是性能优化，也是代码文档。
2. LIKE模糊查询在数据量大时很慢——`LIKE '%keyword%'` 无法使用索引，因为百分号在前面。如果需要全文搜索，考虑用专用的搜索方案（如Elasticsearch或SQLite的FTS5）。
3. 频繁的单条INSERT会很慢——如果需要插入大量数据，用批量插入（`executemany` 或 `INSERT INTO ... VALUES (...), (...), (...)`），性能差距可达100倍。

## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| UPDATE/DELETE忘了WHERE | 全表数据被改或删除 | 缺少WHERE时SQL默认作用于所有行 | 养成先写WHERE再写主体的习惯；生产环境开启事务，确认前不COMMIT |
| 用字符串拼接构造SQL | 用户输入`'; DROP TABLE users; --` 删掉了整张表 | SQL注入攻击 | 始终使用参数化查询（`?`占位符），永不拼接 |
| `LIMIT 10`不加ORDER BY | 每次查出来的10条都不一样 | 没有ORDER BY时，数据库不保证返回顺序 | LIMIT必须和ORDER BY配对使用 |
| 用`=`比较NULL值 | `WHERE age = NULL`一条都查不到 | SQL中NULL不等于任何值，包括它自己 | 使用`IS NULL`和`IS NOT NULL`，不要用`= NULL` |
