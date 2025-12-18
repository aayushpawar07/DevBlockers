import { authApi } from './api';

export const groupService = {
  createGroup: async (orgId, groupData) => {
    const response = await authApi.post(`/organizations/${orgId}/groups`, groupData);
    return response.data;
  },

  getGroups: async (orgId) => {
    const response = await authApi.get(`/organizations/${orgId}/groups`);
    return response.data;
  },

  addMember: async (orgId, groupId, userId) => {
    const response = await authApi.post(`/organizations/${orgId}/groups/${groupId}/members/${userId}`);
    return response.data;
  },

  removeMember: async (orgId, groupId, userId) => {
    const response = await authApi.delete(`/organizations/${orgId}/groups/${groupId}/members/${userId}`);
    return response.data;
  },

  getGroupMembers: async (orgId, groupId) => {
    const response = await authApi.get(`/organizations/${orgId}/groups/${groupId}/members`);
    return response.data;
  }
};

