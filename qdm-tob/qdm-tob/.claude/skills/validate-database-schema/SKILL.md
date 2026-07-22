---
name: validate-database-schema
description: AI 生成 SQL 后即时校验，确保表设计符合 tob-database.md 全部规则，违规硬阻断
---

# validate-database-schema

## 触发时机

当 AI 准备输出任何包含 `CREATE TABLE` / `ALTER TABLE` / `CREATE INDEX` 的 SQL 语句到文件时，**必须**先调用本 Skill 进行校验。校验通过后方可写入文件。

## 规则来源

**读取 `.claude/rules/tob-database.md` 获取完整规则定义。** 本 Skill 是规则的执行层，不重复定义规则内容。校验时逐节对照规则文件，以下为执行要点：

> **ALTER TABLE 注意：** ALTER TABLE 仅校验涉及变更的字段/索引是否符合对应规则（如新增字段需有 COMMENT、类型正确，新增索引需命名规范），不要求全表重新满足所有规则。表前缀、引擎与字符集仅对 CREATE TABLE 生效，ALTER TABLE 自动跳过。

### 规则文件章节与校验映射

| 规则文件章节 | 校验重点 |
|-------------|---------|
| 表设计规范 | 表前缀 (`sys_/cst_/ord_/prd_`)、表名小写+下划线、表 COMMENT、ENGINE=InnoDB、CHARSET=utf8mb4 |
| 字段设计规范 | 审计字段（如使用则统一命名与类型：created_at/updated_at/created_by/updated_by）、ENUM 类型使用、字段 COMMENT、字段命名规范、NULL 约束 |
| 金额/数量字段规范 | DECIMAL(12,2) / DECIMAL(10,2) / DECIMAL(5,2) 或 DECIMAL(10,4) 精度匹配 |
| 索引设计规范 | 唯一索引 `uk_` 前缀、普通索引 `idx_` 前缀、主键排除 |
| 数据快照规范 | `ord_` 表关联字段 COMMENT 标注 `（快照）` |
| 逻辑删除规范 | 审计类表禁止 `is_deleted`，配置类表 `is_deleted` 格式正确 |
| 跨模块关联规范 | 跨前缀表 JOIN 检测 (`sys_/cst_/ord_/prd_`)，含 Mapper XML |
| Flyway 迁移规范 | 版本号格式、幂等性 (`IF NOT EXISTS`/`IF EXISTS`)、描述命名 |

---

## 校验流程

1. 读取 `.claude/rules/tob-database.md`
2. 对输入 SQL，逐章节对照规则文件，逐条检查
3. 规则文件中每条规范（如 `- **xxx**：...`）视为一项独立检查点
4. 输出通过/违规报告

---

## 输出格式

### 全部通过

```
✅ 数据库规范校验通过（N/N）
```

校验通过后，AI 可以将 SQL 写入目标文件。

### 存在违规

```
❌ 数据库规范校验未通过（X/N）

[规则章节] 违规描述 → 修正建议
...

🔧 请修正以上违规项后重新校验。校验通过前不得将 SQL 写入文件。
```

---

## 阻断规则

校验不通过时：
1. **禁止**将 SQL 写入任何文件
2. **必须**修正所有违规项
3. **必须**重新调用本 Skill 校验，直至全部通过
4. 不允许跳过、不允许标记为"后续修复"、不允许手动豁免（除非用户明确要求）

---

## 扫描范围

| 产出物 | 路径模式 | 校验规则 |
|--------|---------|---------|
| Flyway 迁移 SQL | `**/db/migration/V*__*.sql` | 规则文件全部章节 |
| 通用 SQL 文件 | `**/*.sql` | 规则文件全部章节 |
| MyBatis Mapper XML | `**/mapper/**/*.xml` | 仅跨模块关联规范 |
