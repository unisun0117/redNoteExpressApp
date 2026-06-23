# 项目通用规范 & 新项目搭建指南

> 基于 aicoding-test（加盟商管理平台）项目提炼的通用规范。
> 适用于使用 OpenSpec + SuperPower 工作流的前后端实战项目。

---

## 一、项目整体架构

```
<project-root>/
├── front/                    # 前端：Vue 3 + TypeScript + Vite
│   ├── src/
│   │   ├── main.ts           # 入口
│   │   ├── App.vue           # 根组件（tabbar + router-view）
│   │   ├── router/           # Vue Router 路由
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── views/            # 页面级组件
│   │   ├── components/       # 领域分组的可复用组件
│   │   ├── utils/            # 工具函数（request.ts 等）
│   │   ├── styles/           # 全局 CSS 变量
│   │   └── __tests__/        # 测试
│   ├── vite.config.ts        # 构建配置 + 代理 + 测试
│   └── package.json
├── back/                     # 后端：Spring Boot 2.7 + Java 8 + Maven
│   ├── src/main/java/com/demo/
│   │   ├── Application.java      # 入口
│   │   ├── common/               # Result 统一响应、DataInitializer
│   │   ├── config/               # Security、JWT Filter、GlobalExceptionHandler
│   │   ├── controller/           # REST 控制器
│   │   ├── service/              # 业务逻辑
│   │   ├── mapper/               # MyBatis-Plus Mapper
│   │   ├── entity/               # 数据库实体
│   │   ├── dto/                  # 请求/响应 DTO
│   │   └── util/                 # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml       # 主配置
│   │   └── db/                   # schema.sql + data.sql
│   └── pom.xml
├── openspec/                # OpenSpec 变更管理
│   ├── config.yaml          # schema: spec-driven
│   ├── changes/archive/     # 已归档变更
│   └── specs/               # 当前活跃规格
├── docs/superpowers/        # SuperPower 文档归档
│   ├── plans/               # 实施计划文档
│   └── specs/               # 设计规格文档
├── .claude/                 # Claude Code 配置
│   ├── settings.json        # 项目级权限
│   ├── settings.local.json  # 本地权限（gitignore）
│   ├── skills/              # OpenSpec 技能定义
│   └── commands/opsx/       # OPSX 命令定义
├── .superpowers/            # SuperPower 头脑风暴数据
└── .gitignore
```

---

## 二、前端通用规范

### 技术栈

| 类别 | 选型 | 版本 |
|------|------|------|
| 框架 | Vue 3 Composition API | 3.5.x |
| 语言 | TypeScript | 5.x / 6.x |
| 构建 | Vite | 8.x |
| UI 库 | Vant 4（移动端） | 4.x |
| 状态管理 | Pinia（组合式） | 3.x |
| 路由 | Vue Router 4 | 4.x |
| HTTP | 原生 fetch 封装 | - |
| 图标 | Remixicon | 4.x |
| 测试 | Vitest + @testing-library/vue | 4.x |

### 组件约定

- **命名**：PascalCase 文件名：`BannerSwiper.vue`、`KpiCardGrid.vue`
- **脚本**：统一 `<script setup lang="ts">`，使用 `defineProps<T>()`、`defineEmits<T>()`
- **样式**：`<style scoped>`，根元素 class 用 kebab-case 匹配组件名
- **目录**：`components/<domain>/ComponentName.vue`，测试放 `__tests__/` 子目录
- **视图**：`views/XxxPage.vue`，在 `onMounted` 中调 store 加载数据

### Store 模式（Pinia 组合式）

```ts
export const useXxxStore = defineStore('xxx', () => {
  // 状态用 ref
  const data = ref<Item[]>([])
  const loading = ref(false)

  // 操作用 async function
  async function fetchAll() {
    loading.value = true
    try {
      data.value = await request<Item[]>('/api/xxx')
    } finally {
      loading.value = false
    }
  }

  // 返回所有 ref 和方法
  return { data, loading, fetchAll }
})
```

### HTTP 客户端（`utils/request.ts`）

- 封装原生 `fetch`，自动附加 `Authorization: Bearer <token>`
- 统一响应：`{ code: number; data: T; message: string }`
- 401 单次跳转防护（`handling401` 标志位）
- 非 200 code 抛异常，页面层 try/catch 处理

### 路由模式

- `createWebHistory()` 模式
- `meta.requiresAuth` 控制认证
- `router.beforeEach` 导航守卫检查 token
- 懒加载：`() => import('../views/XxxPage.vue')`

### 样式约定

- CSS 自定义属性集中在 `styles/variables.css`
- Vant 组件穿透用 `:deep(.van-xxx)`

---

## 三、后端通用规范

### 技术栈

| 类别 | 选型 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 2.7.x |
| 语言 | Java | 8 |
| 构建 | Maven | 3.9.x |
| ORM | MyBatis-Plus | 3.5.x |
| 安全 | Spring Security + JWT | jjwt 0.11.x |
| 密码 | BCrypt | spring-boot-starter-security |
| 验证 | javax.validation | 2.x |
| 测试 | JUnit 5 + Mockito | - |

### 分层架构（4 层）

