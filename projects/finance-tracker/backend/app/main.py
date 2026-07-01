import os
os.environ.setdefault("NO_PROXY", "*")

from app.database import engine, Base
from app.config import settings

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import auth, transactions, budgets, stats, profile

app = FastAPI(title="金橘记账 API", version="0.1.0")

Base.metadata.create_all(bind=engine)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS.split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router, prefix="/api/auth", tags=["auth"])
app.include_router(transactions.router, prefix="/api", tags=["transactions"])
app.include_router(budgets.router, prefix="/api", tags=["budgets"])
app.include_router(stats.router, prefix="/api", tags=["stats"])
app.include_router(profile.router, prefix="/api", tags=["profile"])


@app.get("/api/health")
def health():
    return {"status": "ok"}
