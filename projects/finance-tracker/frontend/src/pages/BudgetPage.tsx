import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../services/api";

const CAT_OPTIONS = [
  { name: "餐饮", emoji: "🍽️" }, { name: "交通", emoji: "🚇" }, { name: "购物", emoji: "🛍️" }, { name: "娱乐", emoji: "🎬" },
  { name: "住房", emoji: "🏠" }, { name: "医疗", emoji: "💊" }, { name: "教育", emoji: "📚" }, { name: "其他", emoji: "📌" },
];

export function BudgetPage() {
  const navigate = useNavigate();
  const month = new Date().toISOString().slice(0, 7);
  const [data, setData] = useState<any>(null);
  const [showModal, setShowModal] = useState(false);
  const [editCat, setEditCat] = useState<string | null>(null);
  const [modalCat, setModalCat] = useState("餐饮");
  const [modalEmoji, setModalEmoji] = useState("🍽️");
  const [modalAmount, setModalAmount] = useState("");

  const load = () => {
    api.getBudgets(month).then(d => {
      if (d.categories) setData(d);
    });
  };

  useEffect(() => { load(); }, []);

  const fmt = (v: number) => (v / 100).toFixed(0);

  const openAdd = () => {
    setEditCat(null);
    setModalCat("餐饮"); setModalEmoji("🍽️"); setModalAmount("");
    setShowModal(true);
  };

  const openEdit = (cat: any) => {
    setEditCat(cat.category);
    setModalCat(cat.category); setModalEmoji(cat.emoji || "📌");
    setModalAmount(String(cat.budget / 100));
    setShowModal(true);
  };

  const saveBudget = async () => {
    const amt = parseFloat(modalAmount);
    if (!amt || amt <= 0) return;

    const existing = data?.categories || [];
    let categories;
    if (editCat) {
      categories = existing.map((c: any) =>
        c.category === editCat ? { category: c.category, category_emoji: c.emoji, amount: Math.round(amt * 100) } : { category: c.category, category_emoji: c.emoji, amount: c.budget }
      );
    } else {
      if (existing.find((c: any) => c.category === modalCat)) { alert("该分类已有预算"); return; }
      categories = [...existing.map((c: any) => ({ category: c.category, category_emoji: c.emoji, amount: c.budget })), { category: modalCat, category_emoji: modalEmoji, amount: Math.round(amt * 100) }];
    }
    const total = categories.reduce((s: number, c: any) => s + c.amount, 0);
    await api.updateBudgets({ month, total_budget: total, categories });
    setShowModal(false);
    load();
  };

  const barColor = (pct: number) => pct > 90 ? "#EF4444" : pct > 60 ? "#F59E0B" : "#10B981";

  return (
    <div style={{ padding: "24px 16px", background: "#FFF8F0", minHeight: "100vh" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 20 }}>
        <span onClick={() => navigate(-1)} style={{ fontSize: 20, cursor: "pointer" }}>←</span>
        <h1 style={{ fontSize: 20, color: "#1F2937" }}>🎯 月度预算</h1>
        <span onClick={openAdd} style={{ marginLeft: "auto", fontSize: 14, color: "#F59E0B", cursor: "pointer", fontWeight: 500 }}>+ 新增</span>
      </div>
      <div style={{ fontSize: 13, color: "#F59E0B", marginBottom: 16 }}>{month}</div>

      {data && (
        <>
          <div style={{ background: "#fff", borderRadius: 16, padding: 20, boxShadow: "0 1px 3px rgba(0,0,0,.06)", marginBottom: 20 }}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 10 }}>
              <span style={{ fontSize: 14, color: "#6B7280" }}>月度总预算</span>
              <span style={{ fontSize: 16, fontWeight: 600 }}>¥{fmt(data.total_budget)}</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 10 }}>
              <span style={{ fontSize: 14, color: "#6B7280" }}>已使用</span>
              <span style={{ fontSize: 16, fontWeight: 600, color: "#F59E0B" }}>¥{fmt(data.total_spent)}</span>
            </div>
            <div style={{ height: 8, background: "#F3E8DC", borderRadius: 4 }}>
              <div style={{ height: "100%", background: "linear-gradient(90deg, #F59E0B, #F97316)", borderRadius: 4, width: `${data.total_budget > 0 ? Math.min(data.total_spent / data.total_budget * 100, 100) : 0}%` }} />
            </div>
            <div style={{ fontSize: 12, color: "#9CA3AF", marginTop: 8 }}>剩余 ¥{fmt(data.total_budget - data.total_spent)}</div>
          </div>

          <div style={{ fontSize: 14, color: "#6B7280", fontWeight: 500, marginBottom: 10 }}>分类预算</div>
          {data.categories.map((c: any) => (
            <div key={c.category} onClick={() => openEdit(c)} style={{ background: "#fff", borderRadius: 12, padding: 16, marginBottom: 10, cursor: "pointer", position: "relative", boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 10 }}>
                <span>{c.emoji} {c.category}</span>
                <span style={{ fontSize: 14, color: "#6B7280" }}><span style={{ color: "#EF4444" }}>¥{fmt(c.spent)}</span> / ¥{fmt(c.budget)}</span>
              </div>
              <div style={{ height: 6, background: "#F3E8DC", borderRadius: 3 }}>
                <div style={{ height: "100%", background: barColor(c.budget > 0 ? c.spent / c.budget * 100 : 0), borderRadius: 3, width: `${c.budget > 0 ? Math.min(c.spent / c.budget * 100, 100) : 0}%` }} />
              </div>
            </div>
          ))}
          <button onClick={openAdd} style={{ width: "100%", padding: 14, background: "#fff", border: "2px dashed #F3E8DC", borderRadius: 12, fontSize: 15, color: "#9CA3AF", cursor: "pointer", marginTop: 8 }}>+ 添加分类预算</button>
        </>
      )}

      {/* Modal */}
      {showModal && (
        <div onClick={() => setShowModal(false)} style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, background: "rgba(0,0,0,.4)", zIndex: 200, display: "flex", alignItems: "flex-end", justifyContent: "center" }}>
          <div onClick={e => e.stopPropagation()} style={{ background: "#fff", borderRadius: "20px 20px 0 0", padding: 24, width: "min(480px, 100vw)", animation: "slideUp .3s ease" }}>
            <h3 style={{ fontSize: 18, marginBottom: 20 }}>{editCat ? "编辑预算" : "新增分类预算"}</h3>
            <div style={{ marginBottom: 16 }}>
              <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>分类</div>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 6 }}>
                {CAT_OPTIONS.map(c => (
                  <div key={c.name} onClick={() => { setModalCat(c.name); setModalEmoji(c.emoji); }} style={{ padding: "8px 4px", border: `2px solid ${modalCat === c.name ? "#F59E0B" : "#F3E8DC"}`, borderRadius: 8, textAlign: "center", fontSize: 13, cursor: "pointer", background: modalCat === c.name ? "#FEF3C7" : "#fff" }}>{c.emoji} {c.name}</div>
                ))}
              </div>
            </div>
            <div style={{ marginBottom: 20 }}>
              <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>预算金额</div>
              <input value={modalAmount} onChange={e => setModalAmount(e.target.value)} type="number" placeholder="0" style={{ width: "100%", padding: "12px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 15, outline: "none" }} />
            </div>
            <div style={{ display: "flex", gap: 12 }}>
              <button onClick={() => setShowModal(false)} style={{ flex: 1, padding: 14, border: "none", borderRadius: 10, background: "#F3F4F6", color: "#6B7280", fontSize: 15, fontWeight: 600, cursor: "pointer" }}>取消</button>
              <button onClick={saveBudget} style={{ flex: 1, padding: 14, border: "none", borderRadius: 10, background: "#F59E0B", color: "#fff", fontSize: 15, fontWeight: 600, cursor: "pointer" }}>保存</button>
            </div>
          </div>
          <style>{`@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }`}</style>
        </div>
      )}
    </div>
  );
}
