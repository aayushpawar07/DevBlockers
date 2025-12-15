export const BLOCKER_STATUS = {
  OPEN: 'OPEN',
  IN_PROGRESS: 'IN_PROGRESS',
  RESOLVED: 'RESOLVED',
  CLOSED: 'CLOSED',
  DUPLICATE: 'DUPLICATE',
};

export const BLOCKER_SEVERITY = {
  CRITICAL: 'CRITICAL',
  HIGH: 'HIGH',
  MEDIUM: 'MEDIUM',
  LOW: 'LOW',
  TRIVIAL: 'TRIVIAL',
};

export const STATUS_COLORS = {
  OPEN: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
  RESOLVED: 'bg-green-100 text-green-800',
  CLOSED: 'bg-gray-100 text-gray-800',
  DUPLICATE: 'bg-purple-100 text-purple-800',
};

export const SEVERITY_COLORS = {
  CRITICAL: 'bg-red-100 text-red-800',
  HIGH: 'bg-orange-100 text-orange-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  LOW: 'bg-blue-100 text-blue-800',
  TRIVIAL: 'bg-gray-100 text-gray-800',
};

export const NOTIFICATION_TYPES = {
  BLOCKER_CREATED: 'BLOCKER_CREATED',
  COMMENT_ADDED: 'COMMENT_ADDED',
  SOLUTION_ADDED: 'SOLUTION_ADDED',
  SOLUTION_ACCEPTED: 'SOLUTION_ACCEPTED',
  SOLUTION_UPVOTED: 'SOLUTION_UPVOTED',
  USER_MENTIONED: 'USER_MENTIONED',
  BLOCKER_RESOLVED: 'BLOCKER_RESOLVED',
  BLOCKER_UPDATED: 'BLOCKER_UPDATED',
};

