from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from app.models.user import User
from app.middleware.auth import get_current_user, require_vip

router = APIRouter()


class ViralAnalyzeRequest(BaseModel):
    url: str | None = None
    text: str | None = None


class ViralStyleProfile(BaseModel):
    tone: str
    avg_sentence_length: str
    emoji_density: str
    hook_patterns: list[str]
    characteristic_phrases: list[str]


@router.post("/viral/analyze", response_model=ViralStyleProfile)
async def analyze_viral_article(
    req: ViralAnalyzeRequest,
    user: User = Depends(require_vip),
):
    if not req.url and not req.text:
        raise HTTPException(status_code=400, detail="Provide either URL or text")
    # Stub: implemented in task 6.x
    return ViralStyleProfile(
        tone="casual",
        avg_sentence_length="short",
        emoji_density="high",
        hook_patterns=["question opening", "exclamation"],
        characteristic_phrases=["绝绝子", "冲"],
    )
