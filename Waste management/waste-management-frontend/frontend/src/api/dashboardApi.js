import axiosInstance from './axiosInstance.js';

export const dashboardApi = {
  getStats: () => axiosInstance.get('/admin/dashboard/stats'),
};

/**
 * Report/export endpoints return raw binary (PDF/CSV/XLSX) bytes, so each
 * call sets responseType: 'blob' and the caller is responsible for
 * triggering the browser download (see utils/downloadFile.js).
 */
export const reportApi = {
  downloadDashboardPdf: () =>
    axiosInstance.get('/admin/reports/dashboard/pdf', { responseType: 'blob' }),
  exportCitizensCsv: () => axiosInstance.get('/admin/reports/citizens/csv', { responseType: 'blob' }),
  exportCitizensExcel: () => axiosInstance.get('/admin/reports/citizens/excel', { responseType: 'blob' }),
  exportWorkersCsv: () => axiosInstance.get('/admin/reports/workers/csv', { responseType: 'blob' }),
  exportWorkersExcel: () => axiosInstance.get('/admin/reports/workers/excel', { responseType: 'blob' }),
  exportComplaintsCsv: () => axiosInstance.get('/admin/reports/complaints/csv', { responseType: 'blob' }),
  exportComplaintsExcel: () =>
    axiosInstance.get('/admin/reports/complaints/excel', { responseType: 'blob' }),
  exportCollectionsCsv: (params) =>
    axiosInstance.get('/admin/reports/collections/csv', { params, responseType: 'blob' }),
  exportCollectionsExcel: (params) =>
    axiosInstance.get('/admin/reports/collections/excel', { params, responseType: 'blob' }),
};

export const notificationApi = {
  getMine: (unreadOnly = false) => axiosInstance.get('/notifications', { params: { unreadOnly } }),
  getUnreadCount: () => axiosInstance.get('/notifications/unread-count'),
  markAsRead: (notificationId) => axiosInstance.patch(`/notifications/${notificationId}/read`),
  markAllAsRead: () => axiosInstance.patch('/notifications/read-all'),
};

export const uploadApi = {
  uploadComplaintImages: (files) => {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    return axiosInstance.post('/uploads/complaints', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  uploadCollectionImages: (files) => {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    return axiosInstance.post('/uploads/collections', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
