from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, date, timedelta
from app.database import get_db
from app.models.user import User
from app.models.transaction import Transaction
from app.middleware.auth import get_current_user

router = APIRouter()


@router.get("/dashboard")
def get_dashboard(
    month: str | None = None,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not month:
        month = datetime.utcnow().strftime("%Y-%m")

    # 当月收支
    income = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.type == "income",
        Transaction.tx_date.like(f"{month}%"),
    ).scalar() or 0

    expense = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.type == "expense",
        Transaction.tx_date.like(f"{month}%"),
    ).scalar() or 0

    balance = income - expense

    # 最近记录
    recent = (
        db.query(Transaction)
        .filter(Transaction.user_id == user.id)
        .order_by(Transaction.tx_date.desc(), Transaction.created_at.desc())
        .limit(5)
        .all()
    )

    return {
        "month": month,
        "balance": balance,
        "month_income": income,
        "month_expense": expense,
        "recent_transactions": [
            {
                "id": t.id,
                "type": t.type,
                "amount": t.amount,
                "category": t.category,
                "category_emoji": t.category_emoji,
                "note": t.note,
                "tx_date": t.tx_date.isoformat() if t.tx_date else "",
            }
            for t in recent
        ],
    }


@router.get("/stats/trend")
def get_trend(
    month: str | None = None,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not month:
        month = datetime.utcnow().strftime("%Y-%m")

    # 最近7天
    today = date.today()
    daily = []
    total = 0
    for i in range(6, -1, -1):
        d = today - timedelta(days=i)
        day_expense = db.query(func.sum(Transaction.amount)).filter(
            Transaction.user_id == user.id,
            Transaction.type == "expense",
            Transaction.tx_date == d,
        ).scalar() or 0
        daily.append({"date": d.isoformat(), "amount": day_expense})
        total += day_expense

    daily_avg = total // 7 if total > 0 else 0

    return {"daily": daily, "daily_avg": daily_avg}


@router.get("/stats/category")
def get_category_stats(
    month: str | None = None,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not month:
        month = datetime.utcnow().strftime("%Y-%m")

    expense_total = db.query(func.sum(Transaction.amount)).filter(
        Transaction.user_id == user.id,
        Transaction.type == "expense",
        Transaction.tx_date.like(f"{month}%"),
    ).scalar() or 0

    results = (
        db.query(Transaction.category, Transaction.category_emoji, func.sum(Transaction.amount).label("total"))
        .filter(
            Transaction.user_id == user.id,
            Transaction.type == "expense",
            Transaction.tx_date.like(f"{month}%"),
        )
        .group_by(Transaction.category)
        .order_by(func.sum(Transaction.amount).desc())
        .all()
    )

    categories = []
    for r in results:
        pct = round(r.total / expense_total * 100) if expense_total > 0 else 0
        categories.append({
            "name": r.category,
            "emoji": r.category_emoji or "📌",
            "amount": r.total,
            "percentage": pct,
        })

    return {"categories": categories}
