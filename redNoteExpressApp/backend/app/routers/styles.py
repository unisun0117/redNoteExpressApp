from fastapi import APIRouter

router = APIRouter()

STYLE_TEMPLATES = [
    {"id": "retro", "name": "复古风", "description": "Vintage, nostalgic tone with classical phrasing"},
    {"id": "minimalist", "name": "简约风", "description": "Clean, understated, and elegant"},
    {"id": "humorous", "name": "幽默风", "description": "Witty, playful, and entertaining"},
    {"id": "deep-review", "name": "深度测评风", "description": "In-depth review style with detailed analysis"},
]

TRACKS = [
    {"id": "food", "name": "美食"},
    {"id": "sports", "name": "运动"},
    {"id": "photography", "name": "摄影"},
    {"id": "pets", "name": "萌宠"},
    {"id": "home", "name": "家居"},
    {"id": "beauty", "name": "美妆"},
    {"id": "tech", "name": "数码"},
    {"id": "baby", "name": "母婴"},
    {"id": "dining", "name": "餐饮"},
]


@router.get("/styles")
def get_styles():
    return {"templates": STYLE_TEMPLATES, "tracks": TRACKS}
