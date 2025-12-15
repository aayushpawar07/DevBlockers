import { authApi } from './api';

export const authService = {
  async register(email, password) {
    const response = await authApi.post('/auth/register', { email, password });
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
};

