import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';

/**
 * Top application bar.
 * Shows the app name, logged-in user, role badge, and a logout button.
 */
function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  const roleBadgeColor = {
    ADMIN:  '#9f7aea',
    DOCTOR: '#48bb78',
    STAFF:  '#4299e1',
  }[user?.role] ?? '#a0aec0';

  return (
    <header style={styles.header} data-testid="app-header">
      <span style={styles.appName}>Hospital PMS</span>

      <div style={styles.userSection}>
        {user && (
          <>
            <span style={styles.username}>{user.username}</span>
            <span style={{ ...styles.roleBadge, background: roleBadgeColor }}>
              {user.role}
            </span>
          </>
        )}
        <button onClick={handleLogout} style={styles.logoutBtn} data-testid="logout-button">
          Sign out
        </button>
      </div>
    </header>
  );
}

const styles = {
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 24px',
    height: '56px',
    background: '#2d3748',
    color: '#fff',
    boxShadow: '0 1px 4px rgba(0,0,0,0.2)',
    flexShrink: 0,
  },
  appName:     { fontWeight: 700, fontSize: '18px', letterSpacing: '0.5px' },
  userSection: { display: 'flex', alignItems: 'center', gap: '12px' },
  username:    { fontSize: '14px', color: '#e2e8f0' },
  roleBadge:   {
    fontSize: '11px',
    fontWeight: 600,
    padding: '2px 8px',
    borderRadius: '12px',
    color: '#fff',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  logoutBtn: {
    padding: '6px 14px',
    borderRadius: '4px',
    border: '1px solid #718096',
    background: 'transparent',
    color: '#e2e8f0',
    fontSize: '13px',
    cursor: 'pointer',
  },
};

export default Header;
