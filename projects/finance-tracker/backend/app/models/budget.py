import uuid
from datetime import datetime
from sqlalchemy import String, Integer, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.database import Base


class Budget(Base):
    __tablename__ = "budgets"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id"), nullable=False)
    month: Mapped[str] = mapped_column(String(7), nullable=False)  # "2026-06"
    category: Mapped[str] = mapped_column(String(50), nullable=False)  # "餐饮" | "__total__"
    category_emoji: Mapped[str | None] = mapped_column(String(10), nullable=True)
    amount: Mapped[int] = mapped_column(Integer, nullable=False)  # 单位：分
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
