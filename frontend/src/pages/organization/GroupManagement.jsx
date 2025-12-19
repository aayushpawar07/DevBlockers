import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { authService } from '../../services/authService';
import { organizationService } from '../../services/organizationService';
import { groupService } from '../../services/groupService';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { 
  Users, 
  UserPlus, 
  X, 
  ArrowLeft,
  User,
  Mail,
  CheckCircle,
  XCircle
} from 'lucide-react';
import toast from 'react-hot-toast';

export const GroupManagement = () => {
  const { orgId, groupId } = useParams();
  const navigate = useNavigate();
  const [group, setGroup] = useState(null);
  const [members, setMembers] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddMemberModal, setShowAddMemberModal] = useState(false);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState('');
  const [userRole, setUserRole] = useState(null);
  const isAdmin = userRole === 'ORG_ADMIN';

  useEffect(() => {
    const userInfo = authService.getUserInfo();
    // Allow both ORG_ADMIN and EMPLOYEE to view group management
    if (!userInfo || (userInfo.role !== 'ORG_ADMIN' && userInfo.role !== 'EMPLOYEE')) {
      toast.error('Access denied. Organization access required.');
      navigate('/dashboard');
      return;
    }
    
    setUserRole(userInfo.role);
    
    if (orgId && groupId) {
      loadGroupData();
    }
  }, [orgId, groupId, navigate]);

  const loadGroupData = async () => {
    try {
      setLoading(true);
      const [groupsData, employeesData, membersData] = await Promise.all([
        groupService.getGroups(orgId),
        organizationService.getEmployees(orgId),
        groupService.getGroupMembers(orgId, groupId)
      ]);
      
      const currentGroup = groupsData.find(g => g.groupId === groupId);
      setGroup(currentGroup);
      setEmployees(employeesData);
      setMembers(membersData);
    } catch (error) {
      toast.error('Failed to load group data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddMember = async () => {
    if (!selectedEmployeeId) {
      toast.error('Please select an employee');
      return;
    }

    try {
      await groupService.addMember(orgId, groupId, selectedEmployeeId);
      toast.success('Employee added to group successfully!');
      setShowAddMemberModal(false);
      setSelectedEmployeeId('');
      loadGroupData();
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.response?.data?.error || 'Failed to add employee to group';
      toast.error(errorMessage);
    }
  };

  const handleRemoveMember = async (userId) => {
    if (!window.confirm('Are you sure you want to remove this employee from the group?')) {
      return;
    }

    try {
      await groupService.removeMember(orgId, groupId, userId);
      toast.success('Employee removed from group successfully!');
      loadGroupData();
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.response?.data?.error || 'Failed to remove employee from group';
      toast.error(errorMessage);
    }
  };

  // Get employees not in the group
  const availableEmployees = employees.filter(emp => 
    !members.some(member => member.userId === emp.userId)
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!group) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Card className="p-8 text-center">
          <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Group Not Found</h2>
          <p className="text-gray-600 mb-6">The group you're looking for doesn't exist.</p>
          <Button onClick={() => navigate(`/organization/dashboard`)}>
            Back to Dashboard
          </Button>
        </Card>
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
              <Button 
                variant="outline" 
                onClick={() => navigate('/organization/dashboard')}
                className="flex items-center gap-2"
              >
                <ArrowLeft className="w-4 h-4" />
                Back
              </Button>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">{group.name}</h1>
                <p className="text-sm text-gray-600">
                  {group.description || 'Group management'}
                </p>
              </div>
            </div>
            {isAdmin && (
              <Button onClick={() => setShowAddMemberModal(true)}>
                <UserPlus className="w-4 h-4 mr-2" />
                Add Employee
              </Button>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total Members</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{members.length}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <Users className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Available Employees</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{availableEmployees.length}</p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <UserPlus className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total Employees</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{employees.length}</p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <Users className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </Card>
        </div>

        {/* Members List */}
        <Card>
          <div className="p-6 border-b border-gray-200">
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              <Users className="w-5 h-5" />
              Group Members ({members.length})
            </h2>
          </div>
          <div className="p-6">
            {members.length === 0 ? (
              <div className="text-center py-12">
                <Users className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600 mb-4">No members in this group yet</p>
                <Button onClick={() => setShowAddMemberModal(true)}>
                  Add First Member
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                {members.map((member) => (
                  <div key={member.memberId} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                        <User className="w-5 h-5 text-primary-600" />
                      </div>
                      <div>
                        <p className="font-semibold text-gray-900">{member.userName}</p>
                        <p className="text-sm text-gray-600">{member.userEmail}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          Joined {new Date(member.joinedAt).toLocaleDateString()}
                        </p>
                      </div>
                    </div>
                    {isAdmin && (
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => handleRemoveMember(member.userId)}
                        className="text-red-600 hover:text-red-700 hover:border-red-300"
                      >
                        <X className="w-4 h-4 mr-1" />
                        Remove
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>
      </div>

      {/* Add Member Modal */}
      {showAddMemberModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900">Add Employee to Group</h2>
                <button onClick={() => setShowAddMemberModal(false)} className="text-gray-400 hover:text-gray-600">
                  <X className="w-6 h-6" />
                </button>
              </div>
              
              {availableEmployees.length === 0 ? (
                <div className="text-center py-8">
                  <CheckCircle className="w-12 h-12 text-green-500 mx-auto mb-4" />
                  <p className="text-gray-600">All employees are already in this group!</p>
                  <Button className="mt-4" onClick={() => setShowAddMemberModal(false)}>
                    Close
                  </Button>
                </div>
              ) : (
                <>
                  <div className="mb-4">
                    <label className="block text-sm font-semibold text-gray-700 mb-2">
                      Select Employee
                    </label>
                    <select
                      value={selectedEmployeeId}
                      onChange={(e) => setSelectedEmployeeId(e.target.value)}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                    >
                      <option value="">-- Select an employee --</option>
                      {availableEmployees.map((employee) => (
                        <option key={employee.userId} value={employee.userId}>
                          {employee.name} ({employee.email})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="flex gap-3 pt-4">
                    <Button 
                      type="button" 
                      className="flex-1" 
                      onClick={handleAddMember}
                      disabled={!selectedEmployeeId}
                    >
                      Add to Group
                    </Button>
                    <Button 
                      type="button" 
                      variant="outline" 
                      onClick={() => setShowAddMemberModal(false)}
                    >
                      Cancel
                    </Button>
                  </div>
                </>
              )}
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

