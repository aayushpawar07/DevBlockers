import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { organizationService } from '../../services/organizationService';
import { groupService } from '../../services/groupService';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { 
  Building2, 
  Users, 
  UserPlus, 
  PlusCircle, 
  Settings, 
  ArrowRight,
  X,
  CheckCircle,
  Mail,
  Lock,
  User
} from 'lucide-react';
import toast from 'react-hot-toast';

export const OrganizationDashboard = () => {
  const navigate = useNavigate();
  const [orgId, setOrgId] = useState(null);
  const [organization, setOrganization] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Modals
  const [showEmployeeModal, setShowEmployeeModal] = useState(false);
  const [showGroupModal, setShowGroupModal] = useState(false);
  
  // Employee form
  const [employeeName, setEmployeeName] = useState('');
  const [employeeEmail, setEmployeeEmail] = useState('');
  const [employeePassword, setEmployeePassword] = useState('');
  
  // Group form
  const [groupName, setGroupName] = useState('');
  const [groupDescription, setGroupDescription] = useState('');

  useEffect(() => {
    const userInfo = authService.getUserInfo();
    if (!userInfo || userInfo.role !== 'ORG_ADMIN') {
      toast.error('Access denied. Organization admin access required.');
      navigate('/dashboard');
      return;
    }
    
    if (userInfo.orgId) {
      setOrgId(userInfo.orgId);
      loadOrganizationData(userInfo.orgId);
    } else {
      toast.error('No organization found');
      navigate('/dashboard');
    }
  }, [navigate]);

  const loadOrganizationData = async (id) => {
    try {
      setLoading(true);
      const [orgData, employeesData, groupsData] = await Promise.all([
        organizationService.getOrganization(id),
        organizationService.getEmployees(id),
        groupService.getGroups(id)
      ]);
      
      setOrganization(orgData);
      setEmployees(employeesData);
      setGroups(groupsData);
    } catch (error) {
      toast.error('Failed to load organization data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateEmployee = async (e) => {
    e.preventDefault();
    
    if (employeePassword.length < 8) {
      toast.error('Password must be at least 8 characters');
      return;
    }

    try {
      await organizationService.createEmployee(orgId, {
        name: employeeName,
        email: employeeEmail,
        password: employeePassword
      });
      
      toast.success('Employee created successfully!');
      setShowEmployeeModal(false);
      setEmployeeName('');
      setEmployeeEmail('');
      setEmployeePassword('');
      loadOrganizationData(orgId);
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to create employee';
      toast.error(errorMessage);
    }
  };

  const handleCreateGroup = async (e) => {
    e.preventDefault();

    try {
      await groupService.createGroup(orgId, {
        name: groupName,
        description: groupDescription
      });
      
      toast.success('Group created successfully!');
      setShowGroupModal(false);
      setGroupName('');
      setGroupDescription('');
      loadOrganizationData(orgId);
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to create group';
      toast.error(errorMessage);
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
                <Building2 className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">{organization?.name || 'Organization'}</h1>
                <p className="text-sm text-gray-600">Organization Dashboard</p>
              </div>
            </div>
            <Button variant="outline" onClick={() => navigate('/blockers')}>
              View Blockers
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total Employees</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{employees.length}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <Users className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Groups</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{groups.length}</p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <Building2 className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Domain</p>
                <p className="text-lg font-semibold text-gray-900 mt-2">
                  {organization?.domain || 'Not set'}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </Card>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <Card className="p-6 hover:shadow-lg transition-shadow cursor-pointer" onClick={() => setShowEmployeeModal(true)}>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
                <UserPlus className="w-6 h-6 text-primary-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900">Add Employee</h3>
                <p className="text-sm text-gray-600">Create a new employee account</p>
              </div>
              <ArrowRight className="w-5 h-5 text-gray-400" />
            </div>
          </Card>

          <Card className="p-6 hover:shadow-lg transition-shadow cursor-pointer" onClick={() => setShowGroupModal(true)}>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <PlusCircle className="w-6 h-6 text-purple-600" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold text-gray-900">Create Group</h3>
                <p className="text-sm text-gray-600">Set up a new team group</p>
              </div>
              <ArrowRight className="w-5 h-5 text-gray-400" />
            </div>
          </Card>
        </div>

        {/* Employees Section */}
        <Card className="mb-8">
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <Users className="w-5 h-5" />
              Employees ({employees.length})
            </h2>
          </div>
          <div className="p-6">
            {employees.length === 0 ? (
              <div className="text-center py-12">
                <Users className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600">No employees yet</p>
                <Button className="mt-4" onClick={() => setShowEmployeeModal(true)}>
                  Add First Employee
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                {employees.map((employee) => (
                  <div key={employee.userId} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                        <User className="w-5 h-5 text-primary-600" />
                      </div>
                      <div>
                        <p className="font-semibold text-gray-900">{employee.name}</p>
                        <p className="text-sm text-gray-600">{employee.email}</p>
                      </div>
                    </div>
                    <span className="px-3 py-1 bg-blue-100 text-blue-700 text-xs font-semibold rounded-full">
                      {employee.role}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>

        {/* Groups Section */}
        <Card>
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <Building2 className="w-5 h-5" />
              Groups ({groups.length})
            </h2>
          </div>
          <div className="p-6">
            {groups.length === 0 ? (
              <div className="text-center py-12">
                <Building2 className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600">No groups yet</p>
                <Button className="mt-4" onClick={() => setShowGroupModal(true)}>
                  Create First Group
                </Button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {groups.map((group) => (
                  <div key={group.groupId} className="p-4 bg-gray-50 rounded-lg border border-gray-200">
                    <h3 className="font-semibold text-gray-900 mb-1">{group.name}</h3>
                    {group.description && (
                      <p className="text-sm text-gray-600 mb-3">{group.description}</p>
                    )}
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => navigate(`/organization/groups/${group.groupId}`)}
                    >
                      Manage Members
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>
      </div>

      {/* Create Employee Modal */}
      {showEmployeeModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900">Add Employee</h2>
                <button onClick={() => setShowEmployeeModal(false)} className="text-gray-400 hover:text-gray-600">
                  <X className="w-6 h-6" />
                </button>
              </div>
              
              <form onSubmit={handleCreateEmployee} className="space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Name</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <User className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      type="text"
                      value={employeeName}
                      onChange={(e) => setEmployeeName(e.target.value)}
                      required
                      placeholder="John Doe"
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Email</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Mail className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      type="email"
                      value={employeeEmail}
                      onChange={(e) => setEmployeeEmail(e.target.value)}
                      required
                      placeholder="employee@example.com"
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Password</label>
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <Lock className="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                      type="password"
                      value={employeePassword}
                      onChange={(e) => setEmployeePassword(e.target.value)}
                      required
                      minLength={8}
                      placeholder="••••••••"
                      className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                    />
                  </div>
                  <p className="mt-1 text-xs text-gray-500">Minimum 8 characters</p>
                </div>

                <div className="flex gap-3 pt-4">
                  <Button type="submit" className="flex-1">Create Employee</Button>
                  <Button type="button" variant="outline" onClick={() => setShowEmployeeModal(false)}>
                    Cancel
                  </Button>
                </div>
              </form>
            </div>
          </Card>
        </div>
      )}

      {/* Create Group Modal */}
      {showGroupModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900">Create Group</h2>
                <button onClick={() => setShowGroupModal(false)} className="text-gray-400 hover:text-gray-600">
                  <X className="w-6 h-6" />
                </button>
              </div>
              
              <form onSubmit={handleCreateGroup} className="space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Group Name</label>
                  <input
                    type="text"
                    value={groupName}
                    onChange={(e) => setGroupName(e.target.value)}
                    required
                    placeholder="Developers"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Description (Optional)</label>
                  <textarea
                    value={groupDescription}
                    onChange={(e) => setGroupDescription(e.target.value)}
                    placeholder="Development team"
                    rows={3}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  />
                </div>

                <div className="flex gap-3 pt-4">
                  <Button type="submit" className="flex-1">Create Group</Button>
                  <Button type="button" variant="outline" onClick={() => setShowGroupModal(false)}>
                    Cancel
                  </Button>
                </div>
              </form>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

