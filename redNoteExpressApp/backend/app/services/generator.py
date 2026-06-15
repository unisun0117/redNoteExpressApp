import json
from openai import AsyncOpenAI
from app.config import settings

STYLE_PROMPTS = {
    "复古风": "使用复古怀旧的语气，加入经典词汇和年代感表达，营造温暖怀旧的氛围。",
    "简约风": "使用简洁优雅的语气，避免冗长修饰，突出核心信息，风格清新自然。",
    "幽默风": "使用俏皮幽默的语气，加入网络流行语和搞笑表达，让读者会心一笑。",
    "深度测评风": "使用专业细致的分析语气，多维度评测，注重细节和对比，给人可信赖感。",
}

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


class ArticleGenerator:
    def __init__(self):
        self.client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)

    async def generate(
        self,
        image_data: bytes | None,
        keywords: str,
        style_template: str,
        track: str,
        emoji_enabled: bool,
        subtitles_enabled: bool,
        store_name: str,
        store_address: str,
    ) -> dict:
        style_instruction = STYLE_PROMPTS.get(style_template, STYLE_PROMPTS["简约风"])
        track_instruction = TRACK_PROMPTS.get(track, TRACK_PROMPTS["美食"])

        emoji_instruction = "在正文中自然地穿插使用emoji表情符号，增加生动感。" if emoji_enabled else "不使用任何emoji表情符号。"

        subtitle_instruction = "每个正文段落前加一个吸引眼球的爆款小标题。" if subtitles_enabled else "正文段落不需要单独的小标题。"

        system_prompt = (
            f"{track_instruction}\n"
            f"{style_instruction}\n"
            f"{emoji_instruction}\n"
            f"{subtitle_instruction}\n"
            "严格按照指定字数限制和JSON格式输出。"
        )

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": self._build_user_prompt(keywords, store_name, store_address)},
        ]

        if image_data:
            import base64
            img_b64 = base64.b64encode(image_data).decode("utf-8")
            messages[1]["content"] = [
                {"type": "text", "text": self._build_user_prompt(keywords, store_name, store_address)},
                {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{img_b64}"}},
            ]

        response = await self.client.chat.completions.create(
            model=settings.LLM_MODEL,
            messages=messages,
            response_format={"type": "json_object"},
            temperature=0.8,
        )

        raw = response.choices[0].message.content
        article = json.loads(raw)

        return {
            "id": "",
            "title": article.get("title", ""),
            "intro": article.get("intro", ""),
            "sections": article.get("sections", []),
            "summary": article.get("summary", ""),
            "store_info": article.get("store_info", ""),
            "tokens_used": response.usage.total_tokens if response.usage else 0,
        }

    def _build_user_prompt(self, keywords: str, store_name: str, store_address: str) -> str:
        prompt = (
            "请根据以下信息生成一篇小红书风格的文章，严格按照JSON格式返回。\n"
            "字数要求：标题18-20字，前言约50字，3个正文段落每段约100字，总结约50字，店铺信息约50字。\n"
            "JSON格式：{\"title\": \"标题\", \"intro\": \"前言\", "
            "\"sections\": [{\"subtitle\": \"小标题\", \"content\": \"内容\"}, ...], "
            "\"summary\": \"总结\", \"store_info\": \"店铺信息\"}\n"
        )
        if keywords:
            prompt += f"\n关键词/主题：{keywords}"
        if store_name:
            prompt += f"\n店铺名称：{store_name}"
        if store_address:
            prompt += f"\n店铺地址：{store_address}"
        return prompt
