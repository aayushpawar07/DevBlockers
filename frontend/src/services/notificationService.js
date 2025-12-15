import { notificationApi } from './api';

export const notificationService = {
  async getNotifications(userId, params = {}) {
    const response = await notificationApi.get('/notifications', {
      params: { userId, ...params },
    });
    return response.data;
  },

  async markAsRead(notificationId, userId) {
    const response = await notificationApi.post(`/notifications/${notificationId}/mark-read`, null, {
      params: { userId },
    });
    return response.data;
  },

  async getUnreadCount(userId) {
    const response = await notificationApi.get('/notifications/unread-count', {
      params: { userId },
    });
    return response.data;
  },
};

