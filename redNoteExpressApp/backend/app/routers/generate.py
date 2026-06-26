from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.database import get_db
from app.models.user import User
from app.models.generation import Generation
from app.middleware.auth import get_current_user
from app.services.generator import ArticleGenerator

router = APIRouter()


class GenerateResponse(BaseModel):
    id: str
    title: str
    intro: str
    sections: list[dict]
    summary: str
    store_info: str
    tokens_used: int


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
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 把 user 合并到当前会话（解决跨会话问题）
    user = db.merge(current_user)

    if user.credits_remaining <= 0:
        raise HTTPException(status_code=402, detail="Insufficient credits. Please recharge.")

    generator = ArticleGenerator()
    image_data = await image.read() if image else None

    result = await generator.generate(
        image_data=image_data,
        keywords=keywords,
        style_template=style_template,
        track=track,
        emoji_enabled=emoji_enabled,
        subtitles_enabled=subtitles_enabled,
        store_name=store_name,
        store_address=store_address,
    )

    # 扣点数
    user.credits_remaining -= 1

    # 保存生成记录
    generation = Generation(
        user_id=user.id,
        keywords=keywords,
        style_config={
            "template": style_template,
            "track": track,
            "emoji_enabled": emoji_enabled,
            "subtitles_enabled": subtitles_enabled,
        },
        generated_article={
            "title": result.get("title", ""),
            "intro": result.get("intro", ""),
            "sections": result.get("sections", []),
            "summary": result.get("summary", ""),
            "store_info": result.get("store_info", ""),
        },
        tokens_used=result.get("tokens_used", 0),
    )
    db.add(generation)
    db.commit()

    return GenerateResponse(**result)


class HistoryItem(BaseModel):
    id: str
    keywords: str | None
    style_config: dict | None
    generated_article: dict | None
    tokens_used: int
    created_at: str


class HistoryResponse(BaseModel):
    items: list[HistoryItem]
    total: int


@router.get("/generations", response_model=HistoryResponse)
def get_generations(
    skip: int = 0,
    limit: int = 20,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    total = db.query(Generation).filter(Generation.user_id == user.id).count()
    items = (
        db.query(Generation)
        .filter(Generation.user_id == user.id)
        .order_by(Generation.created_at.desc())
        .offset(skip)
        .limit(limit)
        .all()
    )
    return HistoryResponse(
        items=[
            HistoryItem(
                id=g.id,
                keywords=g.keywords,
                style_config=g.style_config,
                generated_article=g.generated_article,
                tokens_used=g.tokens_used,
                created_at=g.created_at.isoformat() if g.created_at else "",
            )
            for g in items
        ],
        total=total,
    )
