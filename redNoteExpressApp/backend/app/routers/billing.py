from fastapi import APIRouter, Depends
from pydantic import BaseModel
from app.models.user import User
from app.middleware.auth import get_current_user
from sqlalchemy.orm import Session
from app.database import get_db

router = APIRouter()

CREDIT_PACKAGES = [
    {"id": "credits-100", "name": "100 credits", "price": 9.9, "credits": 100},
]

VIP_PLANS = [
    {"id": "vip-monthly", "name": "VIP Monthly", "price": 29.9, "duration_days": 30},
    {"id": "vip-quarterly", "name": "VIP Quarterly", "price": 79.9, "duration_days": 90},
]


class RechargeRequest(BaseModel):
    package_id: str


class SubscribeRequest(BaseModel):
    plan_id: str


@router.get("/billing/packages")
def get_packages():
    return {"credit_packages": CREDIT_PACKAGES, "vip_plans": VIP_PLANS}


@router.post("/billing/recharge")
def recharge(req: RechargeRequest, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    # Stub: payment integration in later tasks
    pkg = next((p for p in CREDIT_PACKAGES if p["id"] == req.package_id), None)
    if not pkg:
        return {"error": "Invalid package"}
    user.credits_remaining += pkg["credits"]
    db.commit()
    return {"success": True, "credits": user.credits_remaining}


@router.post("/billing/subscribe")
def subscribe(req: SubscribeRequest, user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    plan = next((p for p in VIP_PLANS if p["id"] == req.plan_id), None)
    if not plan:
        return {"error": "Invalid plan"}
    from app.models.user import UserTier
    user.tier = UserTier.VIP
    db.commit()
    return {"success": True, "tier": user.tier.value}
