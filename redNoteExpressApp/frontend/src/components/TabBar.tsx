import { useLocation, useNavigate } from "react-router-dom";

export function TabBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const path = location.pathname;

  const tabs = [
    { path: "/", icon: "✏️", label: "生成" },
    { path: "/batch", icon: "📦", label: "批量" },
    { path: "/dashboard", icon: "👤", label: "我的" },
  ];

  const isActive = (tabPath: string) => path === tabPath;

  return (
    <div className="tab-bar">
      {tabs.map((tab) => (
        <button
          key={tab.path}
          className={`tab-item${isActive(tab.path) ? " active" : ""}`}
          onClick={() => navigate(tab.path)}
        >
          <span className="tab-icon">{tab.icon}</span>
          <span>{tab.label}</span>
        </button>
      ))}
    </div>
  );
}
