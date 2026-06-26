# Lab 06：AI 接口对接 — 调用 DeepSeek 生成文章

> **预计用时：** 40-45 分钟
> **难度：** ⭐⭐⭐
> **前置 Lab：** Lab 05（登录流程必须跑通）

---

## 📌 前言

对接 AI 接口这件事，说难不难，说简单也不简单。关键不是调 API，是写 Prompt。我第一次写 Prompt 的时候，随便丢了一句话给 AI——"帮我写一篇小红书文案"——结果吐出来的东西又长又臭，完全没法用。

后来我发现：**给 AI 的指令越清晰，它吐出来的东西越靠谱。** 你要告诉它——你是谁（人设）、你想要什么风格（语气）、输出什么格式（JSON）、字数多少（约束）。这一 Lab，我把我调了十几版的 Prompt 模板全部分享给你。

---

## 📚 基础知识储备

- **LLM（大语言模型）** — 就是我们常说的 AI 大脑。GPT、DeepSeek、Claude 都是 LLM
- **API Key** — 调用 AI 服务需要的"钥匙"，相当于你的账号密码，不要泄露
- **Prompt（提示词）** — 你发给 AI 的指令。Prompt 的质量直接决定输出质量
- **System Prompt vs User Prompt** — System Prompt 设定 AI 的角色和行为（"你是一个美食博主"），User Prompt 是你具体要它做什么（"写一篇关于咖啡店的文章"）
- **OpenAI SDK** — OpenAI 提供的 Python 库。DeepSeek 兼容 OpenAI 的接口格式，所以我们可以直接用 OpenAI SDK 调 DeepSeek
- **AsyncOpenAI** — OpenAI SDK 的异步版本。FastAPI 是异步框架，我们用异步的客户端

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 获取并配置 DeepSeek API Key
2. 理解 `ArticleGenerator` 的 Prompt 构建逻辑
3. 在 Swagger 上测试 `/api/generate`，拿到第一篇 AI 生成的文章
4. 理解 JSON 结构化输出的原理

---

## 🛠 动手实战

### 步骤 1：获取 DeepSeek API Key

**操作：**
1. 打开 https://platform.deepseek.com
2. 注册账号（用手机号即可）
3. 进入"API Keys"页面，点击"创建 API Key"
4. 复制生成的 key（格式：`sk-xxxxxxxxxxxxxxxx`）
5. 新用户通常有免费额度，够学完这门课

---

### 步骤 2：配置环境变量

**操作：**
在 `backend` 目录下创建 `.env` 文件：

```bash
cd E:\AI_Workspace\redNoteExpressApp\backend
```

创建 `.env` 文件，写入：
```
OPENAI_API_KEY=sk-你的DeepSeek-Key
LLM_BASE_URL=https://api.deepseek.com
LLM_MODEL=deepseek-chat
```

**验证：** 重启后端，看日志有没有报 API Key 相关错误 ✅

---

### 步骤 3：读懂 AI 生成核心 `services/generator.py`

**打开文件：** `backend/app/services/generator.py`

**风格 Prompt 字典（决定了文章的语气）：**

```python
STYLE_PROMPTS = {
    "复古风": "使用复古怀旧的语气，加入经典词汇和年代感表达，营造温暖怀旧的氛围。",
    "简约风": "使用简洁优雅的语气，避免冗长修饰，突出核心信息，风格清新自然。",
    "幽默风": "使用俏皮幽默的语气，加入网络流行语和搞笑表达，让读者会心一笑。",
    "深度测评风": "使用专业细致的分析语气，多维度评测，注重细节和对比，给人可信赖感。",
}
```

**赛道 Prompt 字典（决定了 AI 的人设）：**

```python
TRACK_PROMPTS = {
    "美食": "你是一个美食探店博主，专注分享美食体验和餐厅推荐。",
    "运动": "你是一个运动健身博主，专注分享运动体验和健康生活方式。",
    "摄影": "你是一个摄影博主，专注分享摄影技巧和视觉美学。",
    "萌宠": "你是一个宠物博主，专注分享萌宠日常和养宠心得。",
    "家居": "你是一个家居博主，专注分享家居设计和生活美学。",
    "美妆": "你是一个美妆博主，专注分享化妆技巧和护肤心得。",
    "数码": "你是一个数码博主，专注分享数码产品评测和科技资讯。",
    "母婴": "你是一个母婴博主，专注分享育儿经验和母婴好物。",
    "餐饮": "你是一个餐饮行业博主，专注分享餐饮经营和店铺推荐。",
}
```

---

### 步骤 4：理解生成函数的核心逻辑

