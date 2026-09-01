import React, { createContext, useCallback, useContext, useState } from 'react';

// ------------------------------------------------------------------ //
//  Toast context + provider
// ------------------------------------------------------------------ //

const ToastContext = createContext(null);

let toastIdCounter = 0;

/**
 * Provides toast notifications to the whole app.
 *
 * Wrap your app (or a sub-tree) with this and call {@link useToast}
 * anywhere inside.
 *
 * @example
 * const { showToast } = useToast();
 * showToast('Patient saved.', 'success');
 * showToast('Network error.', 'error', 6000);
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const showToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = ++toastIdCounter;
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, duration);
  }, []);

  const dismiss = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <ToastContainer toasts={toasts} onDismiss={dismiss} />
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within a ToastProvider');
  return ctx;
}

// ------------------------------------------------------------------ //
//  Rendering
// ------------------------------------------------------------------ //

const TYPE_STYLES = {
  success: { background: '#276749', border: '#9ae6b4' },
  error:   { background: '#742a2a', border: '#fc8181' },
  warning: { background: '#744210', border: '#f6e05e' },
  info:    { background: '#1a365d', border: '#63b3ed' },
};

function ToastContainer({ toasts, onDismiss }) {
  if (!toasts.length) return null;

  return (
    <div style={styles.container} aria-live="polite" aria-atomic="false">
      {toasts.map(toast => (
        <ToastItem key={toast.id} toast={toast} onDismiss={onDismiss} />
      ))}
    </div>
  );
}

function ToastItem({ toast, onDismiss }) {
  const typeStyle = TYPE_STYLES[toast.type] || TYPE_STYLES.info;

  return (
    <div
      role="status"
      style={{
        ...styles.toast,
        background: typeStyle.background,
        borderLeft: `4px solid ${typeStyle.border}`,
      }}
      data-testid="toast"
    >
      <span style={styles.message}>{toast.message}</span>
      <button
        onClick={() => onDismiss(toast.id)}
        style={styles.closeBtn}
        aria-label="Dismiss notification"
      >
        x
      </button>
    </div>
  );
}

const styles = {
  container: {
    position: 'fixed',
    bottom: '24px',
    right: '24px',
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    zIndex: 9998,
    maxWidth: '380px',
  },
  toast: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '12px',
    padding: '12px 16px',
    borderRadius: '6px',
    color: '#fff',
    boxShadow: '0 4px 12px rgba(0,0,0,0.25)',
    fontSize: '14px',
    animation: 'slideIn 0.2s ease',
  },
  message:  { flex: 1 },
  closeBtn: {
    background: 'transparent',
    border: 'none',
    color: 'rgba(255,255,255,0.7)',
    cursor: 'pointer',
    fontSize: '14px',
    padding: '0 2px',
    lineHeight: 1,
  },
};
