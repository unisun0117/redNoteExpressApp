from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from pydantic import BaseModel
from app.database import get_db
from app.models.user import User
from app.models.transaction import Transaction
from app.middleware.auth import get_current_user

router = APIRouter()


class ProfileUpdate(BaseModel):
    nickname: str | None = None
    avatar: str | None = None


@router.get("/user/profile")
def get_profile(
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    total_count = db.query(func.count(Transaction.id)).filter(
        Transaction.user_id == user.id
    ).scalar() or 0

    total_income = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.type == "income",
    ).scalar() or 0

    total_expense = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.type == "expense",
    ).scalar() or 0

    return {
        "id": user.id,
        "email": user.email,
        "nickname": user.nickname or user.email.split("@")[0],
        "avatar": user.avatar or "😊",
        "created_at": user.created_at.isoformat() if user.created_at else "",
        "total_count": total_count,
        "total_income": total_income,
        "total_expense": total_expense,
    }


@router.put("/user/profile")
def update_profile(
    req: ProfileUpdate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if req.nickname is not None:
        user.nickname = req.nickname
    if req.avatar is not None:
        user.avatar = req.avatar
    db.commit()
    return {"ok": True}
