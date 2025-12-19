import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { blockerService } from '../../services/blockerService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input, Select } from '../../components/ui/Input';
import { STATUS_COLORS, SEVERITY_COLORS, BLOCKER_STATUS, BLOCKER_SEVERITY } from '../../utils/constants';
import { formatRelativeTime } from '../../utils/format';
import { PlusCircle, Search, Filter } from 'lucide-react';
import toast from 'react-hot-toast';

const TEAM_CODES = [
  { value: '', label: 'All Teams' },
  { value: 'DEVOPS', label: 'DevOps' },
  { value: 'BACKEND', label: 'Backend' },
  { value: 'FRONTEND', label: 'Frontend' },
  { value: 'QA', label: 'QA' },
];

export const BlockerList = () => {
  const [blockers, setBlockers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    status: '',
    severity: '',
    teamCode: '',
    search: '',
  });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const { user } = useAuth();

  useEffect(() => {
    fetchBlockers();
  }, [page, filters]);

  const fetchBlockers = async () => {
    try {
      setLoading(true);
      const params = {
        page,
        size: 20,
      };
      if (filters.status) params.status = filters.status;
      if (filters.severity) params.severity = filters.severity;
      if (filters.teamCode) params.teamCode = filters.teamCode;
      if (filters.search) params.search = filters.search;
      // Pass userId for priority sorting (team blockers first)
      if (user?.userId) {
        params.userId = user.userId;
      }

      const response = await blockerService.getBlockers(params);
      setBlockers(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (error) {
      toast.error('Failed to fetch blockers');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  if (loading && blockers.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">All Blockers</h1>
        <Link to="/blockers/create">
          <Button>
            <PlusCircle className="w-4 h-4" />
            New Blocker
          </Button>
        </Link>
      </div>

      {/* Filters */}
      <Card>
        <CardBody>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <Input
                placeholder="Search blockers..."
                value={filters.search}
                onChange={(e) => handleFilterChange('search', e.target.value)}
                className="pl-10"
              />
            </div>
            <Select
              options={[
                { value: '', label: 'All Statuses' },
                ...Object.values(BLOCKER_STATUS).map((status) => ({
                  value: status,
                  label: status,
                })),
              ]}
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
            />
            <Select
              options={[
                { value: '', label: 'All Severities' },
                ...Object.values(BLOCKER_SEVERITY).map((severity) => ({
                  value: severity,
                  label: severity,
                })),
              ]}
              value={filters.severity}
              onChange={(e) => handleFilterChange('severity', e.target.value)}
            />
            <Select
              options={TEAM_CODES}
              value={filters.teamCode}
              onChange={(e) => handleFilterChange('teamCode', e.target.value)}
            />
            <Button
              variant="secondary"
              onClick={() => {
                setFilters({ status: '', severity: '', teamCode: '', search: '' });
                setPage(0);
              }}
            >
              <Filter className="w-4 h-4" />
              Clear Filters
            </Button>
          </div>
        </CardBody>
      </Card>

      {/* Blockers List */}
      {blockers.length === 0 ? (
        <Card>
          <CardBody>
            <div className="text-center py-12">
              <p className="text-gray-600">No blockers found</p>
              <Link to="/blockers/create" className="mt-4 inline-block">
                <Button>Create Your First Blocker</Button>
              </Link>
            </div>
          </CardBody>
        </Card>
      ) : (
        <div className="space-y-4">
          {blockers.map((blocker) => (
            <Link
              key={blocker.blockerId}
              to={`/blockers/${blocker.blockerId}`}
              className="block"
            >
              <Card className="hover:shadow-md transition-shadow">
                <CardBody>
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <h3 className="text-xl font-semibold text-gray-900 mb-2">
                        {blocker.title}
                      </h3>
                      <p className="text-gray-600 mb-4 line-clamp-2">
                        {blocker.description}
                      </p>
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <span>{formatRelativeTime(blocker.createdAt)}</span>
                        <span>•</span>
                        <span>Created by {blocker.createdBy || 'Unknown'}</span>
                        {blocker.teamCode && (
                          <>
                            <span>•</span>
                            <Badge variant="default" className="text-xs">
                              {blocker.teamCode}
                            </Badge>
                          </>
                        )}
                        {blocker.assignedTo && (
                          <>
                            <span>•</span>
                            <span>Assigned to {blocker.assignedTo}</span>
                          </>
                        )}
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
                </CardBody>
              </Card>
            </Link>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4">
          <Button
            variant="secondary"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            Previous
          </Button>
          <span className="text-gray-600">
            Page {page + 1} of {totalPages}
          </span>
          <Button
            variant="secondary"
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};

