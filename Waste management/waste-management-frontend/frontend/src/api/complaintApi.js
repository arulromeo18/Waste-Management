import axiosInstance from './axiosInstance.js';

export const complaintApi = {
  file: (payload) => axiosInstance.post('/citizen/complaints', payload),
  getMine: () => axiosInstance.get('/citizen/complaints'),
  getMyComplaintById: (complaintId) => axiosInstance.get(`/citizen/complaints/${complaintId}`),
  getForAdmin: (params) => axiosInstance.get('/admin/complaints', { params }),
  getByIdForAdmin: (complaintId) => axiosInstance.get(`/admin/complaints/${complaintId}`),
  updateStatus: (complaintId, status, resolutionRemarks) =>
    axiosInstance.patch(`/admin/complaints/${complaintId}/status`, null, {
      params: { status, resolutionRemarks },
    }),
};
