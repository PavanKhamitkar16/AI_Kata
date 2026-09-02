import axios from 'axios';

/**
 * Axios instance pre-configured for the hospital API.
 * The Vite dev-server proxy forwards /api/* to localhost:8080, so no
 * hard-coded base URL is needed for local development.
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

// Attach the JWT Bearer token to every request if one is available.
apiClient.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ------------------------------------------------------------------ //
//  Auth API calls
// ------------------------------------------------------------------ //

/**
 * POST /api/auth/login
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{token, tokenType, username, email, role, expiresIn}>}
 */
export async function login(username, password) {
  const response = await apiClient.post('/api/auth/login', { username, password });
  return response.data.data; // unwrap ApiResponse<LoginResponse>
}

/**
 * POST /api/auth/logout
 * Tells the server to blacklist the current token.
 */
export async function logout() {
  try {
    await apiClient.post('/api/auth/logout');
  } catch {
    // Logout best-effort — client clears token regardless of server response.
  }
}

export default apiClient;
