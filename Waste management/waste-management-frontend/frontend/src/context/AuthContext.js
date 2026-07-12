import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { authApi } from '../api/authApi.js';

/**
 * Central session store for the whole app. Wraps AuthResponse
 * (token, userId, fullName, email, role) in localStorage so a page
 * refresh doesn't log the user out, and exposes login/register/logout
 * actions plus the current `user` object to every component via context.
 *
 * Deliberately thin: it does not re-fetch the full profile (Citizen/Worker
 * document) on every load — pages that need those richer fields (address,
 * zoneId, rewardPoints, etc.) fetch them from their own dedicated
 * "my profile" endpoints as needed, keeping this context focused purely on
 * "who is logged in and what role are they."
 */
const AuthContext = createContext(null);

const STORAGE_TOKEN_KEY = 'cc_token';
const STORAGE_USER_KEY = 'cc_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(STORAGE_TOKEN_KEY);
    const storedUser = localStorage.getItem(STORAGE_USER_KEY);
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem(STORAGE_TOKEN_KEY);
        localStorage.removeItem(STORAGE_USER_KEY);
      }
    }
    setInitializing(false);
  }, []);

  const persistSession = useCallback((authResponse) => {
    const sessionUser = {
      userId: authResponse.userId,
      fullName: authResponse.fullName,
      email: authResponse.email,
      role: authResponse.role,
    };
    localStorage.setItem(STORAGE_TOKEN_KEY, authResponse.token);
    localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(sessionUser));
    setUser(sessionUser);
    return sessionUser;
  }, []);

  const login = useCallback(
    async (email, password) => {
      const { data } = await authApi.login({ email, password });
      return persistSession(data.data);
    },
    [persistSession]
  );

  const register = useCallback(
    async (payload) => {
      const { data } = await authApi.register(payload);
      return persistSession(data.data);
    },
    [persistSession]
  );

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_TOKEN_KEY);
    localStorage.removeItem(STORAGE_USER_KEY);
    setUser(null);
  }, []);

  const value = { user, initializing, login, register, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
