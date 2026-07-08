import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./hooks/useAuth";
import { GeneratorPage } from "./pages/GeneratorPage";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { BatchPage } from "./pages/BatchPage";
import { BillingPage } from "./pages/BillingPage";
import { TabBar } from "./components/TabBar";

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return (
    <div style={{
      display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
      minHeight: "100vh", background: "#faf9f7", color: "#5c5852", fontFamily: "system-ui, sans-serif",
      gap: 16, padding: 40
    }}>
      <div style={{ fontSize: 48, animation: "pulse 1.5s ease-in-out infinite" }}>✍️</div>
      <div style={{ fontSize: 18, fontWeight: 600, color: "#3d3a35" }}>红薯快写</div>
      <div style={{ fontSize: 14, color: "#8b867e" }}>正在唤醒服务器，稍等片刻…</div>
      <style>{`@keyframes pulse { 0%,100%{opacity:0.4;transform:scale(0.95)} 50%{opacity:1;transform:scale(1.05)} }`}</style>
    </div>
  );
  if (!user) return <Navigate to="/login" />;
  return <>{children}</>;
}

function AppLayout() {
  const { user } = useAuth();
  const hideTabs = !user;

  return (
    <div style={{ maxWidth: 480, margin: "0 auto", minHeight: "100vh", paddingBottom: hideTabs ? 0 : 60 }}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<PrivateRoute><GeneratorPage /></PrivateRoute>} />
        <Route path="/dashboard" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
        <Route path="/batch" element={<PrivateRoute><BatchPage /></PrivateRoute>} />
        <Route path="/billing" element={<PrivateRoute><BillingPage /></PrivateRoute>} />
      </Routes>
      {!hideTabs && <TabBar />}
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </BrowserRouter>
  );
}
