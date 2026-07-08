const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000/api";
const DEFAULT_TIMEOUT = 12000; // 12s default timeout

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

/** fetch with AbortController timeout */
async function fetchWithTimeout(url: string, options: RequestInit & { timeout?: number } = {}): Promise<Response> {
  const { timeout = DEFAULT_TIMEOUT, ...fetchOptions } = options;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeout);
  try {
    const res = await fetch(url, { ...fetchOptions, signal: controller.signal });
    return res;
  } finally {
    clearTimeout(timer);
  }
}

async function refreshAccessToken(): Promise<boolean> {
  if (!refreshToken) return false;
  try {
    const res = await fetchWithTimeout(`${BASE_URL}/auth/refresh?refresh_token=${refreshToken}`, { method: "POST", timeout: 8000 });
    if (!res.ok) return false;
    const data = await res.json();
    setTokens(data.access_token, data.refresh_token);
    return true;
  } catch {
    return false;
  }
}

async function authFetch(path: string, options: RequestInit & { timeout?: number } = {}): Promise<Response> {
  const { timeout, ...rest } = options;
  const headers: Record<string, string> = {
    ...(rest.headers as Record<string, string>),
  };
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

  let res: Response;
  try {
    res = await fetchWithTimeout(`${BASE_URL}${path}`, { ...rest, headers, timeout });
  } catch (err: any) {
    if (err.name === "AbortError") throw new Error("TIMEOUT");
    throw err;
  }

  if (res.status === 401 && refreshToken) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      headers["Authorization"] = `Bearer ${accessToken}`;
      res = await fetchWithTimeout(`${BASE_URL}${path}`, { ...rest, headers, timeout });
    } else {
      clearTokens();
      window.location.href = "/login";
      throw new Error("AUTH_EXPIRED");
    }
  }
  return res;
}

export const api = {
  // Auth
  register: (email: string, password: string) =>
    fetchWithTimeout(`${BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }).then((r) => r.json()),

  login: (email: string, password: string) =>
    fetchWithTimeout(`${BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }).then((r) => r.json()),

  getMe: () => authFetch("/auth/me", { timeout: 8000 }).then((r) => r.json()),

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
