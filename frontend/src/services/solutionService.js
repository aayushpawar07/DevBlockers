import { solutionApi } from './api';

export const solutionService = {
  async addSolution(blockerId, content, userId, mediaUrls = []) {
    const response = await solutionApi.post(`/blockers/${blockerId}/solutions`, {
      content,
      userId,
      mediaUrls,
    });
    return response.data;
  },

  async getSolutions(blockerId) {
    const response = await solutionApi.get(`/blockers/${blockerId}/solutions`);
    return response.data;
  },

  async upvoteSolution(solutionId, userId) {
    const response = await solutionApi.post(`/solutions/${solutionId}/upvote`, {
      userId,
    });
    return response.data;
  },

  async acceptSolution(solutionId, userId) {
    const response = await solutionApi.post(`/solutions/${solutionId}/accept`, {
      userId,
    });
    return response.data;
  },

  async getSolution(solutionId) {
    const response = await solutionApi.get(`/solutions/${solutionId}`);
    return response.data;
  },

  async uploadFiles(files) {
    const formData = new FormData();
    Array.from(files).forEach((file) => {
      formData.append('files', file);
    });
    // Don't set Content-Type header - let axios/browser set it with boundary
    const response = await solutionApi.post('/solutions/upload', formData);
    return response.data;
  },

  getFileUrl(url) {
    // If it's already a full URL, return it
    if (url.startsWith('http')) {
      return url;
    }
    // Extract file ID (UUID) from path - format is /api/v1/solutions/files/{fileId}
    const fileId = url.includes('/') ? url.split('/').pop() : url;
    return `${import.meta.env.VITE_SOLUTION_SERVICE_URL || 'http://localhost:8084'}/api/v1/solutions/files/${fileId}`;
  },
};

