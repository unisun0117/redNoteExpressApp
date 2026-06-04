# AI-Native 研发标准作业流程 SOP

**版本：** v1.0  
**发布日期：** 2026-06-02  
**适用对象：** 开发人员  
**基于流程：** OpenSpec + Superpowers + GStack

---

## 目录

- [第一部分：快速参考](#第一部分快速参考)
- [第二部分：详细操作流程](#第二部分详细操作流程)
- [第三部分：附录](#第三部分附录)

---

## 第一部分：快速参考

### 1.1 流程总览图

```
开始需求 
  ↓
[步骤 0] 生成项目初始规范 (首次) → /opsx:init
  ↓
[步骤 1] 需求孵化 → /brainstorming
  ↓
[步骤 2] 契约生成 ✓ 必须 → /opsx:propose
  ↓
质量卡点：架构师审查
  ↓
[步骤 3] 创建 TDD 计划 → /superpowers:write-plan
  ↓
[步骤 4] TDD 开发 ✓ 必须 → /test-driven-development
  ↓
[步骤 5] 单元测试 ✓ 必须 → mvn test
  ↓
[步骤 6] 代码审查 → /review /codex /cso
  ↓
验证检查：任务完成？测试通过？
  ↓
[步骤 7] 归档 ✓ 必须 → /opsx:archive
  ↓
完成
```

### 1.2 命令速查表

| 步骤 | 命令 | 用途 | 必须？ |
|------|------|------|--------|
| 0 | /opsx:init | 生成项目初始规范 | 首次 |
| 1 | /brainstorming | 需求孵化 | 推荐 |
| 2 | /opsx:propose [feature-name] | 契约生成 | ✓ 必须 |
| 3 | /superpowers:write-plan | 创建 TDD 计划 | 推荐 |
| 4 | /test-driven-development | TDD 开发 | ✓ 必须 |
| 5 | mvn clean test | 运行单元测试 | ✓ 必须 |
| 6 | /review /codex /cso | 代码审查 | 推荐 |
| 7 | /opsx:archive | 归档变更 | ✓ 必须 |


### 1.3 角色与职责

| 角色 | 负责步骤 | 关键职责 |
|------|---------|---------|
| 架构师 | 步骤 2 质量卡点 | 审查 proposal.md/design.md，消除歧义，签字确认 |
| 开发工程师 | 步骤 1-6 | 执行开发流程，编写代码和测试 |
| 技术负责人 | 步骤 6 验证检查 | 代码审查，确保质量达标 |
| 全员 | 步骤 7 | 归档和知识沉淀 |

---

## 第二部分：详细操作流程

---

## 步骤 0：生成项目初始规范（首次使用）

### 目标
基于现有项目代码生成初始规范文档，为后续开发建立基线。

### 适用场景
- 首次在项目中使用 AI-Native 流程
- 现有项目没有规范文档
- 需要为遗留系统建立规范

### 前置条件
- ✓ 已安装 OpenSpec：`npm install -g @fission-ai/openspec@latest`
- ✓ 项目代码已存在
- ✓ 在项目根目录下

### 操作步骤

**步骤 0.1：初始化 OpenSpec**
```bash
openspec init
```

**预期输出：**
```
✓ 创建 openspec/ 目录
✓ 创建 openspec/specs/ 目录
✓ 创建 openspec/changes/ 目录
✓ OpenSpec 初始化完成
```

**步骤 0.2：生成初始规范**
```text
在 Claude Code 中执行：
/opsx:init
```

**AI 会做什么：**
1. 扫描项目结构（pom.xml, src/main/java, src/test/java）
2. 分析核心业务模块
3. 识别数据模型和 API 接口
4. 生成初始规范文档

**步骤 0.3：审查生成的规范**
```bash
# 查看生成的规范文件
ls openspec/specs/

# 阅读主规范文件
cat openspec/specs/main.md
```

### 验证检查
- [ ] openspec/ 目录已创建
- [ ] openspec/specs/main.md 文件存在
- [ ] 规范文档包含项目核心模块描述
- [ ] 规范文档包含主要 API 接口

### 常见问题

**Q: 生成的规范不准确怎么办？**
A: 手动编辑 `openspec/specs/main.md`，补充和修正内容。这是正常的，初始规范只是起点。

**Q: 项目太大，生成时间很长？**
A: 可以先为核心模块生成规范，其他模块后续补充。

---

## 步骤 1：需求孵化

### 目标
通过头脑风暴将模糊的需求想法转化为清晰的需求草稿。

### 前置条件
- ✓ 已完成步骤 0（首次使用）
- ✓ 有明确的功能需求或问题要解决

### 操作步骤

**步骤 1.1：启动头脑风暴**
```text
在 Claude Code 中执行：
/brainstorming
```

**步骤 1.2：描述需求背景**
```text
示例：
我需要实现一个用户积分系统：

**背景**：
- 当前系统是一个电商平台（Spring Boot + MyBatis）
- 用户完成订单后需要获得积分
- 积分可以用于兑换优惠券

**核心诉求**：
- 订单完成后自动计算并发放积分
- 用户可以查询积分余额和明细
- 积分可以兑换优惠券
```

**步骤 1.3：回答 AI 的问题**

AI 会提出一系列问题来澄清需求，例如：
- 积分计算规则是什么？（订单金额的百分比？固定积分？）
- 积分有效期吗？
- 积分兑换比例是多少？
- 需要支持积分过期吗？
- 异常情况如何处理？（订单退款、重复发放）

**逐一回答这些问题，直到需求清晰。**

**步骤 1.4：确认需求草稿**

AI 会生成需求草稿文档，通常保存在：
```
docs/superpowers/specs/YYYY-MM-DD-<feature-name>-design.md
```

### 验证检查
- [ ] 需求草稿文档已生成
- [ ] 核心功能描述清晰
- [ ] 边界条件已明确
- [ ] 异常场景已考虑

### 输出物
- `docs/superpowers/specs/YYYY-MM-DD-user-points-design.md`

---


## 步骤 2：契约生成 ✓ 必须

### 目标
将需求草稿转化为机器可读的工程契约，消除歧义。

### 重要性
⚠️ **这是整个流程中最重要的质量卡点！**
- 架构师必须逐行审查
- 不允许任何 TBD 或模糊描述
- 必须签字确认后才能进入开发

### 前置条件
- ✓ 已完成步骤 1（需求孵化）
- ✓ 需求草稿已确认

### 操作步骤

**步骤 2.1：创建 OpenSpec 提案**
```text
在 Claude Code 中执行：
/opsx:propose user-points-system
```

**步骤 2.2：提供详细需求**
```text
基于 docs/superpowers/specs/YYYY-MM-DD-user-points-design.md，
创建完整的 OpenSpec 提案。

请包含：
- 功能描述
- 数据模型（数据库表结构）
- API 接口定义
- 业务规则
- 约束条件
```

**步骤 2.3：AI 生成契约工件**

AI 会生成以下文件：
```
openspec/changes/user-points-system/
├── proposal.md      # 为什么做、做什么
├── design.md        # 技术方案、架构设计
├── specs/           # 增量规范
│   └── points/spec.md
└── tasks.md         # 实现任务清单
```

**步骤 2.4：架构师审查（质量卡点）**

**审查 proposal.md：**
- [ ] 功能描述清晰无歧义
- [ ] 业务术语准确（例如："积分"vs"奖励金"）
- [ ] 范围边界明确

**审查 design.md：**
- [ ] 数据库表结构完整（字段类型、长度、索引）
- [ ] API 接口定义清晰（请求参数、响应格式、错误码）
- [ ] 技术方案可行
- [ ] 性能考虑（例如：积分计算是同步还是异步？）
- [ ] 安全考虑（例如：防止重复发放积分）

**审查 tasks.md：**
- [ ] 任务分解合理
- [ ] 任务之间依赖关系清晰
- [ ] 每个任务都有明确的验收标准

**步骤 2.5：消除 TBD 和歧义**

如果发现 TBD 或模糊描述：
```text
请补充以下内容：
1. proposal.md 第 3 节中的"积分计算规则 TBD"需要明确
2. design.md 中的"API 超时时间"需要指定具体数值
3. tasks.md 中的"实现积分服务"太宽泛，需要拆分
```

**步骤 2.6：架构师签字确认**

在 proposal.md 底部添加：
```markdown
## 审查记录

- 审查人：张三（架构师）
- 审查时间：2026-05-28
- 审查结论：通过 ✓
```

### 验证检查
- [ ] 所有工件文件已生成
- [ ] 无 TBD 标识
- [ ] 架构师已签字确认
- [ ] 数据库表结构完整（Java 实体类对应）
- [ ] API 接口定义清晰（Controller 方法签名对应）

### 输出物
```
openspec/changes/user-points-system/
├── proposal.md      ✓ 已审查
├── design.md        ✓ 已审查
├── specs/points/spec.md
└── tasks.md         ✓ 已审查
```

### Java/Spring Boot 特定检查
- [ ] 实体类字段类型明确（Long/Integer/BigDecimal）
- [ ] MyBatis Mapper 方法签名清晰
- [ ] Controller 接口路径和 HTTP 方法明确
- [ ] Service 层业务逻辑边界清晰
- [ ] 事务边界已定义（@Transactional 位置）

---


## 步骤 3：创建 TDD 计划

### 目标
基于 OpenSpec 任务清单创建测试驱动开发计划。

### 前置条件
- ✓ 已完成步骤 2（契约生成）
- ✓ 架构师已签字确认

### 操作步骤

**步骤 3.1：创建 TDD 计划**
```text
在 Claude Code 中执行：
/superpowers:write-plan
```

**步骤 3.2：提供 OpenSpec 任务**
```text
请根据以下 OpenSpec 任务创建 TDD 实现计划：

【复制 openspec/changes/user-points-system/tasks.md 的全部内容】

## 要求

### 1. TDD 循环
为每个功能创建完整的 RED-GREEN-REFACTOR 循环：
- RED 阶段: 编写测试用例（每个用例一个任务）
- GREEN 阶段: 实现功能让测试通过（一个任务）
- REFACTOR 阶段: 重构和优化代码（一个任务）

### 2. 任务标注
每个任务必须标注对应的 OpenSpec 任务编号：
- 格式: Task N: [OpenSpec: X.X] 功能名称 - 阶段
- 示例: Task 1: [OpenSpec: 1.1] 创建积分实体类 - RED

### 3. Java/Spring Boot 测试要求
- 单元测试使用 JUnit 5 + Mockito
- Service 层测试需要 mock Repository
- Controller 层测试使用 MockMvc
- 集成测试使用 @SpringBootTest
```

**步骤 3.3：审查生成的计划**

AI 会生成类似这样的计划：
```markdown
## OpenSpec 任务映射

| OpenSpec 任务 | Superpowers 任务 | 状态 |
|--------------|-----------------|------|
| 1.1 创建积分实体类 | Task 1-3 | ⏳ |
| 1.2 实现积分 Repository | Task 4-6 | ⏳ |
| 1.3 实现积分 Service | Task 7-12 | ⏳ |
| 1.4 实现积分 Controller | Task 13-18 | ⏳ |

## 详细任务

### Task 1: [OpenSpec: 1.1] 创建积分实体类 - RED
编写测试用例:
- testPointsEntityCreation
- testPointsEntityValidation
验证: 测试编译通过，运行失败（RED）

### Task 2: [OpenSpec: 1.1] 创建积分实体类 - GREEN
实现 Points 实体类
验证: 所有测试通过（GREEN）

### Task 3: [OpenSpec: 1.1] 创建积分实体类 - REFACTOR
重构实体类（如需要）
验证: 测试仍然通过
```

### 验证检查
- [ ] 计划文件已生成（通常在 `docs/superpowers/plans/` 目录）
- [ ] 每个 OpenSpec 任务都有对应的 TDD 任务
- [ ] 每个功能都有 RED-GREEN-REFACTOR 三个阶段
- [ ] 任务标注了 OpenSpec 编号
- [ ] 包含 OpenSpec 任务映射表

### 输出物
- `docs/superpowers/plans/YYYY-MM-DD-user-points-system-plan.md`

---

## 步骤 4：TDD 开发 ✓ 必须

### 目标
严格按照 TDD 循环（RED-GREEN-REFACTOR）实现功能。

### 前置条件
- ✓ 已完成步骤 3（创建 TDD 计划）
- ✓ 开发环境已配置（JDK, Maven, IDE）

### 操作步骤

**步骤 4.1：启动 TDD 开发**
```text
在 Claude Code 中执行：
/test-driven-development

或使用子代理驱动（推荐）：
/subagent-driven-development
```

**步骤 4.2：执行 TDD 循环**

**RED 阶段示例：**
```java
// src/test/java/com/example/points/service/PointsServiceTest.java
@ExtendWith(MockitoExtension.class)
class PointsServiceTest {
    
    @Mock
    private PointsRepository pointsRepository;
    
    @InjectMocks
    private PointsService pointsService;
    
    @Test
    void testAddPoints_Success() {
        // Given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        
        // When & Then
        assertDoesNotThrow(() -> 
            pointsService.addPoints(userId, amount)
        );
        
        verify(pointsRepository, times(1)).save(any(Points.class));
    }
}
```

**运行测试（应该失败）：**
```bash
mvn test -Dtest=PointsServiceTest
```

**预期输出：**
```
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
[ERROR] testAddPoints_Success  Time elapsed: 0.001 s  <<< ERROR!
java.lang.NullPointerException
```

**GREEN 阶段示例：**
```java
// src/main/java/com/example/points/service/PointsService.java
@Service
public class PointsService {
    
    private final PointsRepository pointsRepository;
    
    public PointsService(PointsRepository pointsRepository) {
        this.pointsRepository = pointsRepository;
    }
    
    public void addPoints(Long userId, BigDecimal amount) {
        Points points = new Points();
        points.setUserId(userId);
        points.setAmount(amount);
        points.setCreateTime(LocalDateTime.now());
        
        pointsRepository.save(points);
    }
}
```

**运行测试（应该通过）：**
```bash
mvn test -Dtest=PointsServiceTest
```

**预期输出：**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**REFACTOR 阶段：**
- 提取常量
- 优化代码结构
- 添加必要的注释
- 再次运行测试确保仍然通过

**步骤 4.3：定期同步 OpenSpec 任务**

每完成一个 OpenSpec 任务组后：
```text
请更新 openspec/changes/user-points-system/tasks.md：
- 将 1.1 节下的所有任务标记为 [x] 完成
```

### TDD 最佳实践

**DO ✅**
- 先写测试，再写实现
- 每次只实现一个小功能
- 测试失败时不要写新测试
- 保持测试简单清晰
- 频繁运行测试

**DON'T ❌**
- 跳过 RED 阶段直接写实现
- 一次实现多个功能
- 测试中包含复杂逻辑
- 忽略测试失败
- 提交未通过测试的代码

### 验证检查
- [ ] 每个功能都有对应的测试
- [ ] 所有测试都通过
- [ ] 代码覆盖率达标（建议 ≥ 80%）
- [ ] OpenSpec tasks.md 已同步更新

---


## 步骤 5：单元测试 ✓ 必须

### 目标
运行完整的测试套件，确保所有测试通过，代码质量达标。

### 前置条件
- ✓ 已完成步骤 4（TDD 开发）
- ✓ 所有代码已提交到本地分支

### 操作步骤

**步骤 5.1：清理并编译**
```bash
mvn clean compile
```

**步骤 5.2：运行所有单元测试**
```bash
mvn test
```

**步骤 5.3：检查测试覆盖率**
```bash
mvn test jacoco:report
```

### 验证检查
- [ ] 所有单元测试通过（0 failures, 0 errors）
- [ ] 测试覆盖率达标（≥ 80%）
- [ ] 核心业务逻辑覆盖率 ≥ 90%
- [ ] 无编译警告

---

## 步骤 6：代码审查

### 目标
通过多模型 AI 代码审查发现潜在问题，提升代码质量。

### 操作步骤

**步骤 6.1：资深工程师审查**
```text
/review
```

**步骤 6.2：第二意见审查**
```text
/codex
```

**步骤 6.3：安全审查**
```text
/cso
```

### 验证检查
- [ ] 已完成三轮审查
- [ ] 高严重度问题已修复
- [ ] 修复后测试仍然通过

---

## 步骤 7：归档 ✓ 必须

### 目标
将增量规范合并到主规范，归档变更记录，形成知识沉淀。

### 操作步骤

**步骤 7.1：归档前验证**
```bash
cat openspec/changes/user-points-system/tasks.md
```

**步骤 7.2：执行归档**
```text
/opsx:archive
```

**步骤 7.3：提交代码**
```bash
git add .
git commit -m "feat: 实现用户积分系统"
git push origin feature/user-points-system
```

### 验证检查
- [ ] 变更已归档
- [ ] 主规范已更新
- [ ] 代码已提交并推送

---


## 第三部分：附录

---

## 附录 A：常见问题与故障排除

### A.1 环境配置问题

**Q: OpenSpec 命令找不到？**
```bash
npm install -g @fission-ai/openspec@latest
openspec --version
```

**Q: Maven 编译失败？**
```bash
java -version
mvn clean install -U
```

### A.2 流程执行问题

**Q: 步骤 2 生成的契约不准确？**
- 提供更详细的需求描述
- 明确指出不准确的部分
- 手动编辑 proposal.md 和 design.md

**Q: TDD 开发时测试一直失败？**
- 检查测试代码是否正确
- 检查 mock 对象配置
- 查看详细错误信息
- 逐步调试

**Q: 代码审查发现的问题太多？**
- 按严重程度排序（高 → 中 → 低）
- 优先修复高严重度问题
- 每修复一个问题，运行测试验证

### A.3 测试相关问题

**Q: 测试覆盖率如何提高？**
- 查看 JaCoCo 报告，识别未覆盖代码
- 补充边界条件测试
- 补充异常场景测试

**Q: 集成测试很慢怎么办？**
- 使用切片测试（@WebMvcTest）
- 单元测试使用 Mock
- 避免每个测试都启动完整 Spring 容器

---

## 附录 B：Java/Spring Boot 验证清单

### B.1 代码结构检查

**项目结构：**
```
src/
├── main/java/com/example/
│   ├── entity/          # 实体类
│   ├── repository/      # 数据访问层
│   ├── service/         # 业务逻辑层
│   ├── controller/      # 控制器层
│   ├── dto/             # 数据传输对象
│   └── exception/       # 异常处理
└── test/java/com/example/
    ├── service/
    └── controller/
```

**检查清单：**
- [ ] 分层清晰（Controller → Service → Repository）
- [ ] 包命名规范
- [ ] 类命名符合约定

### B.2 代码质量检查

**实体类检查：**
- [ ] 使用 @Entity 和 @Table 注解
- [ ] 字段类型正确（金额用 BigDecimal）
- [ ] 添加必要的约束

**Service 层检查：**
- [ ] 使用 @Service 注解
- [ ] 事务注解正确（@Transactional）
- [ ] 构造器注入依赖
- [ ] 参数验证完整

**Controller 层检查：**
- [ ] 使用 @RestController 注解
- [ ] 路径映射清晰
- [ ] 使用 @Valid 验证请求参数
- [ ] 返回统一的响应格式

### B.3 测试质量检查

**单元测试检查：**
- [ ] 使用 @ExtendWith(MockitoExtension.class)
- [ ] Mock 对象配置正确
- [ ] 测试方法命名清晰
- [ ] 遵循 Given-When-Then 结构

**集成测试检查：**
- [ ] 使用 @WebMvcTest 切片测试
- [ ] MockMvc 配置正确
- [ ] 测试 HTTP 请求和响应

### B.4 性能和安全检查

**性能检查清单：**
- [ ] 避免 N+1 查询
- [ ] 添加必要的数据库索引
- [ ] 使用分页查询大数据集
- [ ] 合理使用缓存

**安全检查清单：**
- [ ] 使用 PreparedStatement 防止 SQL 注入
- [ ] 验证所有用户输入
- [ ] 敏感信息不记录到日志
- [ ] 实现认证和授权

---

## 附录 C：术语表

| 术语 | 英文 | 说明 |
|------|------|------|
| 契约 | Contract | OpenSpec 生成的机器可读的需求文档 |
| TDD | Test-Driven Development | 测试驱动开发 |
| RED-GREEN-REFACTOR | - | TDD 的三个阶段 |
| 质量卡点 | Quality Gate | 流程中的关键审查点 |
| 归档 | Archive | 将完成的变更合并到主规范 |
| 增量规范 | Incremental Spec | 针对单个功能的规范文档 |
| 主规范 | Main Spec | 项目的完整规范文档 |
| 工件 | Artifact | OpenSpec 生成的文件 |
| 覆盖率 | Coverage | 测试覆盖的代码比例 |
| Mock | - | 模拟对象，用于单元测试 |

---

## 附录 D：快速参考卡片

### 新手开发者快速上手

**第一次使用（项目初始化）：**
```bash
1. openspec init
2. /opsx:init
```

**日常开发流程：**
```bash
1. /brainstorming                    # 需求孵化
2. /opsx:propose [feature-name]      # 契约生成
3. 架构师审查并签字                    # 质量卡点
4. /superpowers:write-plan           # 创建 TDD 计划
5. /test-driven-development          # TDD 开发
6. mvn clean test                    # 运行测试
7. /review /codex /cso               # 代码审查
8. /opsx:archive                     # 归档
9. git commit && git push            # 提交代码
```

**遇到问题时：**
- 查看附录 A（常见问题）
- 查看附录 B（验证清单）
- 询问架构师或技术负责人

---

### 关键检查点

**步骤 2 后（契约生成）：**
- [ ] 无 TBD 标识
- [ ] 架构师已签字
- [ ] 数据库表结构完整
- [ ] API 接口定义清晰

**步骤 5 后（单元测试）：**
- [ ] 所有测试通过
- [ ] 覆盖率 ≥ 80%
- [ ] 无编译警告

**步骤 7 前（归档前）：**
- [ ] 所有任务完成
- [ ] 所有测试通过
- [ ] 代码审查通过

---

**文档版本：** v1.0  
**最后更新：** 2026-06-02
**维护者：** 应用研发技术团队  
**反馈渠道：** 请联系AI Coding 虚拟组

