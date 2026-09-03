import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import DoctorList from './pages/doctors/DoctorList';
import DoctorForm from './pages/doctors/DoctorForm';
import DoctorProfile from './pages/doctors/DoctorProfile';
import DoctorAvailability from './pages/doctors/DoctorAvailability';
import PatientList from './pages/patients/PatientList';
import PatientForm from './pages/patients/PatientForm';
import PatientProfile from './pages/patients/PatientProfile';
import MyAppointments from './pages/appointments/MyAppointments';
import AppointmentDetail from './pages/appointments/AppointmentDetail';
import BookAppointment from './pages/appointments/BookAppointment';
import AppointmentConfirmation from './pages/appointments/AppointmentConfirmation';
import ProtectedRoute from './routes/ProtectedRoute';

/**
 * Root route configuration.
 *
 * Route structure:
 *   /login                                  — public login page
 *   /                                       — redirects to /dashboard
 *   /dashboard                              — protected; requires authentication
 *   /doctors                                — doctor list (ADMIN | DOCTOR | STAFF)
 *   /doctors/new                            — register doctor form (ADMIN only)
 *   /doctors/:id                            — doctor profile (ADMIN | DOCTOR | STAFF)
 *   /doctors/:id/edit                       — edit doctor form (ADMIN only)
 *   /doctors/:id/availability               — doctor availability calendar (Story 3)
 *   /patients                               — patient list (ADMIN | STAFF | DOCTOR)
 *   /patients/new                           — register patient form (ADMIN | STAFF)
 *   /patients/:id                           — patient profile (ADMIN | STAFF | DOCTOR)
 *   /patients/:id/edit                      — edit patient form (ADMIN | STAFF)
 *   /appointments                           — my appointments list (Story 3)
 *   /appointments/book                      — book appointment wizard (Story 3)
 *   /appointments/:id                       — appointment detail (Story 3)
 *   /appointments/:id/confirmation          — post-booking confirmation (Story 3)
 *   *                                       — 404 fallback → /login
 */
function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      {/* All protected pages share the same authentication guard */}
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<Dashboard />} />

        {/* Doctor routes — Story 2 */}
        <Route path="/doctors"                    element={<DoctorList />} />
        <Route path="/doctors/new"                element={<DoctorForm />} />
        <Route path="/doctors/:id"                element={<DoctorProfile />} />
        <Route path="/doctors/:id/edit"           element={<DoctorForm />} />

        {/* Doctor availability — Story 3 */}
        <Route path="/doctors/:id/availability"   element={<DoctorAvailability />} />

        {/* Patient routes — Story 2 */}
        <Route path="/patients"                   element={<PatientList />} />
        <Route path="/patients/new"               element={<PatientForm />} />
        <Route path="/patients/:id"               element={<PatientProfile />} />
        <Route path="/patients/:id/edit"          element={<PatientForm />} />

        {/* Appointment routes — Story 3 */}
        <Route path="/appointments"               element={<MyAppointments />} />
        <Route path="/appointments/book"          element={<BookAppointment />} />
        <Route path="/appointments/:id"           element={<AppointmentDetail />} />
        <Route path="/appointments/:id/confirmation" element={<AppointmentConfirmation />} />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
