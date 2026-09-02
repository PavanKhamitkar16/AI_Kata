import { useContext } from 'react';
import { AuthContext } from '../contexts/AuthContext';

/**
 * Convenience hook — provides the full auth context.
 *
 * Throws an error if used outside of {@link AuthProvider}, catching
 * mis-use early during development.
 *
 * @returns {{ token, user, loading, error, isAuthenticated, login, logout, clearError }}
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
