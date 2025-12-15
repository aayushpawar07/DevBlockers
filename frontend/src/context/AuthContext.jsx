import { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import toast from 'react-hot-toast';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = authService.getToken();
    if (token) {
      setIsAuthenticated(true);
      // Try to get userId from token or localStorage if available
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        setUser({ userId: storedUserId });
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password);
      // Extract userId from response - it might be in response.userId or response.user.userId
      const userId = response.userId || response.user?.userId;
      const userData = { email, userId };
      setUser(userData);
      if (userId) {
        localStorage.setItem('userId', userId);
      }
      setIsAuthenticated(true);
      toast.success('Login successful!');
      return response;
    } catch (error) {
      const errorData = error.response?.data;
      let errorMessage = 'Login failed';
      
      if (errorData) {
        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (typeof errorData === 'object') {
          // Handle validation errors (Map of field -> error message)
          const errors = Object.values(errorData).join(', ');
          errorMessage = errors || errorMessage;
        }
      }
      
      toast.error(errorMessage);
      throw error;
    }
  };

  const register = async (email, password) => {
    try {
      const response = await authService.register(email, password);
      toast.success('Registration successful! Please verify your email.');
      return response;
    } catch (error) {
      const errorData = error.response?.data;
      let errorMessage = 'Registration failed';
      
      if (errorData) {
        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (typeof errorData === 'object') {
          // Handle validation errors (Map of field -> error message)
          const errors = Object.values(errorData).join(', ');
          errorMessage = errors || errorMessage;
        }
      }
      
      toast.error(errorMessage);
      throw error;
    }
  };

  const sendOtp = async (email, type = 'REGISTRATION') => {
    try {
      const response = await authService.sendOtp(email, type);
      toast.success('OTP sent to your email!');
      return response;
    } catch (error) {
      const errorData = error.response?.data;
      let errorMessage = 'Failed to send OTP';
      
      if (errorData) {
        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (typeof errorData === 'object') {
          // Handle validation errors (Map of field -> error message)
          const errors = Object.values(errorData).join(', ');
          errorMessage = errors || errorMessage;
        }
      }
      
      toast.error(errorMessage);
      throw error;
    }
  };

  const verifyOtp = async (email, otp, type = 'REGISTRATION') => {
    try {
      const response = await authService.verifyOtp(email, otp, type);
      toast.success('Email verified successfully!');
      return response;
    } catch (error) {
      const errorData = error.response?.data;
      let errorMessage = 'OTP verification failed';
      
      if (errorData) {
        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (typeof errorData === 'object') {
          // Handle validation errors (Map of field -> error message)
          const errors = Object.values(errorData).join(', ');
          errorMessage = errors || errorMessage;
        }
      }
      
      toast.error(errorMessage);
      throw error;
    }
  };

  const logout = () => {
    authService.logout();
    localStorage.removeItem('userId');
    setUser(null);
    setIsAuthenticated(false);
    toast.success('Logged out successfully');
  };

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    register,
    sendOtp,
    verifyOtp,
    logout,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

