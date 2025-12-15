import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../ui/Button';
import { Bell, LogOut, User, Home, PlusCircle } from 'lucide-react';
import { useState, useEffect } from 'react';
import { notificationService } from '../../services/notificationService';

export const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (isAuthenticated && user?.userId) {
      const fetchUnreadCount = async () => {
        try {
          const response = await notificationService.getUnreadCount(user.userId);
          setUnreadCount(response.count || 0);
        } catch (error) {
          console.error('Failed to fetch unread count:', error);
        }
      };
      fetchUnreadCount();
      const interval = setInterval(fetchUnreadCount, 30000); // Poll every 30 seconds
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-8">
            <Link to="/" className="flex items-center space-x-2">
              <span className="text-2xl font-bold text-primary-600">DevBlocker</span>
            </Link>
            {isAuthenticated && (
              <div className="hidden md:flex items-center space-x-4">
                <Link
                  to="/"
                  className="text-gray-700 hover:text-primary-600 transition-colors flex items-center gap-2"
                >
                  <Home className="w-4 h-4" />
                  Home
                </Link>
                <Link
                  to="/blockers/create"
                  className="text-gray-700 hover:text-primary-600 transition-colors flex items-center gap-2"
                >
                  <PlusCircle className="w-4 h-4" />
                  New Blocker
                </Link>
              </div>
            )}
          </div>

          <div className="flex items-center space-x-4">
            {isAuthenticated ? (
              <>
                <Link
                  to="/notifications"
                  className="relative text-gray-700 hover:text-primary-600 transition-colors"
                >
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-danger-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </Link>
                <Link
                  to="/profile"
                  className="text-gray-700 hover:text-primary-600 transition-colors"
                >
                  <User className="w-5 h-5" />
                </Link>
                <Button variant="secondary" size="sm" onClick={handleLogout}>
                  <LogOut className="w-4 h-4" />
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="secondary" size="sm">Login</Button>
                </Link>
                <Link to="/register">
                  <Button variant="primary" size="sm">Sign Up</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

