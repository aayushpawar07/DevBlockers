import api from './api';

import { authApi } from './api';

export const organizationService = {
  register: async (organizationData) => {
    const response = await authApi.post('/organizations/register', organizationData);
    return response.data;
  },

  getOrganization: async (orgId) => {
    const response = await authApi.get(`/organizations/${orgId}`);
    return response.data;
  },

  createEmployee: async (orgId, employeeData) => {
    const response = await authApi.post(`/organizations/${orgId}/employees`, employeeData);
    return response.data;
  },

  getEmployees: async (orgId) => {
    const response = await authApi.get(`/organizations/${orgId}/employees`);
    return response.data;
  }
};

