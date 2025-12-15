import { commentApi } from './api';

export const commentService = {
  async addComment(blockerId, content, userId) {
    const response = await commentApi.post(`/blockers/${blockerId}/comments`, {
      content,
      userId,
    });
    return response.data;
  },

  async getComments(blockerId) {
    const response = await commentApi.get(`/blockers/${blockerId}/comments`);
    return response.data;
  },

  async replyToComment(commentId, content, userId) {
    const response = await commentApi.post(`/comments/${commentId}/reply`, {
      content,
      userId,
    });
    return response.data;
  },

  async getComment(commentId) {
    const response = await commentApi.get(`/comments/${commentId}`);
    return response.data;
  },
};

