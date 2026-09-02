import React from 'react';

/**
 * Full-page or inline loading indicator.
 *
 * @param {boolean} fullPage  — when true, fills the viewport; default false
 * @param {string}  size      — 'sm' | 'md' | 'lg'; default 'md'
 * @param {string}  label     — accessible sr-only label; default 'Loading…'
 */
function LoadingSpinner({ fullPage = false, size = 'md', label = 'Loading...' }) {
  const diameter = { sm: 24, md: 40, lg: 64 }[size] || 40;

  const spinner = (
    <div
      role="status"
      aria-label={label}
      style={{
        width:  diameter,
        height: diameter,
        border: `${diameter * 0.1}px solid #e2e8f0`,
        borderTopColor: '#3182ce',
        borderRadius: '50%',
        animation: 'spin 0.8s linear infinite',
      }}
    >
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      <span style={srOnly}>{label}</span>
    </div>
  );

  if (fullPage) {
    return (
      <div
        style={{
          position: 'fixed', inset: 0,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'rgba(255,255,255,0.8)',
          zIndex: 9999,
        }}
      >
        {spinner}
      </div>
    );
  }

  return spinner;
}

const srOnly = {
  position: 'absolute',
  width: '1px',
  height: '1px',
  padding: 0,
  margin: '-1px',
  overflow: 'hidden',
  clip: 'rect(0,0,0,0)',
  whiteSpace: 'nowrap',
  border: 0,
};

export default LoadingSpinner;
