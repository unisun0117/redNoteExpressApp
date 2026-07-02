# 用户认证从0到1 第8篇：权限管理：RBAC 入门

@[TOC](目录)

## 摘要
> 登录只是第一步——什么样的用户能做什么样的操作，这才是权限管理的核心。RBAC（基于角色的访问控制）是最通用的权限模型，这篇文章从概念到代码带你入门。

## 引言

前面的 7 篇文章都在解决一个问题——"你是谁"（认证/AuthN）。从本文开始，我们进入第二个核心问题——"你能干什么"（授权/AuthZ）。一个系统里通常有多种角色：管理员可以删库跑路（理论上是删文章），编辑可以写文章但不能删别人的，普通用户只能看。如何优雅地在代码中实现这些规则，而非在每一个接口里写一串 if-else？

RBAC（Role-Based Access Control）是目前最通用的权限管理模型。它的核心思想非常简单：不在用户和数据之间直接建立权限关系，而是通过"角色"这个中间层。用户被分配一个或多个角色，角色被赋予一组权限。这种"用户-角色-权限"的三层结构，让权限管理变得清晰且易于扩展——你今天只有 3 种角色，明天扩展到 10 种角色，代码改动量极小。

RBAC 的精髓在于"抽象"。不应该在 `delete_article` 函数里写 `if user.name == "张三"`——张三离职了怎么办？而是应该写 `if user.has_permission("article:delete")`——权限和具体的人解耦，只管角色有没有这个能力。

## 基础知识储备

1. **装饰器模式**：Python 的 `@decorator` 语法，可以将权限检查逻辑从业务代码中抽离出来。
2. **FastAPI 的依赖注入（Depends）**：理解 FastAPI 如何通过 `Depends` 将认证和授权逻辑注入到路由处理函数中。
3. **枚举（Enum）**：用 Python 枚举类型定义角色和权限，避免字符串拼写错误。
4. **数据库关系**：理解用户表、角色表、权限表之间的多对多关系（虽然本文用内存演示，但概念相同）。

## 正文

### RBAC 三要素：用户、角色、权限

RBAC 有三个核心实体：

**用户（User）**：系统的使用者。用户本身不代表任何权限——一个刚注册的用户什么也干不了。

**角色（Role）**：一组权限的集合。角色是你定义的——比如"admin"是一个角色，"editor"是另一个。角色的命名通常反映使用者的职责，而不是具体权限。好的命名是"内容编辑"，坏的命名是"可以写文章还可以修改文章但不能删除文章的人"。

**权限（Permission）**：对某个资源执行某个操作的能力。权限的粒度可粗可细——粗粒度是"可以管理文章"（包含增删改查），细粒度是"可以删除文章"（单一操作）。推荐的命名风格是 `resource:action`，例如 `article:create`、`article:delete`、`user:ban`。

下面是一段完整的 Python 演示代码，定义了三种角色和对应的权限矩阵：

```python
from enum import Enum
from functools import wraps
from fastapi import FastAPI, Depends, HTTPException

app = FastAPI()

# --- 权限定义 ---
class Permission(str, Enum):
    ARTICLE_READ = "article:read"
    ARTICLE_CREATE = "article:create"
    ARTICLE_UPDATE = "article:update"
    ARTICLE_DELETE = "article:delete"
    USER_MANAGE = "user:manage"

# --- 角色定义：每个角色拥有一组权限 ---
ROLE_PERMISSIONS = {
    "admin":    {Permission.ARTICLE_READ, Permission.ARTICLE_CREATE,
                 Permission.ARTICLE_UPDATE, Permission.ARTICLE_DELETE,
                 Permission.USER_MANAGE},
    "editor":   {Permission.ARTICLE_READ, Permission.ARTICLE_CREATE,
                 Permission.ARTICLE_UPDATE},
    "viewer":   {Permission.ARTICLE_READ},
}

# --- 模拟当前用户（实际项目从 JWT 中解析） ---
class User:
    def __init__(self, user_id: int, name: str, role: str):
        self.id = user_id
        self.name = name
        self.role = role

    def has_permission(self, permission: Permission) -> bool:
        allowed = ROLE_PERMISSIONS.get(self.role, set())
        return permission in allowed or Permission.ARTICLE_READ in allowed and permission == Permission.ARTICLE_READ

# 模拟从 token 解析当前用户
def get_current_user() -> User:
    return User(user_id=1, name="张三", role="editor")

# --- 权限装饰器 ---
def require_permission(permission: Permission):
    def checker(user: User = Depends(get_current_user)):
        if not user.has_permission(permission):
            raise HTTPException(
                status_code=403,
                detail=f"权限不足：需要 {permission.value}"
            )
        return user
    return checker

# --- 路由：不同操作需要不同权限 ---
@app.get("/api/articles")
def list_articles(user: User = Depends(require_permission(Permission.ARTICLE_READ))):
    return {"articles": [], "user": user.name}

@app.post("/api/articles")
def create_article(user: User = Depends(require_permission(Permission.ARTICLE_CREATE))):
    return {"message": "文章已创建", "user": user.name}

@app.delete("/api/articles/{article_id}")
def delete_article(article_id: int, user: User = Depends(require_permission(Permission.ARTICLE_DELETE))):
    return {"message": f"文章 {article_id} 已删除", "user": user.name}

@app.put("/api/admin/users/{user_id}/ban")
def ban_user(user_id: int, user: User = Depends(require_permission(Permission.USER_MANAGE))):
    return {"message": f"用户 {user_id} 已被封禁", "user": user.name}
```

