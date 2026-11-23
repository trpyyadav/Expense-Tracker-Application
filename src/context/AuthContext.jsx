import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api';
import { setUnauthorizedHandler, tokenStore } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => (tokenStore.get() ? tokenStore.getUser() : null));
  const [sessionExpired, setSessionExpired] = useState(false);

  const logout = useCallback(() => {
    tokenStore.clear();
    setUser(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      tokenStore.clear();
      setUser(null);
      setSessionExpired(true);
    });
  }, []);

  const login = useCallback(async (email, password) => {
    const res = await authApi.login({ email, password });
    tokenStore.set(res.token);
    const u = { id: res.userId, name: res.name, email: res.email };
    tokenStore.setUser(u);
    setSessionExpired(false);
    setUser(u);
  }, []);

  const register = useCallback(
    async (name, email, password) => {
      await authApi.register({ name, email, password });
      // Registration does not return a token, so sign in right after.
      await login(email, password);
    },
    [login]
  );

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      sessionExpired,
      clearSessionExpired: () => setSessionExpired(false),
      login,
      register,
      logout,
    }),
    [user, sessionExpired, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
