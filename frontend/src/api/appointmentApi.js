import apiClient from './authApi';

/**
 * Appointment API — all calls go through the Axios instance in authApi.js
 * which automatically attaches the Bearer token.
 */

/** GET /api/doctors/:doctorId/schedule */
export async function getDoctorSchedule(doctorId) {
  const res = await apiClient.get(`/api/doctors/${doctorId}/schedule`);
  return res.data.data;
}

/**
 * GET /api/doctors/:doctorId/available-slots?date=YYYY-MM-DD
 * @returns {Promise<Array<{startTime, endTime, available}>>}
 */
export async function getAvailableSlots(doctorId, date) {
  const res = await apiClient.get(`/api/doctors/${doctorId}/available-slots`, {
    params: { date },
  });
  return res.data.data;
}

/**
 * POST /api/appointments
 * @param {{ patientId, doctorId, date, startTime, reason }} data
 */
export async function createAppointment(data) {
  const res = await apiClient.post('/api/appointments', data);
  return res.data.data;
}

/**
 * GET /api/appointments
 * Returns all appointments in the system, newest first.
 * Only accessible to ADMIN and STAFF roles.
 */
export async function getAllAppointments() {
  const res = await apiClient.get('/api/appointments');
  return res.data.data;
}

/** GET /api/appointments/:id */
export async function getAppointment(id) {
  const res = await apiClient.get(`/api/appointments/${id}`);
  return res.data.data;
}

/** GET /api/appointments/patient/:patientId */
export async function getPatientAppointments(patientId) {
  const res = await apiClient.get(`/api/appointments/patient/${patientId}`);
  return res.data.data;
}

/** GET /api/appointments/doctor/:doctorId */
export async function getDoctorAppointments(doctorId) {
  const res = await apiClient.get(`/api/appointments/doctor/${doctorId}`);
  return res.data.data;
}

/** PUT /api/appointments/:id/confirm */
export async function confirmAppointment(id) {
  const res = await apiClient.put(`/api/appointments/${id}/confirm`);
  return res.data.data;
}

/** PUT /api/appointments/:id/cancel */
export async function cancelAppointment(id) {
  const res = await apiClient.put(`/api/appointments/${id}/cancel`);
  return res.data.data;
}

/**
 * PUT /api/appointments/:id/reschedule
 * @param {{ newDate: string, newStartTime: string }} data
 */
export async function rescheduleAppointment(id, data) {
  const res = await apiClient.put(`/api/appointments/${id}/reschedule`, data);
  return res.data.data;
}

/** PUT /api/appointments/:id/complete */
export async function completeAppointment(id) {
  const res = await apiClient.put(`/api/appointments/${id}/complete`);
  return res.data.data;
}

/** PUT /api/appointments/:id/no-show */
export async function markNoShow(id) {
  const res = await apiClient.put(`/api/appointments/${id}/no-show`);
  return res.data.data;
}
