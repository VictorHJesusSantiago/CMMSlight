import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { api, saveCredentials, clearCredentials, getCredentials } from '../api/client';
import type { AppUser } from '../types';

interface AuthContextValue {
  user: AppUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(null);
  const [loading, setLoading] = useState(true);

  async function fetchMe() {
    try {
      const { data } = await api.get<AppUser>('/auth/me');
      setUser(data);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (getCredentials()) {
      fetchMe();
    } else {
      setLoading(false);
    }
  }, []);

  async function login(email: string, password: string) {
    saveCredentials(email, password);
    await fetchMe();
  }

  function logout() {
    clearCredentials();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider');
  return ctx;
}
