import { useState, useEffect } from "react";
import { api } from "../services/api";
import { useAuth } from "../hooks/useAuth";

export function BillingPage() {
  const { user } = useAuth();
  const [packages, setPackages] = useState<any>({ credit_packages: [], vip_plans: [] });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.getPackages().then((data) => {
      if (data.credit_packages) setPackages(data);
    }).catch(() => {});
  }, []);

  const handleRecharge = async (pkgId: string) => {
    setLoading(true);
    try {
      await api.recharge(pkgId);
      alert("充值成功！请刷新页面查看");
    } catch {
      alert("充值失败");
    } finally {
      setLoading(false);
    }
  };

  const handleSubscribe = async (planId: string) => {
    setLoading(true);
    try {
      await api.subscribe(planId);
      alert("订阅成功！请刷新页面查看");
    } catch {
      alert("订阅失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">充值与升级</h1>

      <div className="card">
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>💎 次数充值</h2>
        {packages.credit_packages?.map((pkg: any) => (
          <div
            key={pkg.id}
            style={{
              display: "flex", justifyContent: "space-between", alignItems: "center",
              padding: "12px 0", borderBottom: "1px solid #f0f0f0",
            }}
          >
            <div>
              <div style={{ fontWeight: 600, fontSize: 15 }}>{pkg.name}</div>
              <div style={{ fontSize: 13, color: "#999" }}>¥{pkg.price}</div>
            </div>
            <button
              className="btn btn-primary"
              style={{ width: "auto", padding: "8px 16px", fontSize: 14 }}
              onClick={() => handleRecharge(pkg.id)}
              disabled={loading}
            >
              购买
            </button>
          </div>
        ))}
      </div>

      <div className="card">
        <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>👑 VIP 订阅</h2>
        {packages.vip_plans?.map((plan: any) => (
          <div
            key={plan.id}
            style={{
              display: "flex", justifyContent: "space-between", alignItems: "center",
              padding: "12px 0", borderBottom: "1px solid #f0f0f0",
            }}
          >
            <div>
              <div style={{ fontWeight: 600, fontSize: 15 }}>{plan.name}</div>
              <div style={{ fontSize: 13, color: "#999" }}>
                ¥{plan.price} / {plan.duration_days}天
              </div>
            </div>
            <button
              className="btn btn-primary"
              style={{ width: "auto", padding: "8px 16px", fontSize: 14 }}
              onClick={() => handleSubscribe(plan.id)}
              disabled={loading || user?.tier === "vip"}
            >
              {user?.tier === "vip" ? "已订阅" : "订阅"}
            </button>
          </div>
        ))}
        {user?.tier === "vip" && (
          <div style={{ fontSize: 13, color: "#ee5a24", marginTop: 8 }}>
            ✅ 您已是VIP会员，享受所有高级功能
          </div>
        )}
      </div>
    </div>
  );
}
