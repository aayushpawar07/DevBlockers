# DevBlocker Organization-Based Access Control Upgrade

## Overview
This document summarizes the implementation of organization-based access control for DevBlocker, enabling Teams-like group collaboration.

## ✅ Completed Backend Implementation

### 1. Database Schema Changes

#### New Tables:
- **organizations**: Stores organization information
  - `org_id` (UUID, PK)
  - `name` (String)
  - `domain` (String, unique)
  - `created_at` (Timestamp)

- **groups**: Stores groups within organizations
  - `group_id` (UUID, PK)
  - `org_id` (UUID, FK to organizations)
  - `name` (String)
  - `description` (String)
  - `created_at` (Timestamp)

- **group_members**: Junction table for group membership
  - `member_id` (UUID, PK)
  - `group_id` (UUID, FK to groups)
  - `user_id` (UUID, FK to users)
  - `joined_at` (Timestamp)

#### Updated Tables:
- **users**: Added fields
  - `name` (String) - User's display name
  - `org_id` (UUID, nullable) - Organization ID if user belongs to an org

- **blockers**: Added fields
  - `visibility` (Enum: PUBLIC, ORG, GROUP) - Default: PUBLIC
  - `org_id` (UUID, nullable) - Organization ID for ORG visibility
  - `group_id` (UUID, nullable) - Group ID for GROUP visibility

### 2. Role-Based Access Control

#### Updated Role Enum:
- `USER` - Normal user (can post/view public blockers)
- `ORG_ADMIN` - Organization administrator
- `EMPLOYEE` - Organization employee
- `ADMIN` - System admin (existing)
- `MODERATOR` - System moderator (existing)

### 3. JWT Token Updates

JWT tokens now include:
- `userId` - User ID
- `email` - User email
- `role` - User role
- `orgId` - Organization ID (if applicable)
- `groupIds` - List of group IDs user belongs to (if applicable)

### 4. Backend APIs

#### Authentication Service (`/api/v1/auth`)
- `POST /register` - Updated to include `name` field
- `POST /login` - Returns JWT with org_id and group_ids
- All existing endpoints remain functional

#### Organization APIs (`/api/v1/organizations`)
- `POST /register` - Register new organization with admin account
- `POST /{orgId}/employees` - Create employee (ORG_ADMIN only)
- `GET /{orgId}/employees` - List organization employees
- `GET /{orgId}` - Get organization details

#### Group APIs (`/api/v1/organizations/{orgId}/groups`)
- `POST /` - Create group (ORG_ADMIN only)
- `GET /` - List organization groups
- `POST /{groupId}/members/{userId}` - Add member to group (ORG_ADMIN only)
- `DELETE /{groupId}/members/{userId}` - Remove member from group (ORG_ADMIN only)
- `GET /{groupId}/members` - List group members

#### Blocker APIs (`/api/v1/blockers`)
- `POST /` - Create blocker (supports visibility, orgId, groupId)
- `GET /` - List blockers (automatically filters by access)
- `GET /{id}` - Get blocker (checks access permissions)

**Access Control:**
- PUBLIC blockers: Visible to everyone
- ORG blockers: Visible only to organization members
- GROUP blockers: Visible only to group members

### 5. Access Control Logic

The system automatically filters blockers based on:
1. User's role (USER, ORG_ADMIN, EMPLOYEE)
2. User's organization ID (if applicable)
3. User's group IDs (if applicable)

**Blocker Visibility Rules:**
- `PUBLIC`: All users can see
- `ORG`: Only users with matching `org_id` can see
- `GROUP`: Only users with matching `group_id` in their `groupIds` can see

## 🔄 Frontend Implementation (Pending)

### Required Frontend Components:

1. **Organization Registration Page**
   - Form: Organization name, domain, admin name, email, password
   - Route: `/register-organization`

2. **Organization Login Page**
   - Separate login for organization users
   - Route: `/login-organization`

3. **Organization Dashboard** (ORG_ADMIN)
   - Create employees
   - Create groups
   - Assign employees to groups
   - View organization statistics
   - Route: `/organization/dashboard`

4. **Employee Dashboard** (EMPLOYEE)
   - View assigned groups
   - View group-specific blockers only
   - Route: `/employee/dashboard`