```python
class ArticleGenerator:
    def __init__(self):
        self.client = AsyncOpenAI(
            api_key=settings.OPENAI_API_KEY,    # ← DeepSeek 的 Key
            base_url=settings.LLM_BASE_URL,     # ← 指向 DeepSeek
        )

    async def generate(self, ...) -> dict:
        # 1. 组装 System Prompt（告诉 AI 它是谁 + 怎么写）
        system_prompt = (
            f"{track_instruction}\n"     # "你是一个美食探店博主..."
            f"{style_instruction}\n"     # "使用复古怀旧的语气..."
            f"{emoji_instruction}\n"     # "自然地使用 emoji"
            f"{subtitle_instruction}\n"  # "每个段落加小标题"
            "严格按照指定字数限制和JSON格式输出。"
        )

        # 2. 组装 User Prompt（告诉 AI 具体写什么）
        user_prompt = (
            "请根据以下信息生成一篇小红书风格的文章，严格按照JSON格式返回。\n"
            "字数要求：标题18-20字，前言约50字，3个正文段落每段约100字，总结约50字，店铺信息约50字。\n"
            f"关键词/主题：{keywords}"
        )

        # 3. 调用 DeepSeek
        response = await self.client.chat.completions.create(
            model=settings.LLM_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            response_format={"type": "json_object"},  # ← 告诉 AI 返回 JSON
            temperature=0.8,  # ← 0=严谨, 1=创意
        )

        # 4. 解析返回的 JSON
        raw = response.choices[0].message.content
        article = json.loads(raw)
        return article
```

**关键参数理解：**
- `temperature=0.8`：稍高的创意度，适合写作文案。0 的话每次输出几乎一样
- `response_format={"type": "json_object"}`：强制 AI 返回 JSON，方便程序解析

---

### 步骤 5：读懂生成接口 `routers/generate.py`

```python
@router.post("/generate", response_model=GenerateResponse)
async def generate_article(
    keywords: str = Form(default=""),
    style_template: str = Form(default="简约风"),
    track: str = Form(default="美食"),
    emoji_enabled: bool = Form(default=True),
    subtitles_enabled: bool = Form(default=True),
    store_name: str = Form(default=""),
    store_address: str = Form(default=""),
    image: UploadFile | None = File(None),
    user: User = Depends(get_current_user),  # ← 必须登录
    db: Session = Depends(get_db),
):
    # 检查点数
    if user.credits_remaining <= 0:
        raise HTTPException(status_code=402, detail="Insufficient credits.")

    generator = ArticleGenerator()
    result = await generator.generate(...)

    # 扣一点数
    user.credits_remaining -= 1
    db.commit()

    return GenerateResponse(**result)
```

---

### 步骤 6：用 Swagger 测试生成

**操作：**
1. 确保后端已启动
2. 打开 http://localhost:8000/docs
3. 先点 "Authorize" 输入 Bearer token（从 Lab04 的登录结果里拿）
4. 找到 `POST /api/generate`
5. 填参数：
   - `keywords`: `咖啡店 brunch 推荐`
   - `style_template`: `幽默风`
   - `track`: `美食`
   - 其他保持默认
6. 点 "Execute"
7. 看返回结果：

```json
{
  "title": "这家咖啡店的brunch绝了！一口沦陷",
  "intro": "周末睡到自然醒，最需要的就是一顿完美的brunch...",
  "sections": [
    {
      "subtitle": "✨ 必点招牌",
      "content": "班尼迪克蛋简直封神！流心蛋黄配上..."
    },
    ...
  ],
  "summary": "这家店绝对是周末约会的好去处...",
  "store_info": "📍 Coffee Lab 咖啡实验室\n📍 朝阳区望京SOHO...",
  "tokens_used": 856
}
```

**验证：** 返回了完整的小红书风格文章 ✅

---

### 步骤 6：试不同风格

用同样的关键词，切换不同风格：
- `复古风` → 语气像"那时候的上海..."
- `幽默风` → 语气像"笑不活了家人们..."
- `深度测评风` → 语气像"经过 7 天实测..."

**你会发现同一个主题，不同风格出来的文章完全不同！**

**验证：** 至少测试两种风格，感受 Prompt 的魔力 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| API Key 报 401 | `Incorrect API key` | Key 没配或配错了 | 检查 `.env` 文件的 `OPENAI_API_KEY` |
| DeepSeek 余额不足 | `Insufficient balance` | 免费额度用完了 | 去 DeepSeek 平台充值（很便宜） |
| 网络超时 | 请求一直转圈 | DeepSeek 服务器响应慢 | 等几秒重试，或者检查网络 |
| JSON 解析失败 | `json.loads` 报错 | AI 返回的不是合法 JSON | 检查 `response_format` 是否正确设置 |

---

## 📝 总结

**本章核心要点：**
- AI 接口调用 = OpenAI SDK + DeepSeek base URL + API Key
- Prompt 分两层：System（设定角色）+ User（具体任务）
- `temperature` 控制创意度，`response_format` 强制 JSON 输出
- 每次生成扣 1 个点数

**你现在应该能做到：**
- 独立获取和配置 DeepSeek API Key
- 用 Swagger 测试 AI 生成
- 说出 System Prompt 和 User Prompt 的区别

**下一步：** Lab 07 我们把前端生成页面做完整——图片上传 + 风格选择 + 一键生成。

---

> —— 阿珊，前端开发者 & AI 提效实践者
