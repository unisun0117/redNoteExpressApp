
# 团队全局智能体核心规则与工具库

你是本项目的首席全栈智能体。在处理任何代码编写、重构或接口新增任务时，你拥有以下团队专属的自动化工具（Tools）。


## 可用工具库 (Available Tools)

### Tool 1: `run_qa_validation`
- **用途描述**：团队标准的自动化集成测试执行器。它会读取业务逻辑 (`prd.md`) 和接口契约 (`swagger.json`)，自动生成并运行边界测试用例，最后出具测试报告。
- **强制触发时机（MUST USE WHEN）**：
    1. 用户明确要求“测试一下”、“验证一下”时。
    2. 当你修改或新增了任何 Controller 层的接口代码后。
    3. 当你修改了核心业务逻辑，且认为可能会影响下游接口契约时。
- **执行动作**：当决定调用此工具时，请在终端执行以下命令：
  `ruflo run --skill ./.claude/skills/agent-qa-expert/SKILL.md --with-context ./prd.md,./swagger.json --allow-shell "pytest"`


### Tool 1: `sync_swagger_docs`
- **用途描述**：利用 smart-doc 插件重新编译并生成最新的 Swagger API 契约文档。
- **强制触发时机**：只要你修改了任何 Controller 或 DTO 的 Java 代码，**必须**首先调用此工具！
- **执行动作**：执行 `ruflo run --skill ./.claude/skills/agent-swagger-generator/SKILL.md --allow-shell "mvn,ls"`

## 更新日志维护 (CHANGELOG)

每次完成前后端代码变更后，**必须**在 [docs/CHANGELOG.md](./docs/CHANGELOG.md) 追加当日记录（无则新建当日 `## YYYY-MM-DD` 小节，置顶于最新）。

- **格式**：按端分组（`后端` / `前端-小程序` / `前端-Web` / `前端-H5`），每条一句话 `<模块>：改动说明（影响 / 可测点）`，面向产品 / 测试可读，避免技术细节。
- **范围**：只记「对外行为或可测试点有变化」的改动；纯重构 / 格式化可省略。
- **接口契约变更**（路径 / 入参 / 出参）必须标注 ⚠️ 并说明前端是否需同步改。
- 当日若已存在小节，直接往里追加，不重复建小节。

## 数据库规范强制校验

生成任何 SQL（DDL/DML/Flyway migration/MyBatis Mapper XML 中的 SQL 片段）后，**必须**立即调用 `validate-database-schema` Skill 进行校验。校验通过（✅）后方可将 SQL 写入文件。校验不通过时必须修正违规项后重新校验，不得跳过、不得标记为"后续修复"。