```
Controller (@RestController)  → 薄层，接收请求，调用 Service，返回 Result<T>
  ↓
Service (@Service)            → 业务逻辑，抛 RuntimeException 表示业务异常
  ↓
Mapper (extends BaseMapper)   → MyBatis-Plus 自动 CRUD + @Select 自定义 SQL
  ↓
Entity (@TableName)           → 数据库实体映射
```

### 统一响应格式

```java
// common/Result.java
public class Result<T> {
    private int code;      // 200 成功，其他失败
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) { return new Result<>(200, "ok", data); }
    public static <T> Result<T> fail(int code, String message) { return new Result<>(code, message, null); }
}
```

### 安全模式

- Spring Security 无状态会话 + 禁用 CSRF
- `JwtAuthenticationFilter` 继承 `OncePerRequestFilter` 提取 `Bearer <token>`
- `SecurityContextHolder` 存储认证信息
- 白名单：`/api/auth/login`、`/api/auth/register`、`/api/health`
- 自定义 `AuthenticationEntryPoint` / `AccessDeniedHandler` 返回 JSON

### Controller 模式

```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {
    private final XxxService xxxService;  // 构造器注入

    @GetMapping("/something")
    public Result<Map<String, Object>> something() {
        try {
            return Result.ok(xxxService.getSomething());
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
```

### 异常处理

- `GlobalExceptionHandler`（`@RestControllerAdvice`）
- `MethodArgumentNotValidException` → 400
- `Exception` → 500（记录日志）

### 数据库约定

- 手动 SQL DDL（`db/schema.sql`），无 Flyway/Liquibase
- `spring.sql.init.mode=always` 自动执行
- 字符集 `utf8mb4`，金额用 `BigDecimal(DECIMAL(12,2))`
- `@TableId(type = IdType.AUTO)` 自增主键
- 不使用 Lombok，手写 getter/setter

### DTO 约定

- 请求 DTO：`XxxRequest`，带 `@NotBlank`、`@Pattern` 校验注解
- 响应 DTO：`XxxResponse`，含无参 + 全参构造函数
- 列表数据用 `Map<String, Object>` 包装

---

## 四、OpenSpec 工作流

### 配置（`openspec/config.yaml`）

```yaml
schema: spec-driven
# context: |        # 可选：项目技术栈描述
# rules:             # 可选：自定义规则
```

### 变更生命周期

```
openspec-propose → 生成 proposal.md + design.md + tasks.md + specs/
    ↓
openspec-apply → 按 tasks.md 逐项实现
    ↓
openspec-archive → 归档到 openspec/changes/archive/
    ↓
文档归入 docs/superpowers/plans/ + docs/superpowers/specs/
```

### 变更目录结构

```
openspec/changes/<change-name>/
├── .openspec.yaml     # 元数据
├── proposal.md        # 变更提案（why/what）
├── design.md          # 技术设计（how）
├── tasks.md           # 任务清单
└── specs/<capability>/
    └── spec.md        # 需求规格
```

---

## 五、SuperPower 用法

### 头脑风暴（`.superpowers/brainstorm/`）
- 可视化探索（HTML mockup 前后对比）
- 生成 `before-after.html` 展示设计变更

### 文档归档（`docs/superpowers/`）
- **plans/**：实施计划（Goal → Architecture → Tasks）
- **specs/**：设计规格（技术决策 + API + 数据库 + 测试策略）
- 命名：`YYYY-MM-DD-feature-name.md`

---

## 六、Git 提交规范

| 前缀 | 用途 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | 修复 bug |
| `chore:` | 杂项（配置、依赖） |
| `perf:` | 性能优化 |
| `test:` | 测试相关 |
| `docs:` | 文档 |

---

## 七、新项目搭建检查清单

1. [ ] **目录结构**：`front/` + `back/` + `openspec/` + `docs/`
2. [ ] **OpenSpec 初始化**：`openspec init` 生成 `openspec/config.yaml`
3. [ ] **前端初始化**：Vue 3 + Vite + Vant + Pinia + Vue Router + Vitest
4. [ ] **后端初始化**：Spring Boot + MyBatis-Plus + Spring Security + JWT + JUnit 5
5. [ ] **Claude Code 配置**：`.claude/settings.json` + skills + commands
6. [ ] **文档目录**：`docs/superpowers/plans/` + `docs/superpowers/specs/`
7. [ ] **启动脚本**：`start-backend.bat` / `start-backend.sh`
8. [ ] **核心配置**：`application.yml`（MySQL+JWT）、`vite.config.ts`（代理+测试）
9. [ ] **Git 规范**：commit 用 `feat:`/`fix:`/`chore:`/`perf:`/`test:`/`docs:`

---

## 八、工作流 SOP

1. **需求阶段**：SuperPower 头脑风暴 → 计划 + 设计文档
2. **规格阶段**：`/opsx:propose` → proposal + design + tasks + specs
3. **实现阶段**：`/opsx:apply` → 逐任务实现 + 测试
4. **归档阶段**：`/opsx:archive` → 归档到 `changes/archive/`
5. **文档归入**：plan 和 spec 归入 `docs/superpowers/`
6. **提交**：`git commit -m "feat: xxx"` + push
