import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { blockerService } from '../../services/blockerService';
import { solutionService } from '../../services/solutionService';
import { commentService } from '../../services/commentService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Textarea } from '../../components/ui/Input';
import { SolutionList } from '../../components/solutions/SolutionList';
import { CommentList } from '../../components/comments/CommentList';
import { STATUS_COLORS, SEVERITY_COLORS } from '../../utils/constants';
import { formatDateTime } from '../../utils/format';
import { ArrowLeft, Edit, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';

export const BlockerDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [blocker, setBlocker] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showSolutionForm, setShowSolutionForm] = useState(false);
  const [solutionContent, setSolutionContent] = useState('');
  const [submittingSolution, setSubmittingSolution] = useState(false);

  useEffect(() => {
    fetchBlocker();
  }, [id]);

  const fetchBlocker = async () => {
    try {
      setLoading(true);
      const data = await blockerService.getBlocker(id);
      setBlocker(data);
    } catch (error) {
      toast.error('Failed to fetch blocker');
      console.error(error);
      navigate('/blockers');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitSolution = async (e) => {
    e.preventDefault();
    if (!user?.userId) {
      toast.error('Please login to submit a solution');
      return;
    }

    setSubmittingSolution(true);
    try {
      await solutionService.addSolution(id, solutionContent, user.userId);
      toast.success('Solution added successfully!');
      setSolutionContent('');
      setShowSolutionForm(false);
      fetchBlocker(); // Refresh to show new solution
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add solution');
      console.error(error);
    } finally {
      setSubmittingSolution(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!blocker) {
    return null;
  }

  return (
    <div className="space-y-6">
      <Button variant="secondary" onClick={() => navigate('/blockers')}>
        <ArrowLeft className="w-4 h-4" />
        Back to Blockers
      </Button>

      {/* Blocker Details */}
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{blocker.title}</h1>
              <div className="flex items-center gap-3 mt-2">
                <Badge
                  variant={blocker.status === 'OPEN' ? 'primary' : blocker.status === 'RESOLVED' ? 'success' : 'default'}
                >
                  {blocker.status}
                </Badge>
                <Badge variant="warning">{blocker.severity}</Badge>
                {blocker.tags?.map((tag) => (
                  <Badge key={tag} variant="default">
                    {tag}
                  </Badge>
                ))}
              </div>
            </div>
            {blocker.status !== 'RESOLVED' && user?.userId === blocker.createdBy && (
              <Button variant="secondary">
                <Edit className="w-4 h-4" />
                Edit
              </Button>
            )}
          </div>
        </CardHeader>
        <CardBody>
          <div className="space-y-4">
            <div>
              <h3 className="text-sm font-medium text-gray-500 mb-1">Description</h3>
              <p className="text-gray-900 whitespace-pre-wrap">{blocker.description}</p>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-gray-200">
              <div>
                <p className="text-sm text-gray-500">Created</p>
                <p className="text-sm font-medium text-gray-900">
                  {formatDateTime(blocker.createdAt)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Created By</p>
                <p className="text-sm font-medium text-gray-900">
                  {blocker.createdBy || 'Unknown'}
                </p>
              </div>
              {blocker.assignedTo && (
                <div>
                  <p className="text-sm text-gray-500">Assigned To</p>
                  <p className="text-sm font-medium text-gray-900">{blocker.assignedTo}</p>
                </div>
              )}
              {blocker.bestSolutionId && (
                <div>
                  <p className="text-sm text-gray-500">Best Solution</p>
                  <p className="text-sm font-medium text-success-600">
                    <CheckCircle className="w-4 h-4 inline mr-1" />
                    Selected
                  </p>
                </div>
              )}
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Solutions Section */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-semibold">Solutions</h2>
            {!showSolutionForm && blocker.status !== 'RESOLVED' && (
              <Button onClick={() => setShowSolutionForm(true)}>
                Add Solution
              </Button>
            )}
          </div>
        </CardHeader>
        <CardBody>
          {showSolutionForm && (
            <form onSubmit={handleSubmitSolution} className="mb-6 pb-6 border-b border-gray-200">
              <Textarea
                label="Your Solution"
                value={solutionContent}
                onChange={(e) => setSolutionContent(e.target.value)}
                required
                rows={6}
                placeholder="Describe your solution to this blocker..."
              />
              <div className="flex gap-4 mt-4">
                <Button type="submit" loading={submittingSolution}>
                  Submit Solution
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setShowSolutionForm(false);
                    setSolutionContent('');
                  }}
                >
                  Cancel
                </Button>
              </div>
            </form>
          )}
          <SolutionList blockerId={id} blocker={blocker} onUpdate={fetchBlocker} />
        </CardBody>
      </Card>

      {/* Comments Section */}
      <Card>
        <CardHeader>
          <h2 className="text-2xl font-semibold">Comments</h2>
        </CardHeader>
        <CardBody>
          <CommentList blockerId={id} />
        </CardBody>
      </Card>
    </div>
  );
};

