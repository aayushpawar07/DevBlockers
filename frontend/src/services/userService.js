import { userApi } from './api';

export const userService = {
  async getUserTeams(userId) {
    const response = await userApi.get(`/users/${userId}/teams`);
    return response.data;
  },

  async getTeamMembers(teamCode) {
    const response = await userApi.get(`/teams/code/${teamCode}/members`);
    return response.data;
  },
};
