import axios from 'axios';

const API_BASE_URLS = {
  auth: import.meta.env.VITE_AUTH_SERVICE_URL || 'http://localhost:8081',
  user: import.meta.env.VITE_USER_SERVICE_URL || 'http://localhost:8082',
  blocker: import.meta.env.VITE_BLOCKER_SERVICE_URL || 'http://localhost:8083',
  solution: import.meta.env.VITE_SOLUTION_SERVICE_URL || 'http://localhost:8084',
  comment: import.meta.env.VITE_COMMENT_SERVICE_URL || 'http://localhost:8085',
  notification: import.meta.env.VITE_NOTIFICATION_SERVICE_URL || 'http://localhost:8086',
};

// Create axios instances for each service
const createApiClient = (baseURL) => {
  const client = axios.create({
    baseURL: `${baseURL}/api/v1`,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Add auth token to requests
  client.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // Handle token refresh on 401
  client.interceptors.response.use(
    (response) => response,
    async (error) => {
      if (error.response?.status === 401) {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          try {
            const response = await axios.post(`${API_BASE_URLS.auth}/api/v1/auth/refresh`, {
              refreshToken,
            });
            const { accessToken } = response.data;
            localStorage.setItem('accessToken', accessToken);
            error.config.headers.Authorization = `Bearer ${accessToken}`;
            return axios.request(error.config);
          } catch (refreshError) {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        }
      }
      return Promise.reject(error);
    }
  );

  return client;
};

export const authApi = createApiClient(API_BASE_URLS.auth);
export const userApi = createApiClient(API_BASE_URLS.user);
export const blockerApi = createApiClient(API_BASE_URLS.blocker);
export const solutionApi = createApiClient(API_BASE_URLS.solution);
export const commentApi = createApiClient(API_BASE_URLS.comment);
export const notificationApi = createApiClient(API_BASE_URLS.notification);

