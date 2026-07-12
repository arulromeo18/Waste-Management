import axiosInstance from './axiosInstance.js';

export const collectionRecordApi = {
  logCollection: (payload) => axiosInstance.post('/worker/collection-records', payload),
  getMyRecords: () => axiosInstance.get('/worker/collection-records'),
  getRecordsForZone: (zoneId) => axiosInstance.get(`/admin/collection-records/zone/${zoneId}`),
  getRecordsForZoneInRange: (zoneId, start, end) =>
    axiosInstance.get(`/admin/collection-records/zone/${zoneId}/range`, { params: { start, end } }),
  getNonCompliant: (start, end) =>
    axiosInstance.get('/admin/collection-records/non-compliant', { params: { start, end } }),
};
