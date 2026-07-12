import axiosInstance from './axiosInstance.js';

export const vehicleApi = {
  create: (payload) => axiosInstance.post('/admin/vehicles', payload),
  update: (vehicleId, payload) => axiosInstance.put(`/admin/vehicles/${vehicleId}`, payload),
  getAll: (zoneId) => axiosInstance.get('/admin/vehicles', { params: zoneId ? { zoneId } : {} }),
  getActiveByZone: (zoneId) => axiosInstance.get('/admin/vehicles/active', { params: { zoneId } }),
  getById: (vehicleId) => axiosInstance.get(`/admin/vehicles/${vehicleId}`),
  setActiveStatus: (vehicleId, active) =>
    axiosInstance.patch(`/admin/vehicles/${vehicleId}/status`, null, { params: { active } }),
  logMaintenance: (vehicleId) => axiosInstance.patch(`/admin/vehicles/${vehicleId}/maintenance`),
};
