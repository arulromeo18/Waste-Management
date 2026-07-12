import axiosInstance from './axiosInstance.js';

export const authApi = {
  login: (payload) => axiosInstance.post('/auth/login', payload),
  register: (payload) => axiosInstance.post('/auth/register', payload),
  forgotPassword: (payload) => axiosInstance.post('/auth/forgot-password', payload),
  resetPassword: (payload) => axiosInstance.post('/auth/reset-password', payload),
  getActiveZonesPublic: () => axiosInstance.get('/public/zones'),
};
