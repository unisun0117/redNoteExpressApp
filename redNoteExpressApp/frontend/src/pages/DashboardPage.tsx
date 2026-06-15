import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  return (
    <div className="page">
      <h1 className="page-title">我的</h1>

      <div className="card">
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
          <div style={{
            width: 48, height: 48, borderRadius: 24,
            background: "linear-gradient(135deg, #ff6b6b, #ee5a24)",
            display: "flex", alignItems: "center", justifyContent: "center",
            color: "#fff", fontSize: 20, fontWeight: 700,
          }}>
            {user.email?.charAt(0).toUpperCase() || "U"}
          </div>
          <div>
            <div style={{ fontWeight: 600, fontSize: 16 }}>{user.email}</div>
            <div style={{ fontSize: 13, color: "#999" }}>
              {user.tier === "vip" ? "🌟 VIP会员" : "免费用户"}
            </div>
          </div>
        </div>

        <div style={{ display: "flex", gap: 12 }}>
          <div style={{
            flex: 1, background: "#f9f9f9", borderRadius: 8,
            padding: 12, textAlign: "center",
          }}>
            <div style={{ fontSize: 24, fontWeight: 700, color: "#ee5a24" }}>
              {user.credits_remaining}
            </div>
            <div style={{ fontSize: 12, color: "#999" }}>剩余次数</div>
          </div>
          <div style={{
            flex: 1, background: "#f9f9f9", borderRadius: 8,
            padding: 12, textAlign: "center",
          }}>
            <div style={{ fontSize: 24, fontWeight: 700, color: "#ee5a24" }}>
              {user.tier === "vip" ? "VIP" : "Free"}
            </div>
            <div style={{ fontSize: 12, color: "#999" }}>当前等级</div>
          </div>
        </div>
      </div>

      <button className="btn btn-outline" style={{ marginTop: 8 }} onClick={() => navigate("/billing")}>
        💰 充值与升级
      </button>

      <button className="btn btn-outline" style={{ marginTop: 8, color: "#e74c3c" }} onClick={logout}>
        退出登录
      </button>
    </div>
  );
}
