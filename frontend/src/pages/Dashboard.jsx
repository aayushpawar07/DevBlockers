import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { blockerService } from '../services/blockerService';
import { Card, CardBody, CardHeader } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { STATUS_COLORS, SEVERITY_COLORS } from '../utils/constants';
import { formatRelativeTime } from '../utils/format';
import { PlusCircle, AlertCircle, CheckCircle, Clock, TrendingUp } from 'lucide-react';
import toast from 'react-hot-toast';

export const Dashboard = () => {
  const navigate = useNavigate();
  const [blockers, setBlockers] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    open: 0,
    inProgress: 0,
    resolved: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Redirect organization users to organization dashboard
    const userInfo = authService.getUserInfo();
    if (userInfo && userInfo.orgId && (userInfo.role === 'ORG_ADMIN' || userInfo.role === 'EMPLOYEE')) {
      navigate('/organization/dashboard', { replace: true });
      return;
    }
    
    fetchBlockers();
  }, [navigate]);

  const fetchBlockers = async () => {
    try {
      setLoading(true);
      // Fetch more blockers to get enough unsolved ones after filtering
      const response = await blockerService.getBlockers({ page: 0, size: 20, sort: 'createdAt,desc' });
      const allBlockers = response.content || [];
      
      // Filter out RESOLVED and CLOSED blockers - only show OPEN and IN_PROGRESS (unsolved)
      const unsolvedBlockers = allBlockers.filter(b => b.status !== 'RESOLVED' && b.status !== 'CLOSED').slice(0, 10);
      setBlockers(unsolvedBlockers);
      
      // Calculate stats from all blockers (for stats display)
      setStats({
        total: allBlockers.length,
        open: allBlockers.filter(b => b.status === 'OPEN').length,
        inProgress: allBlockers.filter(b => b.status === 'IN_PROGRESS').length,
        resolved: allBlockers.filter(b => b.status === 'RESOLVED').length,
      });
    } catch (error) {
      toast.error('Failed to fetch blockers');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <Link to="/blockers/create">
          <Button>
            <PlusCircle className="w-4 h-4" />
            New Blocker
          </Button>
        </Link>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Blockers</p>
                <p className="text-2xl font-bold text-gray-900">{stats.total}</p>
              </div>
              <AlertCircle className="w-8 h-8 text-primary-600" />
            </div>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Open</p>
                <p className="text-2xl font-bold text-gray-900">{stats.open}</p>
              </div>
              <Clock className="w-8 h-8 text-yellow-600" />
            </div>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">In Progress</p>
                <p className="text-2xl font-bold text-gray-900">{stats.inProgress}</p>
              </div>
              <TrendingUp className="w-8 h-8 text-blue-600" />
            </div>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Resolved</p>
                <p className="text-2xl font-bold text-gray-900">{stats.resolved}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-600" />
            </div>
          </CardBody>
        </Card>
      </div>

      {/* Recent Blockers */}
      <Card>
        <CardHeader>
          <h2 className="text-xl font-semibold">Recent Blockers</h2>
        </CardHeader>
        <CardBody>
          {blockers.length === 0 ? (
            <div className="text-center py-12">
              <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-600">No blockers found</p>
              <Link to="/blockers/create" className="mt-4 inline-block">
                <Button>Create Your First Blocker</Button>
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              {blockers.map((blocker) => (
                <Link
                  key={blocker.blockerId}
                  to={`/blockers/${blocker.blockerId}`}
                  className="block p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-md transition-all"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-gray-900 mb-2">
                        {blocker.title}
                      </h3>
                      <p className="text-gray-600 text-sm mb-3 line-clamp-2">
                        {blocker.description}
                      </p>
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <span>{formatRelativeTime(blocker.createdAt)}</span>
                        <span>•</span>
                        <span>Created by {blocker.createdBy || 'Unknown'}</span>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-2 ml-4">
                      <Badge
                        variant={blocker.status === 'OPEN' ? 'primary' : blocker.status === 'RESOLVED' ? 'success' : 'default'}
                      >
                        {blocker.status}
                      </Badge>
                      <Badge variant="warning">
                        {blocker.severity}
                      </Badge>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
          {blockers.length > 0 && (
            <div className="mt-6 text-center">
              <Link to="/blockers">
                <Button variant="secondary">View All Blockers</Button>
              </Link>
            </div>
          )}
        </CardBody>
      </Card>
    </div>
  );
};

