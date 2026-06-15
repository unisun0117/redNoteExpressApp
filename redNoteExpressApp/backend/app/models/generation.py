import uuid
from datetime import datetime
from sqlalchemy import String, Integer, DateTime, JSON, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from app.database import Base


class Generation(Base):
    __tablename__ = "generations"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id"), nullable=False)
    input_images: Mapped[list | None] = mapped_column(JSON, nullable=True)
    keywords: Mapped[str | None] = mapped_column(String(500), nullable=True)
    style_config: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    generated_article: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    viral_source_url: Mapped[str | None] = mapped_column(String(2000), nullable=True)
    tokens_used: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
