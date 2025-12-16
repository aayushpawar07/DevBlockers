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

  async uploadFiles(files) {
    const formData = new FormData();
    Array.from(files).forEach((file) => {
      formData.append('files', file);
    });
    const response = await blockerApi.post('/blockers/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getFileUrl(url) {
    // If it's already a full URL, return it
    if (url.startsWith('http')) {
      return url;
    }
    // Extract file ID (UUID) from path - format is /api/v1/blockers/files/{fileId}
    const fileId = url.includes('/') ? url.split('/').pop() : url;
    return `${import.meta.env.VITE_BLOCKER_SERVICE_URL || 'http://localhost:8083'}/api/v1/blockers/files/${fileId}`;
  },
};

