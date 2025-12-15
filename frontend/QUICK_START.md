# Quick Start Guide

## Installation

1. **Navigate to the frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start the development server:**
   ```bash
   npm run dev
   ```

4. **Open your browser:**
   Navigate to `http://localhost:3000`

## Environment Variables (Optional)

Create a `.env` file in the `frontend` directory if you need to customize service URLs:

```env
VITE_AUTH_SERVICE_URL=http://localhost:8081
VITE_USER_SERVICE_URL=http://localhost:8082
VITE_BLOCKER_SERVICE_URL=http://localhost:8083
VITE_SOLUTION_SERVICE_URL=http://localhost:8084
VITE_COMMENT_SERVICE_URL=http://localhost:8085
VITE_NOTIFICATION_SERVICE_URL=http://localhost:8086
```

## Prerequisites

Make sure all backend services are running:
- Auth Service (port 8081)
- User Service (port 8082)
- Blocker Service (port 8083)
- Solution Service (port 8084)
- Comment Service (port 8085)
- Notification Service (port 8086)

## Features to Try

1. **Register a new account** - `/register`
2. **Login** - `/login`
3. **View Dashboard** - `/` (after login)
4. **Create a Blocker** - `/blockers/create`
5. **View Blockers** - `/blockers`
6. **Add Solutions** - Click on any blocker
7. **Upvote Solutions** - Click the thumbs up button
8. **Accept Best Solution** - As blocker creator
9. **Add Comments** - Threaded discussion
10. **View Notifications** - `/notifications`
11. **View Profile** - `/profile`

## Troubleshooting

### CORS Issues
If you encounter CORS errors, make sure your backend services have CORS configured to allow requests from `http://localhost:3000`.

### Authentication Issues
- Clear browser localStorage if tokens are corrupted
- Check that the auth service is running on port 8081
- Verify JWT token format in browser DevTools

### API Connection Issues
- Verify all backend services are running
- Check service URLs in `.env` file
- Check browser console for detailed error messages

## Build for Production

```bash
npm run build
```

The production build will be in the `dist` directory.

