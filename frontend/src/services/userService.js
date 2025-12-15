import { userApi } from './api';

export const userService = {
  async getProfile(userId) {
    const response = await userApi.get(`/users/${userId}`);
    return response.data;
  },

  async updateProfile(userId, data) {
    const response = await userApi.put(`/users/${userId}`, data);
    return response.data;
  },

  async getReputation(userId) {
    const response = await userApi.get(`/users/${userId}/reputation`);
    return response.data;
  },

  async incrementReputation(userId, points, reason, source) {
    const response = await userApi.post(`/users/${userId}/reputation/increment`, {
      points,
      reason,
      source,
    });
    return response.data;
  },

  async getReputationHistory(userId, page = 0, size = 20) {
    const response = await userApi.get(`/users/${userId}/reputation/history`, {
      params: { page, size },
    });
    return response.data;
  },

  async getUserBadges(userId) {
    const response = await userApi.get(`/users/${userId}/badges`);
    return response.data;
  },

  async searchUsers(params) {
    const response = await userApi.get('/users/search', { params });
    return response.data;
  },
};

