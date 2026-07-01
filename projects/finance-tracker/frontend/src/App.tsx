import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./hooks/useAuth";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { ListPage } from "./pages/ListPage";
import { FormPage } from "./pages/FormPage";
import { BudgetPage } from "./pages/BudgetPage";
import { ProfilePage } from "./pages/ProfilePage";
import { TabBar } from "./components/TabBar";

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <div style={{ padding: 40, textAlign: "center" }}>Loading...</div>;
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
        <Route path="/" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
        <Route path="/list" element={<PrivateRoute><ListPage /></PrivateRoute>} />
        <Route path="/form" element={<PrivateRoute><FormPage /></PrivateRoute>} />
        <Route path="/budget" element={<PrivateRoute><BudgetPage /></PrivateRoute>} />
        <Route path="/profile" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />
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
