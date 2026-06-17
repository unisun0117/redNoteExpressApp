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
}

const AuthContext = createContext<AuthContextType>(null!);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (accessToken) {
      api.getMe().then((data) => {
        if (data.id) setUser(data);
        setLoading(false);
      }).catch(() => {
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

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
