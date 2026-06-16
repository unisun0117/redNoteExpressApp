import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    DATABASE_URL: str = "sqlite:///./rednote.db"
    JWT_SECRET: str = "dev-secret-change-in-production"
    JWT_ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 15
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    OPENAI_API_KEY: str = ""
    ANTHROPIC_API_KEY: str = ""
    LLM_PROVIDER: str = "openai"
    LLM_MODEL: str = "deepseek-chat"
    LLM_BASE_URL: str = "https://api.deepseek.com"
    VISION_MODEL: str = "gpt-4o"
    FREE_CREDITS: int = 10
    FREE_RATE_LIMIT: str = "10/hour"
    VIP_RATE_LIMIT: str = "100/hour"
    IMAGE_STORAGE_PATH: str = "./uploads"
    MAX_IMAGE_SIZE_MB: int = 10
    ALLOWED_IMAGE_TYPES: list[str] = ["image/jpeg", "image/png", "image/webp"]
    MAX_BATCH_IMAGES: int = 50
    WECHAT_APP_ID: str = ""
    WECHAT_APP_SECRET: str = ""
    REDIS_URL: str = "redis://localhost:6379/0"
    ALLOWED_ORIGINS: str = "http://localhost:5173"

    class Config:
        env_file = ".env"


settings = Settings()
