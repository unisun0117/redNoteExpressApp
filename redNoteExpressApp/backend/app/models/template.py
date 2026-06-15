import uuid
from sqlalchemy import String, Boolean, JSON
from sqlalchemy.orm import Mapped, mapped_column
from app.database import Base


class Template(Base):
    __tablename__ = "templates"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    style_config: Mapped[dict] = mapped_column(JSON, nullable=False, default=dict)
    is_preset: Mapped[bool] = mapped_column(Boolean, default=True)
    is_vip_only: Mapped[bool] = mapped_column(Boolean, default=False)
