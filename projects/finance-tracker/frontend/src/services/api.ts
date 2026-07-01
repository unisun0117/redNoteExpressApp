const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000/api";

let accessToken: string | null = localStorage.getItem("access_token");
let refreshToken: string | null = localStorage.getItem("refresh_token");

function setTokens(access: string, refresh: string) {
  accessToken = access;
  refreshToken = refresh;
  localStorage.setItem("access_token", access);
  localStorage.setItem("refresh_token", refresh);
}

function clearTokens() {
  accessToken = null;
  refreshToken = null;
  localStorage.removeItem("access_token");
  localStorage.removeItem("refresh_token");
}

async function refreshAccessToken(): Promise<boolean> {
  if (!refreshToken) return false;
  try {
    const res = await fetch(`${BASE_URL}/auth/refresh?refresh_token=${refreshToken}`, { method: "POST" });
    if (!res.ok) return false;
    const data = await res.json();
    setTokens(data.access_token, data.refresh_token);
    return true;
  } catch {
    return false;
  }
}

async function authFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const headers: Record<string, string> = { ...(options.headers as Record<string, string>) };
  if (accessToken) headers["Authorization"] = `Bearer ${accessToken}`;

  let res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  if (res.status === 401 && refreshToken) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      headers["Authorization"] = `Bearer ${accessToken}`;
      res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
    } else {
      clearTokens();
      window.location.href = "/login";
    }
  }
  return res;
}

export const api = {
  // Auth
  login: (email: string, password: string) =>
    fetch(`${BASE_URL}/auth/login`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password }) }).then(r => r.json()),
  register: (email: string, password: string) =>
    fetch(`${BASE_URL}/auth/register`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password }) }).then(r => r.json()),
  getMe: () => authFetch("/auth/me").then(r => r.json()),

  // Transactions
  getCategories: () => authFetch("/categories").then(r => r.json()),
  getTransactions: (params: Record<string, string>) => {
    const qs = new URLSearchParams(params).toString();
    return authFetch(`/transactions?${qs}`).then(r => r.json());
  },
  createTransaction: (data: any) => authFetch("/transactions", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(data) }).then(r => r.json()),
  updateTransaction: (id: string, data: any) => authFetch(`/transactions/${id}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(data) }).then(r => r.json()),
  deleteTransaction: (id: string) => authFetch(`/transactions/${id}`, { method: "DELETE" }).then(r => r.json()),

  // Budgets
  getBudgets: (month?: string) => authFetch(`/budgets${month ? `?month=${month}` : ""}`).then(r => r.json()),
  updateBudgets: (data: any) => authFetch("/budgets", { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(data) }).then(r => r.json()),

  // Stats
  getDashboard: (month?: string) => authFetch(`/dashboard${month ? `?month=${month}` : ""}`).then(r => r.json()),
  getTrend: (month?: string) => authFetch(`/stats/trend${month ? `?month=${month}` : ""}`).then(r => r.json()),
  getCategoryStats: (month?: string) => authFetch(`/stats/category${month ? `?month=${month}` : ""}`).then(r => r.json()),

  // Profile
  getProfile: () => authFetch("/user/profile").then(r => r.json()),
  updateProfile: (data: any) => authFetch("/user/profile", { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify(data) }).then(r => r.json()),
};

export { setTokens, clearTokens, accessToken };
