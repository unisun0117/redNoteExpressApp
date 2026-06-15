import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export function LoginPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isRegister, setIsRegister] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    if (!email || !password) {
      setError("请填写邮箱和密码");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      if (isRegister) {
        await register(email, password);
      } else {
        await login(email, password);
      }
      navigate("/");
    } catch (e: any) {
      setError(e.message || "操作失败");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page" style={{ paddingTop: 60 }}>
      <div style={{ textAlign: "center", marginBottom: 32 }}>
        <div style={{ fontSize: 48, marginBottom: 8 }}>🍠</div>
        <h1 style={{ fontSize: 24, fontWeight: 700, color: "#1a1a1a" }}>红薯快写</h1>
        <p style={{ fontSize: 14, color: "#999", marginTop: 4 }}>AI驱动的小红书文案生成器</p>
      </div>

      <div className="card">
        <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 16 }}>
          {isRegister ? "注册" : "登录"}
        </h2>

        <div className="form-group">
          <div className="label">邮箱</div>
          <input
            className="input"
            type="email"
            placeholder="your@email.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className="form-group">
          <div className="label">密码</div>
          <input
            className="input"
            type="password"
            placeholder="密码"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && submit()}
          />
        </div>

        {error && (
          <div style={{ color: "#e74c3c", fontSize: 13, marginBottom: 12 }}>{error}</div>
        )}

        <button className="btn btn-primary" onClick={submit} disabled={loading}>
          {loading ? "处理中..." : (isRegister ? "注册" : "登录")}
        </button>

        <div style={{ textAlign: "center", marginTop: 16, fontSize: 14, color: "#666" }}>
          {isRegister ? "已有账号？" : "没有账号？"}
          <span
            style={{ color: "#ee5a24", cursor: "pointer", fontWeight: 600 }}
            onClick={() => setIsRegister(!isRegister)}
          >
            {isRegister ? "去登录" : "去注册"}
          </span>
        </div>
      </div>
    </div>
  );
}
