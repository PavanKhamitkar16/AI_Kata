import React, { createContext, useCallback, useEffect, useState } from 'react';
import { login as apiLogin, logout as apiLogout } from '../api/authApi';

/**
 * Authentication context.
 *
 * TOKEN STORAGE STRATEGY
 * ----------------------
 * JWT is kept in React state (in-memory) as the primary store.
 * sessionStorage is used as a secondary store so the token survives
 * page refreshes within the same browser tab/session, but is cleared
 * when the tab is closed.
 *
 * SECURITY NOTE: sessionStorage (and localStorage) are accessible to
 * JavaScript and therefore vulnerable to XSS. If XSS is a concern,
 * use httpOnly cookies with a refresh-token rotation strategy instead.
 * For this hospital system the risk is mitigated by a strict CSP and
 * the short-lived nature of the access token (24 h default).
 */
export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken]   = useState(() => sessionStorage.getItem('jwt_token'));
  const [user,  setUser]    = useState(() => {
    const stored = sessionStorage.getItem('auth_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  // Sync token to sessionStorage whenever it changes.
  useEffect(() => {
    if (token) {
      sessionStorage.setItem('jwt_token', token);
    } else {
      sessionStorage.removeItem('jwt_token');
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      sessionStorage.setItem('auth_user', JSON.stringify(user));
    } else {
      sessionStorage.removeItem('auth_user');
    }
  }, [user]);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiLogin(username, password);
      setToken(data.token);
      setUser({
        username: data.username,
        email:    data.email,
        role:     data.role,
        // doctorId is populated by the backend for DOCTOR role users whose Doctor
        // profile email matches their login email. null for ADMIN / STAFF.
        doctorId: data.doctorId || null,
      });
      return data;
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error   ||
        'Login failed. Please try again.';
      setError(message);
      throw new Error(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    await apiLogout(); // best-effort server-side blacklist
    setToken(null);
    setUser(null);
    setError(null);
  }, []);

  const value = {
    token,
    user,
    loading,
    error,
    isAuthenticated: !!token,
    login,
    logout,
    clearError: () => setError(null),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
