import apiClient from './authApi';

/**
 * Doctor API — all calls go through the Axios instance in authApi.js
 * which automatically attaches the Bearer token.
 */

/** POST /api/doctors — ADMIN only */
export async function createDoctor(data) {
  const res = await apiClient.post('/api/doctors', data);
  return res.data.data;
}

/** PUT /api/doctors/:id — ADMIN only */
export async function updateDoctor(id, data) {
  const res = await apiClient.put(`/api/doctors/${id}`, data);
  return res.data.data;
}

/** GET /api/doctors/:id */
export async function getDoctorById(id) {
  const res = await apiClient.get(`/api/doctors/${id}`);
  return res.data.data;
}

/**
 * GET /api/doctors — paginated + filtered.
 * @param {object} params — { specialization, department, status, activeOnly, page, size, sort }
 * @returns {Promise<{content, totalElements, totalPages, number, size}>}
 */
export async function getDoctors(params = {}) {
  const res = await apiClient.get('/api/doctors', { params });
  return res.data.data; // Spring Page object
}

/** PUT /api/doctors/:id/activate — ADMIN only */
export async function activateDoctor(id) {
  const res = await apiClient.put(`/api/doctors/${id}/activate`);
  return res.data.data;
}

/** PUT /api/doctors/:id/deactivate — ADMIN only */
export async function deactivateDoctor(id) {
  const res = await apiClient.put(`/api/doctors/${id}/deactivate`);
  return res.data.data;
}
