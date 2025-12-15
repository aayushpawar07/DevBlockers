import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services/userService';
import { Card, CardBody, CardHeader } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Input, Textarea } from '../components/ui/Input';
import { Award, TrendingUp, User } from 'lucide-react';
import toast from 'react-hot-toast';

export const Profile = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [reputation, setReputation] = useState(0);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    bio: '',
    location: '',
  });

  useEffect(() => {
    if (user?.userId) {
      fetchProfile();
      fetchReputation();
    }
  }, [user]);

  const fetchProfile = async () => {
    if (!user?.userId) return;

    try {
      setLoading(true);
      const data = await userService.getProfile(user.userId);
      setProfile(data);
      setFormData({
        name: data.name || user?.email?.split('@')[0] || 'User',
        bio: data.bio || '',
        location: data.location || '',
      });
    } catch (error) {
      // If profile doesn't exist, create it with default values
      if (error.response?.status === 400 || error.response?.status === 404) {
        const defaultName = user?.email?.split('@')[0] || 'User';
        setFormData({
          name: defaultName,
          bio: '',
          location: '',
        });
        // Profile will be created when user saves
        toast.info('Profile will be created when you save');
      } else {
        toast.error('Failed to fetch profile');
        console.error(error);
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchReputation = async () => {
    if (!user?.userId) return;

    try {
      const data = await userService.getReputation(user.userId);
      setReputation(data.points || 0);
    } catch (error) {
      console.error('Failed to fetch reputation:', error);
    }
  };

  const handleSave = async () => {
    if (!user?.userId) return;

    // Validate name is not empty
    if (!formData.name || formData.name.trim() === '') {
      toast.error('Name is required');
      return;
    }

    try {
      await userService.updateProfile(user.userId, formData);
      toast.success('Profile updated successfully!');
      setEditing(false);
      fetchProfile();
    } catch (error) {
      // Handle validation errors from backend
      const errorData = error.response?.data;
      console.error('Profile update error:', error);
      console.error('Error response data:', errorData);
      console.error('Form data sent:', formData);
      
      if (errorData) {
        // Check if it's a validation error with field names
        if (errorData.name) {
          toast.error(`Name: ${errorData.name}`);
        } else if (errorData.error) {
          toast.error(errorData.error);
        } else if (errorData.message) {
          toast.error(errorData.message);
        } else {
          // If it's an object with multiple fields, show first error
          const firstError = Object.values(errorData)[0];
          toast.error(firstError || 'Failed to update profile');
        }
      } else {
        toast.error(error.message || 'Failed to update profile');
      }
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
    <div className="max-w-4xl mx-auto space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Profile</h1>

      {/* Profile Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 bg-primary-600 rounded-full flex items-center justify-center text-white text-2xl font-bold">
                {profile?.name?.charAt(0)?.toUpperCase() || user?.email?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div>
                <h2 className="text-2xl font-bold text-gray-900">
                  {profile?.name || user?.email || 'User'}
                </h2>
                <p className="text-gray-600">{user?.email}</p>
              </div>
            </div>
            {!editing && (
              <Button variant="secondary" onClick={() => setEditing(true)}>
                Edit Profile
              </Button>
            )}
          </div>
        </CardHeader>
        <CardBody>
          {editing ? (
            <div className="space-y-4">
              <Input
                label="Name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Your name"
                required
              />
              <Textarea
                label="Bio"
                value={formData.bio}
                onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                rows={4}
                placeholder="Tell us about yourself..."
              />
              <Input
                label="Location"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                placeholder="Your location"
              />
              <div className="flex gap-4">
                <Button onClick={handleSave}>Save Changes</Button>
                <Button
                  variant="secondary"
                  onClick={() => {
                    setEditing(false);
                    setFormData({
                      name: profile?.name || '',
                      bio: profile?.bio || '',
                      location: profile?.location || '',
                    });
                  }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {profile?.bio && (
                <div>
                  <h3 className="text-sm font-medium text-gray-500 mb-1">Bio</h3>
                  <p className="text-gray-900">{profile.bio}</p>
                </div>
              )}
              {profile?.location && (
                <div>
                  <h3 className="text-sm font-medium text-gray-500 mb-1">Location</h3>
                  <p className="text-gray-900">{profile.location}</p>
                </div>
              )}
            </div>
          )}
        </CardBody>
      </Card>

      {/* Reputation Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <Award className="w-6 h-6 text-yellow-600" />
            <h2 className="text-xl font-semibold">Reputation</h2>
          </div>
        </CardHeader>
        <CardBody>
          <div className="flex items-center gap-4">
            <div className="text-4xl font-bold text-primary-600">{reputation}</div>
            <div>
              <p className="text-sm text-gray-600">Total reputation points</p>
              <p className="text-xs text-gray-500 mt-1">
                Earned by providing solutions and helping others
              </p>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Stats Card */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Solutions</p>
                <p className="text-2xl font-bold text-gray-900">
                  {profile?.solutionsCount || 0}
                </p>
              </div>
              <TrendingUp className="w-8 h-8 text-primary-600" />
            </div>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Accepted Solutions</p>
                <p className="text-2xl font-bold text-gray-900">
                  {profile?.acceptedSolutionsCount || 0}
                </p>
              </div>
              <Award className="w-8 h-8 text-yellow-600" />
            </div>
          </CardBody>
        </Card>
        <Card>
          <CardBody>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Blockers Created</p>
                <p className="text-2xl font-bold text-gray-900">
                  {profile?.blockersCount || 0}
                </p>
              </div>
              <User className="w-8 h-8 text-blue-600" />
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};

