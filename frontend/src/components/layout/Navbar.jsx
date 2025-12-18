import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { authService } from '../../services/authService';
import { Button } from '../ui/Button';
import { Bell, LogOut, User, Home, PlusCircle, Menu, X, Search, Building2, Users } from 'lucide-react';
import { useState, useEffect } from 'react';
import { notificationService } from '../../services/notificationService';

export const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [unreadCount, setUnreadCount] = useState(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

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
      const interval = setInterval(fetchUnreadCount, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user]);

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;
  
  const userInfo = authService.getUserInfo();
  const isOrgAdmin = userInfo?.role === 'ORG_ADMIN';
  const isEmployee = userInfo?.role === 'EMPLOYEE';

  return (
    <nav className="bg-white border-b border-gray-200/80 backdrop-blur-sm sticky top-0 z-50 shadow-sm">
      <div className="max-w-[1920px] mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16 lg:h-20">
          {/* Logo and Main Navigation */}
          <div className="flex items-center space-x-4 lg:space-x-8 flex-1">
            <Link to="/" className="flex items-center space-x-2 group">
              <div className="w-8 h-8 lg:w-10 lg:h-10 bg-gradient-to-br from-primary-600 to-primary-700 rounded-lg flex items-center justify-center transform group-hover:scale-105 transition-transform duration-200">
                <span className="text-white font-bold text-lg lg:text-xl">DB</span>
              </div>
              <span className="text-xl lg:text-2xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">
                DevBlocker
              </span>
            </Link>
            
            {isAuthenticated && (
              <>
                {/* Desktop Navigation */}
                <div className="hidden lg:flex items-center space-x-1">
                  <Link
                    to="/dashboard"
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                      isActive('/dashboard')
                        ? 'bg-primary-50 text-primary-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    <Home className="w-4 h-4" />
                    Dashboard
                  </Link>
                  <Link
                    to="/blockers"
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
                      location.pathname.startsWith('/blockers') && !location.pathname.includes('/create')
                        ? 'bg-primary-50 text-primary-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    Blockers
                  </Link>
                  <Link
                    to="/blockers/create"
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                      isActive('/blockers/create')
                        ? 'bg-primary-50 text-primary-700'
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                    }`}
                  >
                    <PlusCircle className="w-4 h-4" />
                    Create
                  </Link>
                  {isOrgAdmin && (
                    <Link
                      to="/organization/dashboard"
                      className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                        isActive('/organization/dashboard')
                          ? 'bg-primary-50 text-primary-700'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                      }`}
                    >
                      <Building2 className="w-4 h-4" />
                      Organization
                    </Link>
                  )}
                  {isEmployee && (
                    <Link
                      to="/employee/dashboard"
                      className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center gap-2 ${
                        isActive('/employee/dashboard')
                          ? 'bg-primary-50 text-primary-700'
                          : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                      }`}
                    >
                      <Users className="w-4 h-4" />
                      My Dashboard
                    </Link>
                  )}
                </div>
              </>
            )}
          </div>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-2 lg:space-x-4">
            {isAuthenticated ? (
              <>
                {/* Notifications */}
                <Link
                  to="/notifications"
                  className="relative p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-lg transition-all duration-200"
                  title="Notifications"
                >
                  <Bell className="w-5 h-5 lg:w-6 lg:h-6" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 bg-red-500 text-white text-[10px] font-semibold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1.5 animate-pulse">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </Link>

                {/* Profile */}
                <Link
                  to="/profile"
                  className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-lg transition-all duration-200"
                  title="Profile"
                >
                  <User className="w-5 h-5 lg:w-6 lg:h-6" />
                </Link>

                {/* User Menu - Desktop */}
                <div className="hidden lg:flex items-center space-x-3 pl-3 border-l border-gray-200">
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">{user?.email?.split('@')[0] || 'User'}</p>
                    <p className="text-xs text-gray-500">{user?.email || ''}</p>
                  </div>
                  <Button 
                    variant="secondary" 
                    size="sm" 
                    onClick={handleLogout}
                    className="flex items-center gap-2"
                  >
                    <LogOut className="w-4 h-4" />
                    <span className="hidden xl:inline">Logout</span>
                  </Button>
                </div>

                {/* Mobile Menu Button */}
                <button
                  onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                  className="lg:hidden p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-lg transition-all duration-200"
                  aria-label="Toggle menu"
                >
                  {mobileMenuOpen ? (
                    <X className="w-6 h-6" />
                  ) : (
                    <Menu className="w-6 h-6" />
                  )}
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="hidden sm:block">
                  <Button variant="secondary" size="sm" className="text-sm font-medium">
                    Sign in
                  </Button>
                </Link>
                <Link to="/register">
                  <Button variant="primary" size="sm" className="text-sm font-medium">
                    Get started
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>

        {/* Mobile Menu */}
        {isAuthenticated && mobileMenuOpen && (
          <div className="lg:hidden border-t border-gray-200 bg-white">
            <div className="px-4 py-4 space-y-2">
              <Link
                to="/dashboard"
                className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                  isActive('/dashboard')
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Home className="w-5 h-5" />
                  Dashboard
                </div>
              </Link>
              <Link
                to="/blockers"
                className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                  location.pathname.startsWith('/blockers') && !location.pathname.includes('/create')
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                Blockers
              </Link>
              <Link
                to="/blockers/create"
                className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                  isActive('/blockers/create')
                    ? 'bg-primary-50 text-primary-700'
                    : 'text-gray-700 hover:bg-gray-50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <PlusCircle className="w-5 h-5" />
                  Create Blocker
                </div>
              </Link>
              {isOrgAdmin && (
                <Link
                  to="/organization/dashboard"
                  className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                    isActive('/organization/dashboard')
                      ? 'bg-primary-50 text-primary-700'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Building2 className="w-5 h-5" />
                    Organization
                  </div>
                </Link>
              )}
              {isEmployee && (
                <Link
                  to="/employee/dashboard"
                  className={`block px-4 py-3 rounded-lg text-base font-medium transition-all duration-200 ${
                    isActive('/employee/dashboard')
                      ? 'bg-primary-50 text-primary-700'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Users className="w-5 h-5" />
                    My Dashboard
                  </div>
                </Link>
              )}
              <Link
                to="/notifications"
                className="block px-4 py-3 rounded-lg text-base font-medium text-gray-700 hover:bg-gray-50 transition-all duration-200"
              >
                <div className="flex items-center gap-3">
                  <Bell className="w-5 h-5" />
                  Notifications
                  {unreadCount > 0 && (
                    <span className="ml-auto bg-red-500 text-white text-xs font-semibold rounded-full min-w-[20px] h-5 flex items-center justify-center px-1.5">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </div>
              </Link>
              <Link
                to="/profile"
                className="block px-4 py-3 rounded-lg text-base font-medium text-gray-700 hover:bg-gray-50 transition-all duration-200"
              >
                <div className="flex items-center gap-3">
                  <User className="w-5 h-5" />
                  Profile
                </div>
              </Link>
              <button
                onClick={handleLogout}
                className="w-full text-left px-4 py-3 rounded-lg text-base font-medium text-gray-700 hover:bg-gray-50 transition-all duration-200 flex items-center gap-3"
              >
                <LogOut className="w-5 h-5" />
                Logout
              </button>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
};

