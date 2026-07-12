import axiosInstance from './axiosInstance.js';

export const zoneApi = {
  create: (payload) => axiosInstance.post('/admin/zones', payload),
  update: (zoneId, payload) => axiosInstance.put(`/admin/zones/${zoneId}`, payload),
  getAll: () => axiosInstance.get('/admin/zones'),
  getById: (zoneId) => axiosInstance.get(`/admin/zones/${zoneId}`),
  setActiveStatus: (zoneId, active) =>
    axiosInstance.patch(`/admin/zones/${zoneId}/status`, null, { params: { active } }),
};
