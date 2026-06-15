from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.database import get_db
from app.models.user import User
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
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
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

    user.credits_remaining -= 1
    db.commit()

    return GenerateResponse(**result)
