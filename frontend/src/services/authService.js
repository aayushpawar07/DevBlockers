import { authApi } from './api';

export const authService = {
  async register(name, email, password) {
    const response = await authApi.post('/auth/register', { name, email, password });
    return response.data;
  },

  async registerOrganization(organizationData) {
    const response = await authApi.post('/auth/organizations/register', organizationData);
    return response.data;
  },

  async sendOtp(email, type = 'REGISTRATION') {
    const response = await authApi.post('/auth/send-otp', { email, type });
    return response.data;
  },

  async verifyOtp(email, otp, type = 'REGISTRATION') {
    const response = await authApi.post('/auth/verify-otp', { email, code: otp, type });
    return response.data;
  },

  async login(email, password) {
    const response = await authApi.post('/auth/login', { email, password });
    if (response.data.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
    }
    return response.data;
  },

  async refreshToken(refreshToken) {
    const response = await authApi.post('/auth/refresh', { refreshToken });
    if (response.data.accessToken) {
      localStorage.setItem('accessToken', response.data.accessToken);
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  },

  isAuthenticated() {
    return !!localStorage.getItem('accessToken');
  },

  getToken() {
    return localStorage.getItem('accessToken');
  },

  // Extract user info from JWT token
  getUserInfo() {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        userId: payload.userId,
        email: payload.email,
        role: payload.role,
        orgId: payload.orgId || null,
        groupIds: payload.groupIds || []
      };
    } catch (error) {
      console.error('Error parsing JWT token:', error);
      return null;
    }
  },

  getOrgId() {
    const userInfo = this.getUserInfo();
    return userInfo?.orgId || null;
  },

  getGroupIds() {
    const userInfo = this.getUserInfo();
    return userInfo?.groupIds || [];
  },

  getRole() {
    const userInfo = this.getUserInfo();
    return userInfo?.role || null;
  },

  isOrgAdmin() {
    return this.getRole() === 'ORG_ADMIN';
  },

  isEmployee() {
    return this.getRole() === 'EMPLOYEE';
  },
};

