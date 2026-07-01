from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from datetime import date
from app.database import get_db
from app.models.user import User
from app.models.transaction import Transaction
from app.middleware.auth import get_current_user

router = APIRouter()

CATEGORIES = {
    "expense": [
        {"name": "餐饮", "emoji": "🍽️"},
        {"name": "交通", "emoji": "🚇"},
        {"name": "购物", "emoji": "🛍️"},
        {"name": "娱乐", "emoji": "🎬"},
        {"name": "住房", "emoji": "🏠"},
        {"name": "医疗", "emoji": "💊"},
        {"name": "教育", "emoji": "📚"},
        {"name": "其他", "emoji": "📌"},
    ],
    "income": [
        {"name": "工资", "emoji": "💰"},
        {"name": "红包", "emoji": "🧧"},
        {"name": "理财", "emoji": "📈"},
        {"name": "礼物", "emoji": "🎁"},
        {"name": "兼职", "emoji": "💼"},
        {"name": "退款", "emoji": "↩️"},
        {"name": "其他", "emoji": "📌"},
    ],
}


class TxCreate(BaseModel):
    type: str  # "expense" | "income"
    amount: int  # 单位：分
    category: str
    note: str | None = None
    tx_date: date


class TxResponse(BaseModel):
    id: str
    type: str
    amount: int
    category: str
    category_emoji: str | None
    note: str | None
    tx_date: str
    created_at: str


@router.get("/categories")
def get_categories():
    return CATEGORIES


@router.get("/transactions")
def list_transactions(
    month: str | None = None,
    category: str | None = None,
    type: str | None = None,
    q: str | None = None,
    page: int = 1,
    page_size: int = 50,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    query = db.query(Transaction).filter(Transaction.user_id == user.id)

    if month:
        query = query.filter(Transaction.tx_date.like(f"{month}%"))
    if category:
        query = query.filter(Transaction.category == category)
    if type:
        query = query.filter(Transaction.type == type)
    if q:
        query = query.filter(Transaction.note.contains(q))

    total = query.count()
    items = (
        query.order_by(Transaction.tx_date.desc(), Transaction.created_at.desc())
        .offset((page - 1) * page_size)
        .limit(page_size)
        .all()
    )

    # 计算汇总
    income_sum = db.query(Transaction).filter(
        Transaction.user_id == user.id,
        Transaction.type == "income",
    )
    expense_sum = db.query(Transaction).filter(
        Transaction.user_id == user.id,
        Transaction.type == "expense",
    )
    if month:
        income_sum = income_sum.filter(Transaction.tx_date.like(f"{month}%"))
        expense_sum = expense_sum.filter(Transaction.tx_date.like(f"{month}%"))

    return {
        "items": [
            {
                "id": t.id,
                "type": t.type,
                "amount": t.amount,
                "category": t.category,
                "category_emoji": t.category_emoji,
                "note": t.note,
                "tx_date": t.tx_date.isoformat() if t.tx_date else "",
                "created_at": t.created_at.isoformat() if t.created_at else "",
            }
            for t in items
        ],
        "total": total,
        "summary": {
            "income": sum(r.amount for r in income_sum.all()),
            "expense": sum(r.amount for r in expense_sum.all()),
        },
    }


@router.post("/transactions")
def create_transaction(
    req: TxCreate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 查找分类emoji
    cats = CATEGORIES.get(req.type, [])
    emoji = next((c["emoji"] for c in cats if c["name"] == req.category), "📌")

    tx = Transaction(
        user_id=user.id,
        type=req.type,
        amount=req.amount,
        category=req.category,
        category_emoji=emoji,
        note=req.note,
        tx_date=req.tx_date,
    )
    db.add(tx)
    db.commit()
    db.refresh(tx)
    return {
        "id": tx.id,
        "type": tx.type,
        "amount": tx.amount,
        "category": tx.category,
        "category_emoji": tx.category_emoji,
        "note": tx.note,
        "tx_date": tx.tx_date.isoformat() if tx.tx_date else "",
        "created_at": tx.created_at.isoformat() if tx.created_at else "",
    }


@router.put("/transactions/{tx_id}")
def update_transaction(
    tx_id: str,
    req: TxCreate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    tx = db.query(Transaction).filter(Transaction.id == tx_id, Transaction.user_id == user.id).first()
    if not tx:
        raise HTTPException(status_code=404, detail="Transaction not found")

    tx.type = req.type
    tx.amount = req.amount
    tx.category = req.category
    tx.note = req.note
    tx.tx_date = req.tx_date
    cats = CATEGORIES.get(req.type, [])
    tx.category_emoji = next((c["emoji"] for c in cats if c["name"] == req.category), "📌")
    db.commit()
    return {"ok": True}


@router.delete("/transactions/{tx_id}")
def delete_transaction(
    tx_id: str,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    tx = db.query(Transaction).filter(Transaction.id == tx_id, Transaction.user_id == user.id).first()
    if not tx:
        raise HTTPException(status_code=404, detail="Transaction not found")
    db.delete(tx)
    db.commit()
    return {"ok": True}
