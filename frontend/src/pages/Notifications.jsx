import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { notificationService } from '../services/notificationService';
import { useAuth } from '../context/AuthContext';
import { Card, CardBody, CardHeader } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { formatRelativeTime } from '../utils/format';
import { Bell, CheckCircle, MessageSquare, Award, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

const getNotificationIcon = (type) => {
  switch (type) {
    case 'BLOCKER_CREATED':
    case 'BLOCKER_UPDATED':
      return <AlertCircle className="w-5 h-5 text-blue-600" />;
    case 'COMMENT_ADDED':
      return <MessageSquare className="w-5 h-5 text-green-600" />;
    case 'SOLUTION_ADDED':
    case 'SOLUTION_ACCEPTED':
      return <Award className="w-5 h-5 text-yellow-600" />;
    default:
      return <Bell className="w-5 h-5 text-gray-600" />;
  }
};

const getNotificationLink = (notification) => {
  if (notification.relatedEntityType === 'BLOCKER' && notification.relatedEntityId) {
    return `/blockers/${notification.relatedEntityId}`;
  }
  return '#';
};

export const Notifications = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all'); // 'all', 'unread'

  useEffect(() => {
    if (user?.userId) {
      fetchNotifications();
    }
  }, [user, filter]);

  const fetchNotifications = async () => {
    if (!user?.userId) return;

    try {
      setLoading(true);
      const params = filter === 'unread' ? { unreadOnly: true } : {};
      const response = await notificationService.getNotifications(user.userId, params);
      setNotifications(response.content || response || []);
    } catch (error) {
      toast.error('Failed to fetch notifications');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId) => {
    if (!user?.userId) return;

    try {
      await notificationService.markAsRead(notificationId, user.userId);
      setNotifications((prev) =>
        prev.map((n) =>
          n.notificationId === notificationId ? { ...n, read: true } : n
        )
      );
    } catch (error) {
      toast.error('Failed to mark notification as read');
      console.error(error);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (!user?.userId) return;

    try {
      const unreadNotifications = notifications.filter((n) => !n.read);
      await Promise.all(
        unreadNotifications.map((n) =>
          notificationService.markAsRead(n.notificationId, user.userId)
        )
      );
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      toast.success('All notifications marked as read');
    } catch (error) {
      toast.error('Failed to mark all as read');
      console.error(error);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Notifications</h1>
        <div className="flex items-center gap-4">
          <div className="flex gap-2">
            <button
              onClick={() => setFilter('all')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                filter === 'all'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setFilter('unread')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors relative ${
                filter === 'unread'
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              Unread
              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-danger-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {unreadCount > 9 ? '9+' : unreadCount}
                </span>
              )}
            </button>
          </div>
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllAsRead}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium"
            >
              Mark all as read
            </button>
          )}
        </div>
      </div>

      <Card>
        <CardBody>
          {notifications.length === 0 ? (
            <div className="text-center py-12">
              <Bell className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-600">No notifications found</p>
            </div>
          ) : (
            <div className="space-y-4">
              {notifications.map((notification) => {
                const link = getNotificationLink(notification);
                const NotificationContent = (
                  <div
                    className={`p-4 rounded-lg border ${
                      notification.read
                        ? 'bg-white border-gray-200'
                        : 'bg-primary-50 border-primary-200'
                    } transition-all cursor-pointer hover:shadow-md`}
                    onClick={() => {
                      if (!notification.read) {
                        handleMarkAsRead(notification.notificationId);
                      }
                    }}
                  >
                    <div className="flex items-start gap-4">
                      <div className="flex-shrink-0 mt-1">
                        {getNotificationIcon(notification.type)}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <h3 className="text-sm font-semibold text-gray-900 mb-1">
                              {notification.title}
                            </h3>
                            <p className="text-sm text-gray-600 mb-2">
                              {notification.message}
                            </p>
                            <p className="text-xs text-gray-500">
                              {formatRelativeTime(notification.createdAt)}
                            </p>
                          </div>
                          {!notification.read && (
                            <div className="flex-shrink-0 ml-4">
                              <div className="w-2 h-2 bg-primary-600 rounded-full"></div>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                );

                if (link !== '#') {
                  return (
                    <Link key={notification.notificationId} to={link}>
                      {NotificationContent}
                    </Link>
                  );
                }

                return (
                  <div key={notification.notificationId}>{NotificationContent}</div>
                );
              })}
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
};

