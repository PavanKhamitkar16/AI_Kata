import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import DoctorList from './pages/doctors/DoctorList';
import DoctorForm from './pages/doctors/DoctorForm';
import DoctorProfile from './pages/doctors/DoctorProfile';
import PatientList from './pages/patients/PatientList';
import PatientForm from './pages/patients/PatientForm';
import PatientProfile from './pages/patients/PatientProfile';
import ProtectedRoute from './routes/ProtectedRoute';

/**
 * Root route configuration.
 *
 * Route structure:
 *   /login                — public login page
 *   /                     — redirects to /dashboard
 *   /dashboard            — protected; requires authentication
 *   /doctors              — doctor list (ADMIN | DOCTOR | STAFF)
 *   /doctors/new          — register doctor form (ADMIN only)
 *   /doctors/:id          — doctor profile (ADMIN | DOCTOR | STAFF)
 *   /doctors/:id/edit     — edit doctor form (ADMIN only)
 *   /patients             — patient list (ADMIN | STAFF | DOCTOR)
 *   /patients/new         — register patient form (ADMIN | STAFF)
 *   /patients/:id         — patient profile (ADMIN | STAFF | DOCTOR)
 *   /patients/:id/edit    — edit patient form (ADMIN | STAFF)
 *   *                     — 404 fallback → /login
 */
function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      {/* All protected pages share the same authentication guard */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<Dashboard />} />

        {/* Doctor routes — Story 2 */}
        <Route path="/doctors"           element={<DoctorList />} />
        <Route path="/doctors/new"       element={<DoctorForm />} />
        <Route path="/doctors/:id"       element={<DoctorProfile />} />
        <Route path="/doctors/:id/edit"  element={<DoctorForm />} />

        {/* Patient routes — Story 2 */}
        <Route path="/patients"          element={<PatientList />} />
        <Route path="/patients/new"      element={<PatientForm />} />
        <Route path="/patients/:id"      element={<PatientProfile />} />
        <Route path="/patients/:id/edit" element={<PatientForm />} />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
