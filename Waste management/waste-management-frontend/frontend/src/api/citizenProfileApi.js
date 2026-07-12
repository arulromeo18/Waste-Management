import axiosInstance from './axiosInstance.js';

/**
 * Backs Profile.js. Matches CitizenProfileController.java
 * (GET/PUT /api/citizen/profile) — added during the backend audit pass
 * after this frontend was originally built against a contract that
 * didn't exist yet server-side. Both routes resolve the citizen from the
 * authenticated JWT, so no id is ever passed from the client.
 */
export const citizenProfileApi = {
  getMyProfile: () => axiosInstance.get('/citizen/profile'),
  updateMyProfile: (payload) => axiosInstance.put('/citizen/profile', payload),
};
