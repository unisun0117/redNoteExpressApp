import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export function LoginPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<"login" | "register">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPw, setConfirmPw] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    setError("");
    if (!email || !password) { setError("请填写邮箱和密码"); return; }
    if (tab === "register" && password !== confirmPw) { setError("两次密码不一致"); return; }
    if (password.length < 6) { setError("密码至少6位"); return; }
    setLoading(true);
    try {
      if (tab === "login") await login(email, password);
      else await register(email, password);
      navigate("/");
    } catch (e: any) {
      setError(e.message || "操作失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "40px 24px", display: "flex", flexDirection: "column", alignItems: "center", minHeight: "100vh", background: "#FFF8F0" }}>
      <div style={{ textAlign: "center", marginBottom: 40 }}>
        <div style={{ fontSize: 56, animation: "float 3s ease-in-out infinite" }}>🍊</div>
        <h1 style={{ fontSize: 24, color: "#1F2937", marginTop: 8 }}>金橘记账</h1>
        <p style={{ fontSize: 13, color: "#9CA3AF" }}>温暖治愈的个人记账工具</p>
      </div>

      <div style={{ display: "flex", background: "#fff", borderRadius: 12, padding: 4, marginBottom: 24, width: "100%", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
        <button onClick={() => setTab("login")} style={{ flex: 1, padding: 10, border: "none", borderRadius: 10, fontSize: 14, fontWeight: 500, cursor: "pointer", background: tab === "login" ? "#FEF3C7" : "transparent", color: tab === "login" ? "#F59E0B" : "#9CA3AF" }}>登录</button>
        <button onClick={() => setTab("register")} style={{ flex: 1, padding: 10, border: "none", borderRadius: 10, fontSize: 14, fontWeight: 500, cursor: "pointer", background: tab === "register" ? "#FEF3C7" : "transparent", color: tab === "register" ? "#F59E0B" : "#9CA3AF" }}>注册</button>
      </div>

      <div style={{ width: "100%", maxWidth: "100%", display: "flex", flexDirection: "column", gap: 16, boxSizing: "border-box" }}>
        <div>
          <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>邮箱</div>
          <input value={email} onChange={e => setEmail(e.target.value)} placeholder="请输入邮箱" style={{ width: "100%", padding: "14px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 15, outline: "none", background: "#fff" }} />
        </div>
        <div>
          <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>密码</div>
          <div style={{ position: "relative" }}>
            <input type={showPw ? "text" : "password"} value={password} onChange={e => setPassword(e.target.value)} placeholder="请输入密码" style={{ width: "100%", padding: "14px 40px 14px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 15, outline: "none", background: "#fff" }} />
            <button onClick={() => setShowPw(!showPw)} style={{ position: "absolute", right: 12, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", fontSize: 18, cursor: "pointer" }}>{showPw ? "🙈" : "👁️"}</button>
          </div>
        </div>
        {tab === "register" && (
          <div>
            <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>确认密码</div>
            <input type="password" value={confirmPw} onChange={e => setConfirmPw(e.target.value)} placeholder="再次输入密码" style={{ width: "100%", padding: "14px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 15, outline: "none", background: "#fff" }} />
          </div>
        )}

        {error && <div style={{ color: "#EF4444", fontSize: 13, padding: "8px 12px", background: "#FEF2F2", borderRadius: 8 }}>{error}</div>}

        <button onClick={handleSubmit} disabled={loading} style={{ width: "100%", padding: 14, border: "none", borderRadius: 10, fontSize: 16, fontWeight: 600, background: loading ? "#FCD34D" : "#F59E0B", color: "#fff", cursor: loading ? "not-allowed" : "pointer" }}>
          {loading ? "处理中..." : tab === "login" ? "登录" : "注册"}
        </button>
      </div>

      <style>{`@keyframes float { 0%,100% { transform: translateY(0) } 50% { transform: translateY(-8px) } }`}</style>
    </div>
  );
}
