import { solutionApi } from './api';

export const solutionService = {
  async addSolution(blockerId, content, userId) {
    const response = await solutionApi.post(`/blockers/${blockerId}/solutions`, {
      content,
      userId,
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
};

