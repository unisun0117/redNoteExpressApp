import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { api } from "../services/api";

export function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState<any>(null);
  const [trend, setTrend] = useState<any[]>([]);
  const [cats, setCats] = useState<any[]>([]);

  useEffect(() => {
    api.getDashboard().then(setData);
    api.getTrend().then(d => setTrend(d.daily || []));
    api.getCategoryStats().then(d => setCats(d.categories || []));
  }, []);

  const fmt = (v: number) => (v / 100).toFixed(2);
  const maxTrend = Math.max(...trend.map((d: any) => d.amount), 1);

  return (
    <div style={{ padding: "24px 16px", background: "#FFF8F0", minHeight: "100vh" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
        <h1 style={{ fontSize: 20, color: "#1F2937" }}>🍊 金橘记账</h1>
        <div onClick={() => navigate("/profile")} style={{ width: 36, height: 36, borderRadius: "50%", background: "#FEF3C7", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, cursor: "pointer" }}>{user?.avatar || "😊"}</div>
      </div>

      {/* 余额卡片 */}
      <div onClick={() => navigate("/list")} style={{ background: "linear-gradient(135deg, #F59E0B, #F97316)", borderRadius: 16, padding: 24, color: "#fff", marginBottom: 16, cursor: "pointer" }}>
        <div style={{ fontSize: 13, opacity: .8 }}>本月余额</div>
        <div style={{ fontSize: 32, fontWeight: 700, margin: "6px 0" }}>¥ {data ? fmt(data.balance) : "0.00"}</div>
        <div style={{ display: "flex", gap: 16, fontSize: 13 }}>
          <span>📈 收入 ¥{data ? fmt(data.month_income) : "0"}</span>
          <span>📉 支出 ¥{data ? fmt(data.month_expense) : "0"}</span>
        </div>
      </div>

      {/* 快捷记账 */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginBottom: 18 }}>
        <button onClick={() => navigate("/form?type=expense")} style={{ background: "#fff", border: "1px solid transparent", borderRadius: 12, padding: 14, display: "flex", alignItems: "center", gap: 10, fontSize: 14, cursor: "pointer", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}><span style={{ fontSize: 22 }}>📝</span> 记支出</button>
        <button onClick={() => navigate("/form?type=income")} style={{ background: "#fff", border: "1px solid transparent", borderRadius: 12, padding: 14, display: "flex", alignItems: "center", gap: 10, fontSize: 14, cursor: "pointer", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}><span style={{ fontSize: 22 }}>💰</span> 记收入</button>
      </div>

      {/* 周趋势 */}
      <div style={{ marginBottom: 18 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: "#1F2937", marginBottom: 10 }}>本周支出趋势</div>
        <div style={{ background: "#fff", borderRadius: 12, padding: 14, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
          <div style={{ display: "flex", alignItems: "flex-end", gap: 8, height: 80 }}>
            {trend.map((d: any, i: number) => (
              <div key={i} style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 4 }}>
                <div style={{ width: "100%", height: maxTrend > 0 ? `${(d.amount / maxTrend) * 70}px` : "2px", background: i === trend.length - 1 ? "linear-gradient(0deg, #EF4444, #FCA5A5)" : "linear-gradient(0deg, #F59E0B, #FCD34D)", borderRadius: "4px 4px 0 0", minHeight: 4 }} />
                <span style={{ fontSize: 10, color: i === trend.length - 1 ? "#EF4444" : "#9CA3AF" }}>{["一","二","三","四","五","六","日"][i]}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 分类占比 */}
      <div style={{ marginBottom: 18 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: "#1F2937", marginBottom: 10 }}>本月支出分类</div>
        <div style={{ background: "#fff", borderRadius: 12, padding: 14, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
          {cats.slice(0, 5).map((c: any) => (
            <div key={c.name} onClick={() => navigate(`/list?category=${c.name}`)} style={{ display: "flex", alignItems: "center", gap: 8, padding: "6px 0", cursor: "pointer" }}>
              <span style={{ fontSize: 18 }}>{c.emoji}</span>
              <span style={{ flex: 1, fontSize: 13, color: "#1F2937" }}>{c.name}</span>
              <span style={{ fontSize: 13, color: "#EF4444", fontWeight: 500 }}>¥{fmt(c.amount)}</span>
              <div style={{ width: 50, height: 4, background: "#F3E8DC", borderRadius: 2 }}><div style={{ height: "100%", background: c.percentage > 80 ? "#EF4444" : "#F59E0B", width: `${c.percentage}%`, borderRadius: 2 }} /></div>
            </div>
          ))}
          {cats.length === 0 && <div style={{ textAlign: "center", color: "#9CA3AF", padding: 20 }}>暂无数据</div>}
        </div>
      </div>

      {/* 最近记录 */}
      <div>
        <div style={{ fontSize: 15, fontWeight: 600, color: "#1F2937", marginBottom: 10, display: "flex", justifyContent: "space-between" }}>
          最近记录
          <span onClick={() => navigate("/list")} style={{ fontSize: 13, color: "#F59E0B", cursor: "pointer" }}>查看全部 →</span>
        </div>
        {data?.recent_transactions?.map((t: any) => (
          <div key={t.id} onClick={() => navigate(`/form?edit=${t.id}`)} style={{ display: "flex", alignItems: "center", gap: 10, background: "#fff", padding: "12px 14px", borderRadius: 12, marginBottom: 6, cursor: "pointer" }}>
            <span style={{ fontSize: 26 }}>{t.category_emoji || "📌"}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, color: "#1F2937", fontWeight: 500 }}>{t.note || t.category}</div>
              <div style={{ fontSize: 11, color: "#9CA3AF", marginTop: 2 }}>{t.tx_date} · {t.category}</div>
            </div>
            <span style={{ fontSize: 15, fontWeight: 600, color: t.type === "expense" ? "#EF4444" : "#10B981" }}>{t.type === "expense" ? "-" : "+"}¥{fmt(t.amount)}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
