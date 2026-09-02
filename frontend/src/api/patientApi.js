import apiClient from './authApi';

/**
 * Patient API — all calls go through the Axios instance in authApi.js
 * which automatically attaches the Bearer token.
 */

/** POST /api/patients — ADMIN | STAFF */
export async function createPatient(data) {
  const res = await apiClient.post('/api/patients', data);
  return res.data.data;
}

/** PUT /api/patients/:id — ADMIN | STAFF */
export async function updatePatient(id, data) {
  const res = await apiClient.put(`/api/patients/${id}`, data);
  return res.data.data;
}

/** GET /api/patients/:id */
export async function getPatientById(id) {
  const res = await apiClient.get(`/api/patients/${id}`);
  return res.data.data;
}

/**
 * GET /api/patients — paginated + filtered.
 * @param {object} params — { name, bloodGroup, activeOnly, page, size, sort }
 * @returns {Promise<{content, totalElements, totalPages, number, size}>}
 */
export async function getPatients(params = {}) {
  const res = await apiClient.get('/api/patients', { params });
  return res.data.data; // Spring Page object
}

/** PUT /api/patients/:id/activate — ADMIN only */
export async function activatePatient(id) {
  const res = await apiClient.put(`/api/patients/${id}/activate`);
  return res.data.data;
}

/** PUT /api/patients/:id/deactivate — ADMIN only */
export async function deactivatePatient(id) {
  const res = await apiClient.put(`/api/patients/${id}/deactivate`);
  return res.data.data;
}
