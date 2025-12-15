import { useEffect, useState } from 'react';
import { solutionService } from '../../services/solutionService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { formatRelativeTime } from '../../utils/format';
import { ThumbsUp, CheckCircle, Award } from 'lucide-react';
import toast from 'react-hot-toast';

export const SolutionList = ({ blockerId, blocker, onUpdate }) => {
  const [solutions, setSolutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [upvoting, setUpvoting] = useState({});
  const [accepting, setAccepting] = useState({});
  const { user } = useAuth();

  useEffect(() => {
    fetchSolutions();
  }, [blockerId]);

  const fetchSolutions = async () => {
    try {
      setLoading(true);
      const data = await solutionService.getSolutions(blockerId);
      // Sort by upvotes descending, then by accepted status
      const sorted = (data || []).sort((a, b) => {
        if (a.accepted && !b.accepted) return -1;
        if (!a.accepted && b.accepted) return 1;
        return (b.upvotes || 0) - (a.upvotes || 0);
      });
      setSolutions(sorted);
    } catch (error) {
      toast.error('Failed to fetch solutions');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpvote = async (solutionId) => {
    if (!user?.userId) {
      toast.error('Please login to upvote');
      return;
    }

    setUpvoting({ ...upvoting, [solutionId]: true });
    try {
      await solutionService.upvoteSolution(solutionId, user.userId);
      toast.success('Solution upvoted!');
      fetchSolutions();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to upvote');
      console.error(error);
    } finally {
      setUpvoting({ ...upvoting, [solutionId]: false });
    }
  };

  const handleAccept = async (solutionId) => {
    if (!user?.userId) {
      toast.error('Please login to accept solution');
      return;
    }

    if (blocker?.createdBy !== user.userId) {
      toast.error('Only the blocker creator can accept a solution');
      return;
    }

    setAccepting({ ...accepting, [solutionId]: true });
    try {
      await solutionService.acceptSolution(solutionId, user.userId);
      toast.success('Solution accepted as best!');
      fetchSolutions();
      if (onUpdate) onUpdate();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to accept solution');
      console.error(error);
    } finally {
      setAccepting({ ...accepting, [solutionId]: false });
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (solutions.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-600">No solutions yet. Be the first to provide a solution!</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {solutions.map((solution) => (
        <Card
          key={solution.solutionId}
          className={solution.accepted ? 'border-2 border-success-500' : ''}
        >
          <CardBody>
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-3">
                {solution.accepted && (
                  <Badge variant="success" className="flex items-center gap-1">
                    <Award className="w-3 h-3" />
                    Accepted Solution
                  </Badge>
                )}
                <span className="text-sm text-gray-500">
                  by {solution.userId || 'Unknown'} • {formatRelativeTime(solution.createdAt)}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => handleUpvote(solution.solutionId)}
                  disabled={upvoting[solution.solutionId]}
                >
                  <ThumbsUp className="w-4 h-4" />
                  {solution.upvotes || 0}
                </Button>
                {!solution.accepted &&
                  blocker?.createdBy === user?.userId &&
                  blocker?.status !== 'RESOLVED' && (
                    <Button
                      variant="success"
                      size="sm"
                      onClick={() => handleAccept(solution.solutionId)}
                      disabled={accepting[solution.solutionId]}
                    >
                      <CheckCircle className="w-4 h-4" />
                      Accept
                    </Button>
                  )}
              </div>
            </div>
            <p className="text-gray-900 whitespace-pre-wrap">{solution.content}</p>
          </CardBody>
        </Card>
      ))}
    </div>
  );
};