### 权限系统的三层架构

上面的代码演示了 RBAC 的最核心部分，但一个完整的权限系统需要在三个层面实现：

第一层是**路由守卫**——也就是上面代码中 `require_permission` 所做的事。在请求到达业务逻辑之前，先检查用户是否有执行该操作的权限。这一层的职责是"允许或拒绝整个请求"，不关心业务细节。

第二层是**数据级权限**——也叫行级权限（Row-Level Security）。路由守卫只能控制"能不能调用删除文章的 API"，但如果要求"编辑只能删除自己写的文章，不能删除别人的"，这就需要数据级权限。实现方式通常是在数据库查询中加入条件：`DELETE FROM articles WHERE id = :id AND author_id = :current_user_id`。

第三层是**前端权限控制**——在前端 UI 层面，根据用户角色决定显示哪些菜单、按钮。常见做法是封装一个 `<Can permission="article:delete">` 组件，内部检查当前用户的权限列表，决定渲染子组件还是返回 null。但一定要记住：前端权限只是 UI 优化，真正的安全控制必须由后端实现——因为前端代码完全在用户掌控之中。

### 角色设计的常见错误和最佳实践

角色越来越多是 RBAC 系统中最常见的陷阱。新需求来了：销售要看报表 → 加一个 sales 角色；客服要回复评论 → 加一个 support 角色；实习生只能看前10条 → 加一个 intern 角色。半年后你有了 47 个角色，没人说得清它们之间的区别。

正确的做法是：坚持用"最小角色集合"原则。一开始只用 2-3 个角色，新需求来的时候先想"能不能用现有角色解决？"而不是"创造一个新角色"。如果真的需要一个新角色，问自己：这个角色的权限集合和某个现有角色的区别是否足够大？如果不是，那就合并到现有角色中。

另外，不要在代码中硬编码角色名称。永远不要写 `if user.role == "admin"`。这种判断方式让你的系统完全丧失了灵活性——如果未来 admin 改名叫 super_admin，或者增加一个 co_admin 角色拥有同样的权限，你就要满世界改代码。正确的做法是只判断权限：`if user.has_permission("article:delete")`——角色可以变，权限不变。

## 总结

1. RBAC 的核心是"用户-角色-权限"三层结构，用户不直接拥有权限，而是通过角色间接获得。
2. 权限命名推荐 `resource:action` 格式（如 `article:delete`），语义清晰且易于扩展。
3. 权限控制分为三层：路由守卫（能不能调 API）、数据级权限（能操作哪些数据）、前端权限（UI 显隐），每一层都不能少。
4. 代码中绝不硬编码角色名称，只判断用户是否拥有某个权限点——角色可以变化，权限点保持稳定。
5. 初始设计时坚持最小角色集合（2-3个），新角色必须有充分理由才能增加。

## 注意事项

1. 前端权限控制只是 UI 层的便捷优化，真正的安全防线必须在后端。攻击者可以通过 curl 直接调 API，绕开所有前端限制。
2. 权限检查失败应返回 403 Forbidden，而非 401 Unauthorized。401 表示"没登录或 token 失效"，403 表示"登录了但权限不够"。
3. 角色之间如果有包含关系（admin 包含 editor 的所有权限），代码中应该通过权限集合来实现，而非通过角色层级——避免出现"admin 也是 editor"这种隐式假设。

## 坑点

| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 代码里硬编码角色名称 | 一旦角色改名或新增同类角色，需要改几十处 `if role == "admin"` | 角色名和权限检查耦合在一起 | 只用权限点判断（`has_permission("article:delete")`），拒绝用角色名判断 |
| 只做前端权限控制 | 普通用户通过浏览器控制台发 fetch 请求，成功删除了文章 | 后端没有做权限校验，以为"前端隐藏了按钮就够了" | 每个需要权限的 API 都必须在后端做权限检查——前端只是用户体验优化 |
| 403 和 401 混用 | 用户看到"403 Forbidden"但实际只是 token 过期 | 认证失败（没登录）和授权失败（没权限）被混淆处理 | 认证中间件统一返回 401，授权装饰器统一返回 403，语义清晰 |
| 权限粒度太粗 | 同一角色内的用户无法做差异化控制，"编辑"既能编辑文章又能删除文章 | 权限设计时一个角色包含过多不同权限点 | 按照资源+操作拆分权限（CRUD 各一个），角色可以灵活组合 |
