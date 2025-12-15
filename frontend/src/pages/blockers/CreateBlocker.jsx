import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { blockerService } from '../../services/blockerService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input, Textarea, Select } from '../../components/ui/Input';
import { BLOCKER_SEVERITY } from '../../utils/constants';
import toast from 'react-hot-toast';

export const CreateBlocker = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    severity: 'MEDIUM',
    tags: '',
  });
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.userId) {
      toast.error('User not authenticated');
      return;
    }

    setLoading(true);
    try {
      const blocker = await blockerService.createBlocker({
        ...formData,
        createdBy: user.userId,
        tags: formData.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
      });
      toast.success('Blocker created successfully!');
      navigate(`/blockers/${blocker.blockerId}`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create blocker');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <CardHeader>
          <h1 className="text-3xl font-bold text-gray-900">Create New Blocker</h1>
          <p className="mt-2 text-sm text-gray-600">
            Describe the issue you're facing in detail
          </p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit} className="space-y-6">
            <Input
              label="Title"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
              placeholder="Brief description of the blocker"
            />
            <Textarea
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
              rows={8}
              placeholder="Provide detailed information about the blocker, steps to reproduce, expected vs actual behavior..."
            />
            <Select
              label="Severity"
              value={formData.severity}
              onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
              options={Object.values(BLOCKER_SEVERITY).map((severity) => ({
                value: severity,
                label: severity,
              }))}
            />
            <Input
              label="Tags (comma-separated)"
              value={formData.tags}
              onChange={(e) => setFormData({ ...formData, tags: e.target.value })}
              placeholder="e.g., bug, frontend, api"
            />
            <div className="flex gap-4">
              <Button type="submit" className="flex-1" loading={loading}>
                Create Blocker
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate('/blockers')}
              >
                Cancel
              </Button>
            </div>
          </form>
        </CardBody>
      </Card>
    </div>
  );
};

