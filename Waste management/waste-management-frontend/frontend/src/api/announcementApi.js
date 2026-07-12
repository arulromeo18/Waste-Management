import axiosInstance from './axiosInstance.js';

export const announcementApi = {
  create: (payload) => axiosInstance.post('/admin/announcements', payload),
  update: (announcementId, payload) => axiosInstance.put(`/admin/announcements/${announcementId}`, payload),
  deactivate: (announcementId) => axiosInstance.delete(`/admin/announcements/${announcementId}`),
  getAll: () => axiosInstance.get('/admin/announcements'),
  getById: (announcementId) => axiosInstance.get(`/admin/announcements/${announcementId}`),
  getMyFeed: () => axiosInstance.get('/announcements/feed'),
};
