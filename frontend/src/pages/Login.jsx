import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Login page — username/password form that calls the auth context.
 *
 * After successful login, redirects to the page the user originally
 * tried to access (via location.state.from), or to /dashboard.
 */
function Login() {
  const { login, loading, error, isAuthenticated, clearError } = useAuth();
  const navigate  = useNavigate();
  const location  = useLocation();
  const redirectTo = location.state?.from?.pathname || '/dashboard';

  const [formData, setFormData] = useState({ username: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState({});

  // If already authenticated, skip the login page.
  useEffect(() => {
    if (isAuthenticated) {
      navigate(redirectTo, { replace: true });
    }
  }, [isAuthenticated, navigate, redirectTo]);

  // Clear auth-context error whenever user starts typing again.
  function handleChange(e) {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    clearError();
    setFieldErrors(prev => ({ ...prev, [name]: '' }));
  }

  function validate() {
    const errors = {};
    if (!formData.username.trim()) errors.username = 'Username is required.';
    if (!formData.password)        errors.password = 'Password is required.';
    return errors;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length) {
      setFieldErrors(errors);
      return;
    }
    try {
      await login(formData.username, formData.password);
      // Successful login — useEffect above handles the redirect.
    } catch {
      // Error is already in AuthContext.error; nothing extra to do here.
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Hospital PMS</h1>
        <h2 style={styles.subtitle}>Sign in to your account</h2>

        {error && (
          <div role="alert" style={styles.errorBanner} data-testid="login-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate style={styles.form}>
          <div style={styles.field}>
            <label htmlFor="username" style={styles.label}>Username</label>
            <input
              id="username"
              name="username"
              type="text"
              autoComplete="username"
              value={formData.username}
              onChange={handleChange}
              disabled={loading}
              style={{
                ...styles.input,
                ...(fieldErrors.username ? styles.inputError : {}),
              }}
              data-testid="username-input"
            />
            {fieldErrors.username && (
              <span style={styles.fieldError} role="alert">
                {fieldErrors.username}
              </span>
            )}
          </div>

          <div style={styles.field}>
            <label htmlFor="password" style={styles.label}>Password</label>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              value={formData.password}
              onChange={handleChange}
              disabled={loading}
              style={{
                ...styles.input,
                ...(fieldErrors.password ? styles.inputError : {}),
              }}
              data-testid="password-input"
            />
            {fieldErrors.password && (
              <span style={styles.fieldError} role="alert">
                {fieldErrors.password}
              </span>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            style={loading ? { ...styles.button, ...styles.buttonDisabled } : styles.button}
            data-testid="login-button"
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
      </div>
    </div>
  );
}

// Inline styles — replace with Tailwind / CSS Modules in later sprints.
const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#f0f4f8',
    fontFamily: 'system-ui, sans-serif',
  },
  card: {
    background: '#fff',
    borderRadius: '8px',
    boxShadow: '0 2px 12px rgba(0,0,0,0.1)',
    padding: '40px',
    width: '100%',
    maxWidth: '400px',
  },
  title: { textAlign: 'center', margin: '0 0 4px', fontSize: '24px', color: '#1a202c' },
  subtitle: { textAlign: 'center', margin: '0 0 24px', fontSize: '16px', color: '#718096', fontWeight: 400 },
  errorBanner: {
    background: '#fff5f5',
    border: '1px solid #fc8181',
    borderRadius: '4px',
    color: '#c53030',
    padding: '10px 14px',
    marginBottom: '16px',
    fontSize: '14px',
  },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  field: { display: 'flex', flexDirection: 'column', gap: '4px' },
  label: { fontSize: '14px', fontWeight: 500, color: '#4a5568' },
  input: {
    padding: '10px 12px',
    borderRadius: '4px',
    border: '1px solid #cbd5e0',
    fontSize: '15px',
    outline: 'none',
    transition: 'border-color 0.2s',
  },
  inputError: { borderColor: '#fc8181' },
  fieldError: { fontSize: '12px', color: '#c53030' },
  button: {
    padding: '11px',
    borderRadius: '4px',
    border: 'none',
    background: '#3182ce',
    color: '#fff',
    fontSize: '15px',
    fontWeight: 600,
    cursor: 'pointer',
    marginTop: '4px',
  },
  buttonDisabled: { background: '#a0aec0', cursor: 'not-allowed' },
};

export default Login;
