import { blockerApi } from './api';

export const blockerService = {
  async createBlocker(data) {
    const response = await blockerApi.post('/blockers', data);
    return response.data;
  },

  async getBlockers(params = {}) {
    const response = await blockerApi.get('/blockers', { params });
    return response.data;
  },

  async getBlocker(blockerId) {
    const response = await blockerApi.get(`/blockers/${blockerId}`);
    return response.data;
  },

  async updateBlocker(blockerId, data) {
    const response = await blockerApi.put(`/blockers/${blockerId}`, data);
    return response.data;
  },

  async resolveBlocker(blockerId, bestSolutionId, resolvedBy) {
    const response = await blockerApi.post(`/blockers/${blockerId}/resolve`, {
      bestSolutionId,
    }, {
      headers: {
        'X-User-Id': resolvedBy,
      },
    });
    return response.data;
  },

  async updateBestSolution(blockerId, bestSolutionId) {
    const response = await blockerApi.put(`/blockers/${blockerId}/best-solution`, {
      bestSolutionId,
    });
    return response.data;
  },
};

