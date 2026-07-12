import axiosInstance from './axiosInstance.js';

export const rewardApi = {
  getMyHistory: () => axiosInstance.get('/citizen/rewards'),
  awardBonus: (payload) => axiosInstance.post('/admin/rewards/bonus', payload),
};
