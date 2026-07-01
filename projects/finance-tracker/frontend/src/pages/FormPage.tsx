import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { api } from "../services/api";

const EXPENSE_CATS = [
  { name: "餐饮", emoji: "🍽️" }, { name: "交通", emoji: "🚇" }, { name: "购物", emoji: "🛍️" }, { name: "娱乐", emoji: "🎬" },
  { name: "住房", emoji: "🏠" }, { name: "医疗", emoji: "💊" }, { name: "教育", emoji: "📚" }, { name: "其他", emoji: "📌" },
];
const INCOME_CATS = [
  { name: "工资", emoji: "💰" }, { name: "红包", emoji: "🧧" }, { name: "理财", emoji: "📈" }, { name: "礼物", emoji: "🎁" },
  { name: "兼职", emoji: "💼" }, { name: "退款", emoji: "↩️" }, { name: "其他", emoji: "📌" },
];

export function FormPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const editId = params.get("edit");

  const [txType, setTxType] = useState<"expense" | "income">((params.get("type") as any) || "expense");
  const [amount, setAmount] = useState("");
  const [category, setCategory] = useState(txType === "expense" ? "餐饮" : "工资");
  const [note, setNote] = useState("");
  const [txDate, setTxDate] = useState(new Date().toISOString().slice(0, 10));
  const [loading, setLoading] = useState(false);

  const cats = txType === "expense" ? EXPENSE_CATS : INCOME_CATS;

  useEffect(() => {
    if (editId) {
      api.getTransactions({ page: "1", page_size: "100" }).then(data => {
        const tx = data.items?.find((t: any) => t.id === editId);
        if (tx) {
          setTxType(tx.type);
          setAmount(String(tx.amount / 100));
          setCategory(tx.category);
          setNote(tx.note || "");
          setTxDate(tx.tx_date);
        }
      });
    }
  }, [editId]);

  useEffect(() => {
    setCategory(txType === "expense" ? "餐饮" : "工资");
  }, [txType]);

  const handleSave = async () => {
    const amt = parseFloat(amount);
    if (!amt || amt <= 0) return;
    setLoading(true);
    const data = {
      type: txType,
      amount: Math.round(amt * 100),
      category,
      note: note || null,
      tx_date: txDate,
    };
    try {
      if (editId) await api.updateTransaction(editId, data);
      else await api.createTransaction(data);
      navigate("/");
    } catch (e) {
      alert("保存失败");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!editId || !confirm("确定删除这条记录吗？")) return;
    await api.deleteTransaction(editId);
    navigate("/");
  };

  return (
    <div style={{ padding: "24px 16px", background: "#FFF8F0", minHeight: "100vh" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 24 }}>
        <span onClick={() => navigate(-1)} style={{ fontSize: 20, cursor: "pointer" }}>←</span>
        <h1 style={{ fontSize: 20, color: "#1F2937" }}>{editId ? "编辑账单" : "记一笔"}</h1>
      </div>

      {/* 类型切换 */}
      <div style={{ display: "flex", background: "#fff", borderRadius: 12, padding: 4, marginBottom: 24, boxShadow: "0 1px 3px rgba(0,0,0,.06)" }}>
        <button onClick={() => setTxType("expense")} style={{ flex: 1, padding: 10, border: "none", borderRadius: 10, fontSize: 15, fontWeight: 600, cursor: "pointer", background: txType === "expense" ? "#FEE2E2" : "transparent", color: txType === "expense" ? "#EF4444" : "#9CA3AF" }}>🔴 支出</button>
        <button onClick={() => setTxType("income")} style={{ flex: 1, padding: 10, border: "none", borderRadius: 10, fontSize: 15, fontWeight: 600, cursor: "pointer", background: txType === "income" ? "#D1FAE5" : "transparent", color: txType === "income" ? "#10B981" : "#9CA3AF" }}>🟢 收入</button>
      </div>

      {/* 金额 */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>金额</div>
        <div style={{ display: "flex", alignItems: "center", gap: 8, background: "#fff", border: "2px solid #F3E8DC", borderRadius: 12, padding: "8px 16px" }}>
          <span style={{ fontSize: 24, color: "#F59E0B", fontWeight: 600 }}>¥</span>
          <input value={amount} onChange={e => setAmount(e.target.value)} type="number" placeholder="0.00" style={{ flex: 1, border: "none", fontSize: 28, fontWeight: 700, color: "#1F2937", outline: "none", background: "transparent", padding: "8px 0" }} />
        </div>
      </div>

      {/* 分类 */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>分类</div>
        <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 8 }}>
          {cats.map(c => (
            <div key={c.name} onClick={() => setCategory(c.name)} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4, padding: "12px 8px", background: category === c.name ? "#FEF3C7" : "#fff", border: `2px solid ${category === c.name ? "#F59E0B" : "transparent"}`, borderRadius: 12, cursor: "pointer", fontSize: 12, color: "#6B7280" }}>
              <span style={{ fontSize: 28 }}>{c.emoji}</span>{c.name}
            </div>
          ))}
        </div>
      </div>

      {/* 日期 */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>日期</div>
        <input type="date" value={txDate} onChange={e => setTxDate(e.target.value)} style={{ width: "100%", padding: "12px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 15, outline: "none", background: "#fff" }} />
      </div>

      {/* 备注 */}
      <div style={{ marginBottom: 20 }}>
        <div style={{ fontSize: 13, color: "#6B7280", marginBottom: 6 }}>备注（可选）</div>
        <input value={note} onChange={e => setNote(e.target.value)} placeholder="写点什么..." style={{ width: "100%", padding: "12px 16px", border: "1px solid #F3E8DC", borderRadius: 10, fontSize: 15, outline: "none", background: "#fff" }} />
      </div>

      <div style={{ display: "flex", gap: 12 }}>
        <button onClick={handleSave} disabled={loading} style={{ flex: 1, padding: 16, border: "none", borderRadius: 12, fontSize: 16, fontWeight: 600, background: "#F59E0B", color: "#fff", cursor: "pointer" }}>{loading ? "保存中..." : "✓ 保存"}</button>
        {editId && <button onClick={handleDelete} style={{ padding: 16, border: "1px solid #FEE2E2", borderRadius: 12, fontSize: 16, background: "#fff", color: "#EF4444", cursor: "pointer" }}>🗑</button>}
      </div>
    </div>
  );
}
