import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { api } from "../services/api";

export function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<any>(null);

  useEffect(() => {
    api.getProfile().then(setProfile);
  }, []);

  const fmt = (v: number) => (v / 100).toFixed(2);
  const menuStyle: React.CSSProperties = {
    display: "flex", alignItems: "center", gap: 12, padding: "14px 16px",
    cursor: "pointer", background: "none", border: "none", width: "100%",
    fontSize: 15, color: "#1F2937",
  };

  return (
    <div style={{ padding: "24px 16px", background: "#FFF8F0", minHeight: "100vh" }}>
      <h1 style={{ fontSize: 20, color: "#1F2937", marginBottom: 20 }}>👤 我的</h1>

      {/* 个人信息卡 */}
      <div style={{ background: "#fff", borderRadius: 16, padding: 24, display: "flex", alignItems: "center", gap: 16, marginBottom: 16, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
        <div style={{ width: 56, height: 56, borderRadius: "50%", background: "linear-gradient(135deg, #FEF3C7, #FDE68A)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 28 }}>{user?.avatar || "😊"}</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 18, fontWeight: 600 }}>{user?.nickname || user?.email?.split("@")[0]}</div>
          <div style={{ fontSize: 13, color: "#9CA3AF" }}>{user?.email}</div>
          {profile && <div style={{ fontSize: 12, color: "#D1D5DB", marginTop: 2 }}>已记账 {profile.total_count} 天</div>}
        </div>
        <span style={{ color: "#D1D5DB" }}>›</span>
      </div>

      {/* 数据概览 */}
      {profile && (
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 8, marginBottom: 16 }}>
          <div style={{ background: "#fff", borderRadius: 12, padding: 16, textAlign: "center", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
            <div style={{ fontSize: 20, fontWeight: 700 }}>{profile.total_count}</div>
            <div style={{ fontSize: 11, color: "#9CA3AF", marginTop: 4 }}>总记账笔数</div>
          </div>
          <div style={{ background: "#fff", borderRadius: 12, padding: 16, textAlign: "center", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
            <div style={{ fontSize: 20, fontWeight: 700, color: "#10B981" }}>¥{fmt(profile.total_income)}</div>
            <div style={{ fontSize: 11, color: "#9CA3AF", marginTop: 4 }}>累计收入</div>
          </div>
          <div style={{ background: "#fff", borderRadius: 12, padding: 16, textAlign: "center", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
            <div style={{ fontSize: 20, fontWeight: 700, color: "#EF4444" }}>¥{fmt(profile.total_expense)}</div>
            <div style={{ fontSize: 11, color: "#9CA3AF", marginTop: 4 }}>累计支出</div>
          </div>
        </div>
      )}

      {/* 菜单 */}
      <div style={{ background: "#fff", borderRadius: 16, overflow: "hidden", marginBottom: 12, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
        <div style={{ fontSize: 12, color: "#9CA3AF", padding: "14px 16px 6px", letterSpacing: 1 }}>数据管理</div>
        <button onClick={() => navigate("/list")} style={menuStyle}><span style={{ fontSize: 20, width: 28 }}>📋</span><span style={{ flex: 1 }}>全部账单</span><span style={{ color: "#D1D5DB" }}>›</span></button>
        <button onClick={() => alert("导出功能开发中")} style={menuStyle}><span style={{ fontSize: 20, width: 28 }}>📥</span><span style={{ flex: 1 }}>导出数据</span><span style={{ fontSize: 11, background: "#FEF3C7", color: "#F59E0B", padding: "2px 8px", borderRadius: 10 }}>CSV</span><span style={{ color: "#D1D5DB" }}>›</span></button>
      </div>

      <div style={{ background: "#fff", borderRadius: 16, overflow: "hidden", marginBottom: 12, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
        <div style={{ fontSize: 12, color: "#9CA3AF", padding: "14px 16px 6px", letterSpacing: 1 }}>设置</div>
        <button onClick={() => alert("编辑资料功能开发中")} style={menuStyle}><span style={{ fontSize: 20, width: 28 }}>✏️</span><span style={{ flex: 1 }}>编辑资料</span><span style={{ color: "#D1D5DB" }}>›</span></button>
        <button onClick={() => alert("提醒设置开发中")} style={menuStyle}><span style={{ fontSize: 20, width: 28 }}>🔔</span><span style={{ flex: 1 }}>提醒设置</span><span style={{ color: "#D1D5DB" }}>›</span></button>
        <button onClick={() => alert("分类管理开发中")} style={menuStyle}><span style={{ fontSize: 20, width: 28 }}>🏷️</span><span style={{ flex: 1 }}>分类管理</span><span style={{ color: "#D1D5DB" }}>›</span></button>
      </div>

      <div style={{ background: "#fff", borderRadius: 16, overflow: "hidden", marginBottom: 16, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
        <button onClick={() => alert("金橘记账 v0.1.0")} style={menuStyle}><span style={{ fontSize: 20, width: 28 }}>ℹ️</span><span style={{ flex: 1 }}>关于</span><span style={{ fontSize: 11, background: "#FEF3C7", color: "#F59E0B", padding: "2px 8px", borderRadius: 10 }}>v0.1.0</span><span style={{ color: "#D1D5DB" }}>›</span></button>
      </div>

      <button onClick={() => { logout(); navigate("/login"); }} style={{ width: "100%", padding: 14, background: "#fff", border: "1px solid #FEE2E2", borderRadius: 12, fontSize: 15, color: "#EF4444", fontWeight: 500, cursor: "pointer" }}>退出登录</button>
    </div>
  );
}
