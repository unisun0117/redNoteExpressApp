import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api } from "../services/api";

export function ListPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [items, setItems] = useState<any[]>([]);
  const [summary, setSummary] = useState<any>({ income: 0, expense: 0 });
  const [catFilter, setCatFilter] = useState(params.get("category") || "");
  const [search, setSearch] = useState("");
  const month = new Date().toISOString().slice(0, 7);

  const load = () => {
    const q: Record<string, string> = { month, page: "1", page_size: "100" };
    if (catFilter) q.category = catFilter;
    if (search) q.q = search;
    api.getTransactions(q).then(data => {
      setItems(data.items || []);
      setSummary(data.summary || { income: 0, expense: 0 });
    });
  };

  useEffect(() => { load(); }, [catFilter, search]);

  const fmt = (v: number) => (v / 100).toFixed(2);

  // 按日期分组
  const grouped: Record<string, any[]> = {};
  items.forEach((t: any) => {
    if (!grouped[t.tx_date]) grouped[t.tx_date] = [];
    grouped[t.tx_date].push(t);
  });
  const dates = Object.keys(grouped).sort().reverse();

  const cats = ["餐饮","交通","购物","娱乐","住房","医疗","教育","其他"];

  return (
    <div style={{ padding: "24px 16px", background: "#FFF8F0", minHeight: "100vh" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
        <span onClick={() => navigate(-1)} style={{ fontSize: 20, cursor: "pointer" }}>←</span>
        <h1 style={{ fontSize: 20, color: "#1F2937" }}>全部账单</h1>
        <span style={{ marginLeft: "auto", fontSize: 13, color: "#F59E0B" }}>{month}</span>
      </div>

      {/* 汇总 */}
      <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
        <div style={{ flex: 1, background: "#fff", borderRadius: 12, padding: 12, textAlign: "center", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
          <div style={{ fontSize: 18, fontWeight: 700, color: "#10B981" }}>+{fmt(summary.income)}</div>
          <div style={{ fontSize: 11, color: "#9CA3AF" }}>收入</div>
        </div>
        <div style={{ flex: 1, background: "#fff", borderRadius: 12, padding: 12, textAlign: "center", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
          <div style={{ fontSize: 18, fontWeight: 700, color: "#EF4444" }}>-{fmt(summary.expense)}</div>
          <div style={{ fontSize: 11, color: "#9CA3AF" }}>支出</div>
        </div>
        <div style={{ flex: 1, background: "#fff", borderRadius: 12, padding: 12, textAlign: "center", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
          <div style={{ fontSize: 18, fontWeight: 700 }}>{fmt(summary.income - summary.expense)}</div>
          <div style={{ fontSize: 11, color: "#9CA3AF" }}>结余</div>
        </div>
      </div>

      {/* 搜索 */}
      <input value={search} onChange={e => setSearch(e.target.value)} placeholder="🔍 搜索账单..." style={{ width: "100%", padding: "10px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 14, outline: "none", background: "#fff", marginBottom: 12 }} />

      {/* 分类筛选 */}
      <div style={{ display: "flex", gap: 6, marginBottom: 16, overflowX: "auto" }}>
        <button onClick={() => setCatFilter("")} style={{ padding: "6px 14px", borderRadius: 20, fontSize: 13, border: `1px solid ${!catFilter ? "#F59E0B" : "#F3E8DC"}`, background: !catFilter ? "#F59E0B" : "#fff", color: !catFilter ? "#fff" : "#6B7280", whiteSpace: "nowrap", cursor: "pointer" }}>全部</button>
        {cats.map(c => (
          <button key={c} onClick={() => setCatFilter(catFilter === c ? "" : c)} style={{ padding: "6px 14px", borderRadius: 20, fontSize: 13, border: `1px solid ${catFilter === c ? "#F59E0B" : "#F3E8DC"}`, background: catFilter === c ? "#F59E0B" : "#fff", color: catFilter === c ? "#fff" : "#6B7280", whiteSpace: "nowrap", cursor: "pointer" }}>{c}</button>
        ))}
      </div>

      {/* 账单列表 */}
      {dates.length === 0 && <div style={{ textAlign: "center", color: "#9CA3AF", padding: 40 }}>暂无记录，去<a href="/form" style={{ color: "#F59E0B" }}>记一笔</a>吧～</div>}
      {dates.map(date => (
        <div key={date} style={{ marginBottom: 8 }}>
          <div style={{ fontSize: 13, color: "#9CA3AF", fontWeight: 500, padding: "8px 0" }}>{date}</div>
          {grouped[date].map((t: any) => (
            <div key={t.id} onClick={() => navigate(`/form?edit=${t.id}`)} style={{ display: "flex", alignItems: "center", gap: 10, background: "#fff", padding: "12px 14px", borderRadius: 12, marginBottom: 4, cursor: "pointer" }}>
              <span style={{ fontSize: 26 }}>{t.category_emoji || "📌"}</span>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, color: "#1F2937", fontWeight: 500 }}>{t.note || t.category}</div>
                <div style={{ fontSize: 11, color: "#9CA3AF", marginTop: 2 }}>{t.category}</div>
              </div>
              <span style={{ fontSize: 15, fontWeight: 600, color: t.type === "expense" ? "#EF4444" : "#10B981" }}>{t.type === "expense" ? "-" : "+"}¥{fmt(t.amount)}</span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
