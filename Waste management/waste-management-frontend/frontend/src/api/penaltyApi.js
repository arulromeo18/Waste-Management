import axiosInstance from './axiosInstance.js';

export const penaltyApi = {
  issue: (payload) => axiosInstance.post('/admin/penalties', payload),
  waive: (penaltyId, waivedReason) =>
    axiosInstance.patch(`/admin/penalties/${penaltyId}/waive`, null, { params: { waivedReason } }),
  settle: (penaltyId) => axiosInstance.patch(`/admin/penalties/${penaltyId}/settle`),
  getPending: () => axiosInstance.get('/admin/penalties/pending'),
  getById: (penaltyId) => axiosInstance.get(`/admin/penalties/${penaltyId}`),
  getMine: () => axiosInstance.get('/citizen/penalties'),
};
