import axios from 'axios';

/**
 * Shared Axios instance for every API call in the app.
 *
 * - baseURL is "/api" so calls read as axiosInstance.get('/admin/zones'),
 *   matching the backend's actual "/api/..." route prefixes; Vite's dev
 *   proxy (see vite.config.js) forwards this to the Spring Boot server.
 * - Request interceptor attaches the JWT from localStorage to every call
 *   automatically, so individual page components never handle auth headers.
 * - Response interceptor unwraps nothing (the backend's ApiResponse
 *   envelope — { success, message, data } — is returned as-is) but does
 *   handle a 401 globally by clearing the stored session and redirecting
 *   to /login, since an expired/invalid token means every subsequent call
 *   would fail the same way.
 */
const axiosInstance = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('cc_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('cc_token');
      localStorage.removeItem('cc_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
