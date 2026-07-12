import axiosInstance from './axiosInstance.js';

export const scheduleApi = {
  create: (payload) => axiosInstance.post('/admin/schedules', payload),
  update: (scheduleId, payload) => axiosInstance.put(`/admin/schedules/${scheduleId}`, payload),
  getHistoryForZone: (zoneId) => axiosInstance.get(`/admin/schedules/zone/${zoneId}/history`),
  getActiveForZoneAdmin: (zoneId) => axiosInstance.get(`/admin/schedules/zone/${zoneId}/active`),
  getMyZoneSchedule: () => axiosInstance.get('/citizen/schedule'),
  getMySchedule: (todayOnly = true) =>
    axiosInstance.get('/worker/schedule', { params: { todayOnly } }),
};
