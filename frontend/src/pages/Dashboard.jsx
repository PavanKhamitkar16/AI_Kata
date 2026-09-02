import React from 'react';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../hooks/useAuth';

/**
 * Main dashboard shell — rendered after successful login.
 * Future stories will populate the content area with patient lists,
 * appointments, etc.
 */
function Dashboard() {
  const { user } = useAuth();

  return (
    <div style={styles.layout}>
      <Sidebar />
      <div style={styles.main}>
        <Header />
        <main style={styles.content}>
          <h2>Welcome back, {user?.username}!</h2>
          <p style={{ color: '#718096' }}>
            Role: <strong>{user?.role}</strong>
          </p>
          <p style={{ color: '#718096' }}>
            Select an option from the sidebar to get started.
          </p>
        </main>
      </div>
    </div>
  );
}

const styles = {
  layout:  { display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' },
  main:    { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  content: { flex: 1, padding: '24px', background: '#f7fafc' },
};

export default Dashboard;
