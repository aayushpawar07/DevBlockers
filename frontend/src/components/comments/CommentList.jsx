import { useEffect, useState } from 'react';
import { commentService } from '../../services/commentService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Textarea } from '../../components/ui/Input';
import { formatRelativeTime } from '../../utils/format';
import { MessageSquare, Reply } from 'lucide-react';
import toast from 'react-hot-toast';

const CommentItem = ({ comment, blockerId, onReply, depth = 0 }) => {
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { user } = useAuth();

  const handleReply = async (e) => {
    e.preventDefault();
    if (!user?.userId) {
      toast.error('Please login to reply');
      return;
    }

    setSubmitting(true);
    try {
      await commentService.replyToComment(comment.commentId, replyContent, user.userId);
      toast.success('Reply added!');
      setReplyContent('');
      setShowReplyForm(false);
      if (onReply) onReply();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add reply');
      console.error(error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={depth > 0 ? 'ml-8 mt-4' : ''}>
      <Card className={depth > 0 ? 'bg-gray-50' : ''}>
        <CardBody>
          <div className="flex items-start justify-between mb-2">
            <span className="text-sm font-medium text-gray-900">
              {comment.userId || 'Unknown'}
            </span>
            <span className="text-sm text-gray-500">
              {formatRelativeTime(comment.createdAt)}
            </span>
          </div>
          <p className="text-gray-700 mb-3 whitespace-pre-wrap">{comment.content}</p>
          {depth < 3 && (
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setShowReplyForm(!showReplyForm)}
            >
              <Reply className="w-3 h-3" />
              Reply
            </Button>
          )}
          {showReplyForm && (
            <form onSubmit={handleReply} className="mt-4">
              <Textarea
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                required
                rows={3}
                placeholder="Write a reply..."
              />
              <div className="flex gap-2 mt-2">
                <Button type="submit" size="sm" loading={submitting}>
                  Post Reply
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => {
                    setShowReplyForm(false);
                    setReplyContent('');
                  }}
                >
                  Cancel
                </Button>
              </div>
            </form>
          )}
        </CardBody>
      </Card>
      {comment.replies && comment.replies.length > 0 && (
        <div className="mt-2">
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.commentId}
              comment={reply}
              blockerId={blockerId}
              onReply={onReply}
              depth={depth + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export const CommentList = ({ blockerId }) => {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    fetchComments();
  }, [blockerId]);

  const fetchComments = async () => {
    try {
      setLoading(true);
      const data = await commentService.getComments(blockerId);
      setComments(data || []);
    } catch (error) {
      toast.error('Failed to fetch comments');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.userId) {
      toast.error('Please login to comment');
      return;
    }

    setSubmitting(true);
    try {
      await commentService.addComment(blockerId, content, user.userId);
      toast.success('Comment added!');
      setContent('');
      setShowForm(false);
      fetchComments();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add comment');
      console.error(error);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {!showForm && (
        <Button onClick={() => setShowForm(true)}>
          <MessageSquare className="w-4 h-4" />
          Add Comment
        </Button>
      )}
      {showForm && (
        <Card>
          <CardBody>
            <form onSubmit={handleSubmit}>
              <Textarea
                label="Your Comment"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                required
                rows={4}
                placeholder="Share your thoughts..."
              />
              <div className="flex gap-4 mt-4">
                <Button type="submit" loading={submitting}>
                  Post Comment
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setShowForm(false);
                    setContent('');
                  }}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </CardBody>
        </Card>
      )}
      {comments.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600">No comments yet. Be the first to comment!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {comments.map((comment) => (
            <CommentItem
              key={comment.commentId}
              comment={comment}
              blockerId={blockerId}
              onReply={fetchComments}
            />
          ))}
        </div>
      )}
    </div>
  );
};

