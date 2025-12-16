# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build arguments for Vite environment variables
ARG VITE_AUTH_SERVICE_URL=http://localhost:8081
ARG VITE_USER_SERVICE_URL=http://localhost:8082
ARG VITE_BLOCKER_SERVICE_URL=http://localhost:8083
ARG VITE_SOLUTION_SERVICE_URL=http://localhost:8084
ARG VITE_COMMENT_SERVICE_URL=http://localhost:8085
ARG VITE_NOTIFICATION_SERVICE_URL=http://localhost:8086

# Set environment variables for build
ENV VITE_AUTH_SERVICE_URL=$VITE_AUTH_SERVICE_URL
ENV VITE_USER_SERVICE_URL=$VITE_USER_SERVICE_URL
ENV VITE_BLOCKER_SERVICE_URL=$VITE_BLOCKER_SERVICE_URL
ENV VITE_SOLUTION_SERVICE_URL=$VITE_SOLUTION_SERVICE_URL
ENV VITE_COMMENT_SERVICE_URL=$VITE_COMMENT_SERVICE_URL
ENV VITE_NOTIFICATION_SERVICE_URL=$VITE_NOTIFICATION_SERVICE_URL

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built assets from build stage
COPY --from=build /app/dist .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 3000

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

