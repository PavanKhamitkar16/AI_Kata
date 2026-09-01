import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ProtectedRoute from './routes/ProtectedRoute';

/**
 * Root route configuration.
 *
 * Route structure:
 *   /login      — public login page
 *   /           — redirects to /dashboard
 *   /dashboard  — protected; requires authentication
 *   *           — 404 fallback → /login
 */
function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      {/* All protected pages share the same guard */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<Dashboard />} />
        {/* Future story pages will be added here */}
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
