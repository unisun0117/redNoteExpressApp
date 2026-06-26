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
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string>),
  };
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

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
  register: (email: string, password: string) =>
    fetch(`${BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }).then((r) => r.json()),

  login: (email: string, password: string) =>
    fetch(`${BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }).then((r) => r.json()),

  getMe: () => authFetch("/auth/me").then((r) => r.json()),

  // Generate
  generate: (formData: FormData) =>
    authFetch("/generate", { method: "POST", body: formData }).then((r) => r.json()),

  // Styles
  getStyles: () => fetch(`${BASE_URL}/styles`).then((r) => r.json()),

  // Viral
  analyzeViral: (data: { url?: string; text?: string }) =>
    authFetch("/viral/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }).then((r) => r.json()),

  // Billing
  getPackages: () => authFetch("/billing/packages").then((r) => r.json()),
  recharge: (packageId: string) =>
    authFetch("/billing/recharge", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ package_id: packageId }),
    }).then((r) => r.json()),
  subscribe: (planId: string) =>
    authFetch("/billing/subscribe", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ plan_id: planId }),
    }).then((r) => r.json()),

  // Batch
  batchGenerate: (formData: FormData) =>
    authFetch("/batch/generate", { method: "POST", body: formData }).then((r) => r.json()),

  // History
  getGenerations: (skip = 0, limit = 20) =>
    authFetch(`/generations?skip=${skip}&limit=${limit}`).then((r) => r.json()),
};

export { setTokens, clearTokens, accessToken };
