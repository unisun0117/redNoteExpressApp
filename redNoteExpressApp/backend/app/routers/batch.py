from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.database import get_db
from app.models.user import User
from app.middleware.auth import get_current_user, require_vip

router = APIRouter()


class BatchProgressResponse(BaseModel):
    batch_id: str
    total: int
    completed: int
    results: list[dict]
    eta_seconds: int


@router.post("/batch/generate")
async def batch_generate(
    keywords: str = Form(default=""),
    style_template: str = Form(default="简约风"),
    track: str = Form(default="美食"),
    emoji_enabled: bool = Form(default=True),
    subtitles_enabled: bool = Form(default=True),
    store_name: str = Form(default=""),
    store_address: str = Form(default=""),
    images: list[UploadFile] = File(...),
    user: User = Depends(require_vip),
    db: Session = Depends(get_db),
):
    if len(images) > 50:
        raise HTTPException(status_code=400, detail="Maximum 50 images per batch")
    # Stub: batch generation implemented in task 9.x
    return {"batch_id": "stub", "total": len(images), "status": "queued"}


@router.get("/batch/{batch_id}/progress", response_model=BatchProgressResponse)
def batch_progress(batch_id: str, user: User = Depends(require_vip)):
    return BatchProgressResponse(
        batch_id=batch_id, total=0, completed=0, results=[], eta_seconds=0
    )
