import { useState, useEffect, createContext, useContext, type ReactNode } from "react";
import { api, setTokens, clearTokens, accessToken } from "../services/api";

interface User {
  id: string;
  email: string;
  tier: string;
  credits_remaining: number;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>(null!);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (accessToken) {
      // 8s timeout — if backend is cold, skip auth and let user in as guest
      const timer = setTimeout(() => {
        setLoading(false);
      }, 8000);

      api.getMe().then((data) => {
        clearTimeout(timer);
        if (data.id) setUser(data);
        setLoading(false);
      }).catch(() => {
        clearTimeout(timer);
        clearTokens();
        setLoading(false);
      });
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    const data = await api.login(email, password);
    if (data.access_token) {
      setTokens(data.access_token, data.refresh_token);
      const me = await api.getMe();
      setUser(me);
    } else {
      throw new Error(data.detail || "Login failed");
    }
  };

  const register = async (email: string, password: string) => {
    const data = await api.register(email, password);
    if (data.access_token) {
      setTokens(data.access_token, data.refresh_token);
      const me = await api.getMe();
      setUser(me);
    } else {
      throw new Error(data.detail || "Registration failed");
    }
  };

  const logout = () => {
    clearTokens();
    setUser(null);
  };

  const refreshUser = async () => {
    try {
      const me = await api.getMe();
      if (me.id) setUser(me);
    } catch {
      // token expired or network error, ignore
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
