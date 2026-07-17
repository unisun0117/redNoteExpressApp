# FastAPI 从0到1 第3篇：请求体怎么定义？认识 Pydantic

@[TOC](目录)

## 摘要
> 本文深入讲解 FastAPI 请求体的定义方式，带你认识 Pydantic 这个数据校验利器。适合刚学会路由但还不会处理复杂 JSON 请求的前端转后端开发者，学完你就能写出带数据验证的接口。

## 引言

前端同学一定写过不少表单校验代码：`if (!email.includes('@')) { alert('邮箱格式不对') }`。这种校验逻辑散落在各个组件里，改起来很痛苦。有没有一种方式，把数据格式定义在一个地方，后端和前端共享规则？

这就是 Pydantic 的价值所在。它是 FastAPI 的"亲兄弟"——FastAPI 的所有数据校验、序列化、API 文档自动生成，底层全靠 Pydantic 撑腰。理解了 Pydantic，你就理解了 FastAPI 数据处理的半壁江山。

这篇文章会带你从 TypeScript 的 interface 视角来理解 Pydantic，你会发现它和你熟悉的东西其实非常像。

## 基础知识储备

- **JSON 数据类型**：string、number、boolean、object、array，这些对应 Python 的 str、int、float、bool、dict、list。
- **TypeScript interface 对比**：如果你写过 `interface User { name: string; age: number }`，Pydantic 做的事情一模一样，只是语法是 Python 的。
- **数据校验**：确保客户端发来的数据符合预期——类型正确、字段齐全、值合法。
- **序列化与反序列化**：Python 对象转 JSON（序列化），JSON 转 Python 对象（反序列化），Pydantic 一键搞定。

## 正文

### Pydantic 就是后端的 TypeScript interface

如果你用 TypeScript 是这样定义数据的：

```typescript
interface CreateUserRequest {
    name: string;
    age: number;
    email: string;
}
```

那在 FastAPI 里用 Pydantic 写出来就是：

```python
from pydantic import BaseModel

class CreateUserRequest(BaseModel):
    name: str
    age: int
    email: str
```

是不是一模一样的思路？只是把 `interface` 换成了 `class(BaseModel)`，把 `string` 换成了 `str`。连字段名和类型声明都是同一个写法。

### 用 Pydantic 模型接收请求体

```python
from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()

class CreateUserRequest(BaseModel):
    name: str
    age: int
    email: EmailStr  # EmailStr 会自动校验邮箱格式

@app.post("/api/users")
def create_user(user: CreateUserRequest):
    return {"id": 1, "name": user.name, "email": user.email}
```

前端调用时传一个 JSON body，FastAPI 会自动把它解析成 `CreateUserRequest` 对象。如果数据不对——比如 age 传了字符串 `"二十五"`——FastAPI 会自动返回 422 错误，告诉你哪个字段有问题。你一行校验代码都不用写。

### 更高级的校验：字段约束

```python
from pydantic import BaseModel, Field

class Product(BaseModel):
    name: str = Field(..., min_length=1, max_length=100, description="商品名称")
    price: float = Field(..., gt=0, description="价格，必须大于0")
    stock: int = Field(default=0, ge=0, description="库存，不能为负")
    tags: list[str] = Field(default_factory=list, description="标签列表")
```

几个要点：
- `Field(...)` 里的三个点 `...` 表示**必填字段**，和 TypeScript 的非可选属性一样。
- `default=0` 表示默认值，和 ES6 的函数默认参数一样。
- `gt`（greater than）、`ge`（greater or equal）、`lt`、`le` 做数值范围约束。
- `min_length`、`max_length` 做字符串长度约束。

### 嵌套模型：复杂 JSON 也不怕

```python
class Address(BaseModel):
    city: str
    street: str
    zip_code: str

class User(BaseModel):
    name: str
    age: int
    address: Address  # 嵌套模型！
    roles: list[str] = []
```

前端可以传这样的 JSON：

```json
{
    "name": "小明",
    "age": 25,
    "address": {
        "city": "深圳",
        "street": "科技园路100号",
        "zip_code": "518000"
    },
    "roles": ["admin", "editor"]
}
```

这和 TypeScript 里 `interface User { address: Address }` 的嵌套定义思路完全一致。任意深度的 JSON 结构都可以用 Pydantic 的嵌套模型来表达。

### 响应模型：控制输出

```python
class UserResponse(BaseModel):
    id: int
    name: str
    email: str

@app.get("/api/users/{user_id}", response_model=UserResponse)
def get_user(user_id: int):
    # 假设从数据库拿到 user，包含 password_hash 等敏感字段
    user = {"id": user_id, "name": "小明", "email": "xm@test.com", "password_hash": "xxx"}
    return user  # response_model 会自动过滤掉 password_hash！
```

`response_model` 有三个妙用：过滤多余字段、自动转换数据类型、生成准确的 API 文档。这个功能是你做后端越久越觉得香的东西。

## 总结

1. Pydantic 的 `BaseModel` 等同于 TypeScript 的 `interface`，定义数据结构的思路完全一样。
2. `Field()` 提供了丰富的校验约束：必填、默认值、数值范围、字符串长度、正则匹配等。
3. 嵌套模型让复杂 JSON 结构得到清晰表达，任意深度都没问题。
4. `response_model` 自动过滤敏感字段、生成精准文档，是生产环境必备技能。
5. 所有校验逻辑集中在模型定义里，后端路由函数只管业务逻辑——这是 FastAPI 优雅设计的核心。

## 注意事项

1. `EmailStr` 需要额外安装 `pip install pydantic[email]`，否则运行时会报错。
2. 请求体和查询参数可以混用——函数参数里 Pydantic 类的自动当请求体解析，基础类型的自动当查询参数解析。
3. 不要用 `dict` 来接收请求体，除非你真的要接收任意 JSON。有明确结构就写 Pydantic 模型，这是好习惯。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 用了旧版 dict() 方法 | `user.dict()` 报 DeprecationWarning | Pydantic v2 把 `dict()` 改成了 `model_dump()` | 统一用 `user.model_dump()` 替代 `user.dict()` |
| Optional 没用对 | `name: str = None` 类型检查报错 | 需要显式声明 `Optional[str]` | 写 `from typing import Optional` 然后 `name: Optional[str] = None` |
| 嵌套模型的默认值陷阱 | 所有请求的 tags 都是同一个 list 实例 | 可变默认值 `tags: list = []` 是 Python 经典坑 | 用 `default_factory=list` 替代默认值写法 |
| EmailStr 校验没生效 | 邮箱乱填也能通过 | 没安装 pydantic 的 email 扩展 | `pip install pydantic[email]` |
