from app.database import engine, Base
from app.config import settings

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import auth, generate, styles, viral, billing, batch

app = FastAPI(title="RedNote Express API", version="0.1.0")
# 让 FastAPI 每次启动时，自动扫描并创建所有缺失的表
Base.metadata.create_all(bind=engine)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router, prefix="/api/auth", tags=["auth"])
app.include_router(generate.router, prefix="/api", tags=["generate"])
app.include_router(styles.router, prefix="/api", tags=["styles"])
app.include_router(viral.router, prefix="/api", tags=["viral"])
app.include_router(billing.router, prefix="/api", tags=["billing"])
app.include_router(batch.router, prefix="/api", tags=["batch"])


@app.get("/api/health")
def health():
    return {"status": "ok"}
