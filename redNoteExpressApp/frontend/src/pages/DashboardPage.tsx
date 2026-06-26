import { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import { api } from "../services/api";
import { ResultView } from "../components/ResultView";

export function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [history, setHistory] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  useEffect(() => {
    if (user) {
      api.getGenerations(0, 20).then((data) => {
        if (data.items) setHistory(data.items);
        setLoading(false);
      }).catch(() => setLoading(false));
    }
  }, [user]);

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

      {/* 历史文章列表 */}
      <div style={{ marginTop: 24 }}>
        <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 12 }}>
          📝 历史文章 ({history.length})
        </h2>

        {loading && <div style={{ textAlign: "center", color: "#999", padding: 20 }}>加载中...</div>}

        {!loading && history.length === 0 && (
          <div style={{ textAlign: "center", color: "#999", padding: 20 }}>
            还没有生成过文章，去<a href="/" style={{ color: "#ee5a24" }}>首页</a>试试吧～
          </div>
        )}

        {history.map((item: any) => (
          <div key={item.id} className="card" style={{ marginBottom: 8, cursor: "pointer" }}
               onClick={() => setExpandedId(expandedId === item.id ? null : item.id)}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600, fontSize: 14 }}>
                  {item.generated_article?.title || "无标题"}
                </div>
                <div style={{ fontSize: 12, color: "#999", marginTop: 4 }}>
                  {item.keywords && `🏷 ${item.keywords}`}
                  {item.keywords && " · "}
                  {new Date(item.created_at).toLocaleString("zh-CN")}
                </div>
              </div>
              <span style={{ fontSize: 12, color: "#999" }}>
                {expandedId === item.id ? "收起 ▲" : "展开 ▼"}
              </span>
            </div>

            {expandedId === item.id && (
              <div style={{ marginTop: 12, borderTop: "1px solid #eee", paddingTop: 12 }}>
                <ResultView article={item.generated_article} />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
