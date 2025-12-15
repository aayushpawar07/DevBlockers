# DevBlocker Frontend

A modern React frontend for the DevBlocker microservices platform, built with Vite, React, and Tailwind CSS.

## Features

- 🔐 Authentication (Login, Register, OTP verification)
- 📊 Dashboard with blocker statistics
- 🐛 Blocker management (Create, View, List, Filter)
- 💡 Solution management (Add, Upvote, Accept)
- 💬 Threaded comments system
- 🔔 Real-time notifications
- 👤 User profile management
- 🎨 Beautiful, responsive UI with Tailwind CSS

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **React Router** - Routing
- **Tailwind CSS** - Styling
- **Axios** - HTTP client
- **React Hot Toast** - Notifications
- **Lucide React** - Icons
- **Date-fns** - Date formatting

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn/pnpm

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file in the `frontend` directory (optional, defaults are provided):
```env
VITE_AUTH_SERVICE_URL=http://localhost:8081
VITE_USER_SERVICE_URL=http://localhost:8082
VITE_BLOCKER_SERVICE_URL=http://localhost:8083
VITE_SOLUTION_SERVICE_URL=http://localhost:8084
VITE_COMMENT_SERVICE_URL=http://localhost:8085
VITE_NOTIFICATION_SERVICE_URL=http://localhost:8086
```

3. Start the development server:
```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### Build for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

## Project Structure

```
frontend/
├── src/
│   ├── components/       # Reusable UI components
│   │   ├── ui/          # Base UI components (Button, Card, Input, etc.)
│   │   ├── layout/      # Layout components (Navbar, Layout)
│   │   ├── solutions/   # Solution-related components
│   │   └── comments/     # Comment-related components
│   ├── context/         # React Context providers
│   ├── pages/           # Page components
│   │   ├── blockers/    # Blocker pages
│   │   └── ...
│   ├── services/        # API service layer
│   ├── utils/           # Utility functions
│   ├── App.jsx          # Main app component with routing
│   └── main.jsx         # Entry point
├── public/              # Static assets
├── index.html           # HTML template
├── package.json
├── vite.config.js       # Vite configuration
├── tailwind.config.js   # Tailwind configuration
└── postcss.config.js    # PostCSS configuration
```

## Features Overview

### Authentication
- User registration with email verification (OTP)
- Login with JWT tokens
- Automatic token refresh
- Protected routes

### Dashboard
- Overview statistics (Total, Open, In Progress, Resolved blockers)
- Recent blockers list
- Quick actions

### Blocker Management
- Create new blockers with title, description, severity, tags
- View blocker details
- Filter blockers by status, severity, search
- Pagination support

### Solutions
- Add solutions to blockers
- Upvote solutions (idempotent)
- Accept best solution (blocker creator only)
- View solution list sorted by upvotes

### Comments
- Add comments to blockers
- Threaded replies (nested comments)
- Real-time updates

### Notifications
- View all notifications
- Filter by read/unread
- Mark as read
- Click to navigate to related blocker

### Profile
- View and edit user profile
- Reputation display
- User statistics (solutions, accepted solutions, blockers created)

## API Integration

The frontend communicates with all microservices:
- **Auth Service** (8081) - Authentication
- **User Service** (8082) - User profiles, reputation
- **Blocker Service** (8083) - Blocker CRUD
- **Solution Service** (8084) - Solutions, upvotes, acceptance
- **Comment Service** (8085) - Comments and replies
- **Notification Service** (8086) - Notifications

All API calls are handled through service modules in `src/services/`, with automatic token injection and error handling.

## Styling

The app uses Tailwind CSS with a custom color palette:
- Primary: Blue shades
- Success: Green shades
- Danger: Red shades
- Custom utility classes in `src/index.css`

## Development

### Code Style
- Functional components with hooks
- Consistent naming conventions
- Reusable component patterns

### State Management
- React Context for authentication
- Local state for component-specific data
- Service layer for API calls

## License

MIT

