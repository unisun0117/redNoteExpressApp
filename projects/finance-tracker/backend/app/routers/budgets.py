from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from pydantic import BaseModel
from datetime import datetime
from app.database import get_db
from app.models.user import User
from app.models.budget import Budget
from app.models.transaction import Transaction
from app.middleware.auth import get_current_user

router = APIRouter()


class BudgetItem(BaseModel):
    category: str
    category_emoji: str | None = None
    amount: int  # 分


class BudgetUpdate(BaseModel):
    month: str  # "2026-06"
    total_budget: int
    categories: list[BudgetItem]


@router.get("/budgets")
def get_budgets(
    month: str | None = None,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if not month:
        month = datetime.utcnow().strftime("%Y-%m")

    budgets = db.query(Budget).filter(
        Budget.user_id == user.id,
        Budget.month == month,
    ).all()

    total_budget = next((b for b in budgets if b.category == "__total__"), None)
    cat_budgets = [b for b in budgets if b.category != "__total__"]

    # 计算每个分类的实际支出
    categories = []
    total_spent = 0
    for b in cat_budgets:
        spent = (
            db.query(Transaction.amount)
            .filter(
                Transaction.user_id == user.id,
                Transaction.type == "expense",
                Transaction.category == b.category,
                Transaction.tx_date.like(f"{month}%"),
            )
            .all()
        )
        spent_sum = sum(r[0] for r in spent)
        total_spent += spent_sum
        categories.append({
            "category": b.category,
            "emoji": b.category_emoji,
            "budget": b.amount,
            "spent": spent_sum,
        })

    return {
        "month": month,
        "total_budget": total_budget.amount if total_budget else 0,
        "total_spent": total_spent,
        "categories": categories,
    }


@router.put("/budgets")
def update_budgets(
    req: BudgetUpdate,
    user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # 删除该月旧预算
    db.query(Budget).filter(
        Budget.user_id == user.id,
        Budget.month == req.month,
    ).delete()

    # 插入总预算
    db.add(Budget(
        user_id=user.id,
        month=req.month,
        category="__total__",
        category_emoji=None,
        amount=req.total_budget,
    ))

    # 插入分类预算
    for c in req.categories:
        db.add(Budget(
            user_id=user.id,
            month=req.month,
            category=c.category,
            category_emoji=c.category_emoji,
            amount=c.amount,
        ))

    db.commit()
    return {"ok": True}
