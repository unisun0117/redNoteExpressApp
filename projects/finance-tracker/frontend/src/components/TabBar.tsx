import { useLocation, useNavigate } from "react-router-dom";

export function TabBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const path = location.pathname;

  const tabs = [
    { path: "/", icon: "🏠", label: "首页" },
    { path: "/list", icon: "📋", label: "账单" },
    { path: "/form", icon: "➕", label: "记账" },
    { path: "/budget", icon: "🎯", label: "预算" },
    { path: "/profile", icon: "👤", label: "我的" },
  ];

  return (
    <div style={{
      position: "fixed", bottom: 0, left: "50%", transform: "translateX(-50%)",
      width: "min(480px, 100vw)", background: "#fff", display: "flex",
      borderTop: "1px solid #F3E8DC", padding: "8px 0", zIndex: 100,
    }}>
      {tabs.map(tab => (
        <button
          key={tab.path}
          onClick={() => navigate(tab.path)}
          style={{
            flex: 1, display: "flex", flexDirection: "column", alignItems: "center", gap: 2,
            background: "none", border: "none", cursor: "pointer",
            color: path === tab.path ? "#F59E0B" : "#9CA3AF",
            fontSize: 11, padding: "6px 0",
          }}
        >
          <span style={{ fontSize: 20 }}>{tab.icon}</span>
          {tab.label}
        </button>
      ))}
    </div>
  );
}
