import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { organizationService } from '../../services/organizationService';
import { groupService } from '../../services/groupService';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { 
  Users, 
  Building2, 
  ArrowRight,
  User,
  CheckCircle
} from 'lucide-react';
import toast from 'react-hot-toast';

export const EmployeeDashboard = () => {
  const navigate = useNavigate();
  const [orgId, setOrgId] = useState(null);
  const [organization, setOrganization] = useState(null);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const userInfo = authService.getUserInfo();
    if (!userInfo || userInfo.role !== 'EMPLOYEE') {
      toast.error('Access denied. Employee access required.');
      navigate('/dashboard');
      return;
    }
    
    if (userInfo.orgId) {
      setOrgId(userInfo.orgId);
      loadEmployeeData(userInfo.orgId, userInfo.groupIds || []);
    } else {
      toast.error('No organization found');
      navigate('/dashboard');
    }
  }, [navigate]);

  const loadEmployeeData = async (orgId, groupIds) => {
    try {
      setLoading(true);
      const [orgData, allGroups] = await Promise.all([
        organizationService.getOrganization(orgId),
        groupService.getGroups(orgId)
      ]);
      
      setOrganization(orgData);
      
      // Filter to show only groups the employee belongs to
      const userGroups = allGroups.filter(group => 
        groupIds.includes(group.groupId)
      );
      setGroups(userGroups);
    } catch (error) {
      toast.error('Failed to load employee data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-gradient-to-br from-primary-600 to-primary-700 rounded-xl flex items-center justify-center">
                <User className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">My Dashboard</h1>
                <p className="text-sm text-gray-600">{organization?.name || 'Organization'}</p>
              </div>
            </div>
            <Button variant="outline" onClick={() => navigate('/blockers')}>
              View Blockers
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Card */}
        <Card className="mb-8 p-6 bg-gradient-to-r from-primary-50 to-purple-50 border-primary-200">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 bg-white rounded-xl flex items-center justify-center shadow-lg">
              <CheckCircle className="w-8 h-8 text-primary-600" />
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Welcome!</h2>
              <p className="text-gray-600">You're part of {organization?.name || 'the organization'}</p>
            </div>
          </div>
        </Card>

        {/* My Groups Section */}
        <Card>
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <Users className="w-5 h-5" />
              My Groups ({groups.length})
            </h2>
            <p className="text-sm text-gray-600 mt-1">Groups you're assigned to</p>
          </div>
          <div className="p-6">
            {groups.length === 0 ? (
              <div className="text-center py-12">
                <Users className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600 mb-2">You're not assigned to any groups yet</p>
                <p className="text-sm text-gray-500">Contact your organization admin to be added to a group</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {groups.map((group) => (
                  <div 
                    key={group.groupId} 
                    className="p-6 bg-gradient-to-br from-white to-gray-50 rounded-lg border border-gray-200 hover:shadow-lg transition-all cursor-pointer"
                    onClick={() => navigate(`/blockers?group=${group.groupId}`)}
                  >
                    <div className="flex items-start justify-between mb-4">
                      <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
                        <Building2 className="w-6 h-6 text-primary-600" />
                      </div>
                      <CheckCircle className="w-5 h-5 text-green-500" />
                    </div>
                    <h3 className="font-semibold text-gray-900 mb-2">{group.name}</h3>
                    {group.description && (
                      <p className="text-sm text-gray-600 mb-4 line-clamp-2">{group.description}</p>
                    )}
                    <Button 
                      variant="outline" 
                      size="sm"
                      className="w-full"
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`/blockers?group=${group.groupId}`);
                      }}
                    >
                      View Blockers
                      <ArrowRight className="w-4 h-4 ml-2" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>

        {/* Quick Actions */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card className="p-6 hover:shadow-lg transition-shadow cursor-pointer" onClick={() => navigate('/blockers/create')}>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
                <ArrowRight className="w-6 h-6 text-primary-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900">Create Blocker</h3>
                <p className="text-sm text-gray-600">Report a new blocker</p>
              </div>
            </div>
          </Card>

          <Card className="p-6 hover:shadow-lg transition-shadow cursor-pointer" onClick={() => navigate('/blockers')}>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <Building2 className="w-6 h-6 text-purple-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900">View All Blockers</h3>
                <p className="text-sm text-gray-600">See blockers in your groups</p>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
};