5. **Updated Blocker Creation**
   - Add visibility selector (PUBLIC, ORG, GROUP)
   - Show group selector if user belongs to organization
   - Auto-set org_id and group_id based on selection

6. **Updated Blocker List**
   - Filter blockers based on user's access
   - Show visibility badge on each blocker
   - Hide unauthorized blockers

### Frontend Service Updates Needed:

1. **authService.js**
   - Add `registerOrganization()` method
   - Add `loginOrganization()` method
   - Extract `orgId` and `groupIds` from JWT token

2. **blockerService.js**
   - Update `createBlocker()` to include visibility, orgId, groupId
   - Update API calls to include user context headers

3. **New Services:**
   - `organizationService.js` - Organization management
   - `groupService.js` - Group management

## 🔧 Integration Notes

### JWT Token Extraction
The frontend needs to extract `orgId` and `groupIds` from the JWT token and include them in API requests:

```javascript
// Extract from JWT token
const token = localStorage.getItem('accessToken');
const payload = JSON.parse(atob(token.split('.')[1]));
const orgId = payload.orgId;
const groupIds = payload.groupIds || [];

// Include in API requests
headers: {
  'X-User-Org-Id': orgId,
  'X-User-Group-Ids': groupIds.join(',')
}
```

### Backend Header Requirements
The blocker-service expects these headers for access control:
- `X-User-Org-Id`: User's organization ID
- `X-User-Group-Ids`: Comma-separated list of group IDs

**Note:** In a production setup, these should be extracted from JWT by an API Gateway or middleware, not sent by the frontend.

## 📝 Migration Notes

### Database Migration
Run these SQL commands to add new columns to existing tables:

```sql
-- Add columns to users table
ALTER TABLE users ADD COLUMN name VARCHAR(255);
ALTER TABLE users ADD COLUMN org_id UUID;

-- Add columns to blockers table
ALTER TABLE blockers ADD COLUMN visibility VARCHAR(20) DEFAULT 'PUBLIC';
ALTER TABLE blockers ADD COLUMN org_id UUID;
ALTER TABLE blockers ADD COLUMN group_id UUID;
```

### Backward Compatibility
- Existing blockers default to `PUBLIC` visibility
- Existing users without `name` should be updated
- All existing public functionality remains intact

## 🚀 Next Steps

1. **Frontend Implementation** (Priority)
   - Create organization registration/login pages
   - Build organization dashboard
   - Update blocker creation/listing

2. **API Gateway/Middleware** (Recommended)
   - Extract user context from JWT
   - Add headers automatically
   - Remove need for frontend to send headers

3. **Testing**
   - Test organization registration
   - Test employee creation
   - Test group management
   - Test blocker visibility rules

4. **Documentation**
   - API documentation updates
   - User guide for organizations
   - Admin guide for group management

## 🔐 Security Considerations

1. **JWT Token Security**
   - Tokens include sensitive org/group info
   - Ensure tokens are stored securely
   - Implement token refresh mechanism

2. **Access Control**
   - All access checks happen server-side
   - Frontend filtering is for UX only
   - Never trust client-side permissions

3. **Data Isolation**
   - Organization data is isolated by org_id
   - Group data is isolated by group_id
   - Users can only access their organization's data

## 📚 API Examples

### Register Organization
```bash
POST /api/v1/organizations/register
{
  "organizationName": "Acme Corp",
  "domain": "acme.com",
  "adminName": "John Doe",
  "adminEmail": "admin@acme.com",
  "adminPassword": "SecurePass123"
}
```

### Create Employee
```bash
POST /api/v1/organizations/{orgId}/employees
Authorization: Bearer <ORG_ADMIN_TOKEN>
{
  "name": "Jane Smith",
  "email": "jane@acme.com",
  "password": "EmployeePass123"
}
```

### Create Group
```bash
POST /api/v1/organizations/{orgId}/groups
Authorization: Bearer <ORG_ADMIN_TOKEN>
{
  "name": "Developers",
  "description": "Development team"
}
```

### Create Organization Blocker
```bash
POST /api/v1/blockers
{
  "title": "Database Issue",
  "description": "Connection timeout",
  "severity": "HIGH",
  "visibility": "ORG",
  "orgId": "<org-id>",
  "createdBy": "<user-id>"
}
```

