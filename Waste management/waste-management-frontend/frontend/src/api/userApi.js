import axiosInstance from './axiosInstance.js';

export const userApi = {
  createWorker: (payload) => axiosInstance.post('/admin/users/workers', payload),
  getAllWorkers: () => axiosInstance.get('/admin/users/workers'),
  getAllCitizens: () => axiosInstance.get('/admin/users/citizens'),
  getUserById: (userId) => axiosInstance.get(`/admin/users/${userId}`),
  setUserActiveStatus: (userId, active) =>
    axiosInstance.patch(`/admin/users/${userId}/status`, null, { params: { active } }),
  assignWorker: (workerId, payload) =>
    axiosInstance.post(`/admin/workers/${workerId}/assign`, payload),
};
