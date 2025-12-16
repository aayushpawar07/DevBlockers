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
    console.log('Uploading files:', Array.from(files).map(f => ({ name: f.name, size: f.size, type: f.type })));
    // Don't set Content-Type header - let axios/browser set it with boundary
    const response = await blockerApi.post('/blockers/upload', formData);
    console.log('Upload response:', response.data);
    if (!response.data || !response.data.fileUrls) {
      throw new Error('Invalid response from upload endpoint');
    }
    return response.data;
  },

  getFileUrl(url) {
    // If it's already a full URL, return it
    if (url && url.startsWith('http')) {
      return url;
    }
    // Extract file ID (UUID) from path - format is /api/v1/blockers/files/{fileId}
    // or just the fileId itself
    let fileId = url;
    if (url && url.includes('/')) {
      const parts = url.split('/');
      fileId = parts[parts.length - 1]; // Get last part (fileId)
    }
    const baseUrl = import.meta.env.VITE_BLOCKER_SERVICE_URL || 'http://localhost:8083';
    const fullUrl = `${baseUrl}/api/v1/blockers/files/${fileId}`;
    console.log(`getFileUrl: url=${url}, fileId=${fileId}, fullUrl=${fullUrl}`);
    return fullUrl;
  },
};

