"""Generate a JSON Canvas mind map of the Skills classification system."""
import json, random

def hex_id():
    return ''.join(random.choice('0123456789abcdef') for _ in range(16))

# Skill data organized by category
categories = {
    "📝 内容创作系": {
        "color": "4",
        "skills": [
            "📚 知识专栏生成器 · 从0到1学技术，10篇专栏",
            "🎓 课程大纲生成器 · 项目源码→15个Lab课程",
            "🔬 Lab详细内容生成器 · 大纲+源码→完整实验手册",
            "🏭 项目工厂 · 行业+平台→全栈项目+课程+文档",
            "✍️ CSDN专栏写手 · Markdown适配+排期+发布",
            "🔄 同步课程内容 · OpenSpec变更→自动更新Lab",
            "📋 周报生成器 · 随手日报→标准格式周报",
            "🎨 UI先行设计 · 先出原型再写代码",
            "🏗️ UI页面搭建器 · 12种布局+设计令牌体系",
        ]
    },
    "📱 自媒体 & 商业": {
        "color": "2",
        "skills": [
            "🎯 频道战略师 · Ikigai四圈+七项检验+五重过滤",
            "🔬 博主蒸馏器 · 采集笔记→蒸馏爆款→生成创作指南",
            "🔥 AIHOT资讯日报 · 每日AI五版块自动生成",
            "🏥 dbskill商业诊断 · 21个子工具全覆盖",
            "🔎 寻找社群 · 先社区后产品",
            "🧪 验证想法 · 七项检验评估商业点子",
            "🥚 最小可行产品 · 只做核心必要功能",
            "⚙️ 流程化 · 先人工跑通再写代码",
            "🎯 首批客户 · 三圈法则获取种子用户",
            "💰 定价 · 成本模型 vs 价值模型",
            "📢 营销计划 · 先教育后销售",
            "📈 稳健增长 · 盈利第一规模第二",
            "🏛️ 公司价值观 · 招聘前定义文化",
            "✂️ 极简复盘 · 8条原则快速决策",
        ]
    },
    "💻 开发方法论": {
        "color": "3",
        "skills": [
            "💡 头脑风暴 · 先探索再写代码",
            "📋 Planning with Files · 三文件持久化规划",
            "📝 写实施计划 · 零上下文分任务定文件",
            "▶️ 执行计划 · 按步骤实施带审查检查点",
            "🧪 TDD · 红→绿→重构循环",
            "🔍 系统化调试 · 排查非瞎猜",
            "🤖 子智能体驱动 · 并行提效隔离风险",
            "🚀 并行调度智能体 · 多AI并行处理",
            "✅ 完成前验证 · 端到端验证不改不动",
            "📮 请求代码审查 · 逻辑漏洞+代码质量",
            "📬 接收审查反馈 · 验证而非盲从",
            "🏁 完成开发分支 · 合并/PR/清理",
            "🌲 Git Worktrees · 隔离环境开发",
            "⚡ Superpowers指南 · Skills使用基础规则",
            "✍️ 编写Skills · TDD映射+CSO+流程图",
            "🔧 Skill Creator · 四阶段官方生成器",
            "📦 Agent Skills · Addy Osmani工程库",
            "🔄 Ralph Wiggum · AI自主循环迭代",
            "💎 Taste品味UI · 43k Star设计灵敏度",
            "📋 PM Skills · 100+产品经理技能",
        ]
    },
    "🔧 效率工具": {
        "color": "5",
        "skills": [
            "🧠 Headroom · 上下文压缩60-95% Token",
            "🌐 Agent Reach · 搜17平台零API费",
            "🖥️ OpenCLI · Chrome操作任意网站",
            "🧪 Webapp Testing · Playwright自动化",
            "🔌 MCP Builder · 四阶段搭建MCP",
            "📡 Last30Days · 30天社区信号搜索",
            "📓 NotebookLM · 文档问答带引用",
            "📝 Obsidian Markdown · Wikilinks/Callouts",
            "🗃️ Obsidian Bases · 视图/过滤器/公式",
            "🎨 Obsidian Canvas · JSON Canvas画布",
            "⌨️ Obsidian CLI · 命令行操作vault",
            "📄 Defuddle · 网页提取干净Markdown",
            "🧬 Claude Scientific · 140+科学计算技能",
        ]
    },
    "🎨 设计 / 媒体": {
        "color": "1",
        "skills": [
            "🎯 UI UX Pro Max · 161规则+67风格",
            "📊 PPTX · Anthropic官方PPT技能",
            "🪧 归藏社交卡片 · 28版式+10主题",
            "🖼️ 小红书图片 · 小红书风格图片生成",
            "📝 Word文档 · 创建编辑docx",
            "📕 PDF处理 · 提取/创建/合并/拆分",
            "📗 Excel表格 · 公式/图表/透视表",
            "🎨 Frontend Design · 564K+安装去AI味UI",
            "🖼️ 封面图生成 · (空壳待配置)",
            "🎨 图片生成 · (空壳待配置)",
            "📊 幻灯片生成 · (空壳待配置)",
        ]
    },
    "📦 空壳目录": {
        "color": "6",
        "skills": [
            "📖 读书学习 · 无SKILL.md",
            "🔍 代码审查专家 · 无SKILL.md",
            "Σ Sigma · 用途未知",
            "🔨 Skill Forge + Review · 无SKILL.md",
        ]
    },
}

nodes = []
edges = []

# Center node
center_id = hex_id()
nodes.append({
    "id": center_id,
    "type": "text",
    "x": 60, "y": 600,
    "width": 320, "height": 90,
    "text": "# 🎯 Skills 技能体系\n##### 阿珊的 Claude Code 技能控制台",
    "color": "4",
})

# Layout: categories in a horizontal row below center, skills fan out below each
cat_spacing = 380
skill_spacing_y = 70
cat_y = 780
skill_start_y = 940

cat_ids = []
for idx, (cat_name, cat_data) in enumerate(categories.items()):
    cat_id = hex_id()
    cat_ids.append(cat_id)
    count = len(cat_data["skills"])

    cat_x = 40 + idx * cat_spacing
    nodes.append({
        "id": cat_id,
        "type": "text",
        "x": cat_x, "y": cat_y,
        "width": 340, "height": 65,
        "text": f"## {cat_name}\n*{count} 个技能*",
        "color": cat_data["color"],
    })

    # Edge: center → category
    edges.append({
        "id": hex_id(),
        "fromNode": center_id,
        "fromSide": "bottom",
        "toNode": cat_id,
        "toSide": "top",
        "toEnd": "arrow",
    })

    # Skill nodes under each category
    for si, skill_text in enumerate(cat_data["skills"]):
        sid = hex_id()
        nodes.append({
            "id": sid,
            "type": "text",
            "x": cat_x, "y": skill_start_y + si * skill_spacing_y,
            "width": 340, "height": 55,
            "text": skill_text.replace(" · ", "\n*"),
        })
        edges.append({
            "id": hex_id(),
            "fromNode": cat_id,
            "fromSide": "bottom",
            "toNode": sid,
            "toSide": "top",
            "toEnd": "arrow",
        })

canvas = {"nodes": nodes, "edges": edges}

outpath = "Skills-体系思维导图.canvas"
with open(outpath, "w", encoding="utf-8") as f:
    json.dump(canvas, f, ensure_ascii=False, indent=2)

print(f"Generated {outpath}")
print(f"  {len(nodes)} nodes, {len(edges)} edges")
