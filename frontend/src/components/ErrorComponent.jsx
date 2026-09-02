import React from 'react';

/**
 * Displays an error message with an optional retry action.
 *
 * @param {string}   message   — human-readable error text
 * @param {Function} onRetry   — optional retry callback; shows a "Try again" button
 * @param {string}   variant   — 'banner' (full-width) | 'inline' (default)
 */
function ErrorComponent({ message, onRetry, variant = 'inline' }) {
  const containerStyle = variant === 'banner'
    ? { ...styles.container, ...styles.banner }
    : styles.container;

  return (
    <div role="alert" style={containerStyle} data-testid="error-component">
      <span style={styles.icon} aria-hidden="true">!</span>
      <div style={styles.body}>
        <p style={styles.message}>{message || 'Something went wrong.'}</p>
        {onRetry && (
          <button onClick={onRetry} style={styles.retryBtn}>
            Try again
          </button>
        )}
      </div>
    </div>
  );
}

const styles = {
  container: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: '10px',
    background: '#fff5f5',
    border: '1px solid #feb2b2',
    borderRadius: '6px',
    padding: '12px 16px',
    color: '#742a2a',
    fontFamily: 'system-ui, sans-serif',
  },
  banner: { borderRadius: 0, borderLeft: 'none', borderRight: 'none' },
  icon: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '20px',
    height: '20px',
    background: '#fc8181',
    color: '#fff',
    borderRadius: '50%',
    fontSize: '13px',
    fontWeight: 700,
    flexShrink: 0,
    marginTop: '1px',
  },
  body:     { flex: 1 },
  message:  { margin: 0, fontSize: '14px' },
  retryBtn: {
    marginTop: '8px',
    padding: '4px 12px',
    border: '1px solid #fc8181',
    borderRadius: '4px',
    background: 'transparent',
    color: '#c53030',
    fontSize: '13px',
    cursor: 'pointer',
  },
};

export default ErrorComponent;
