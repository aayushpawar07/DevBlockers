# Manual Fix for 500 Errors

## Problem
The Docker build is failing due to network issues, but the main problem is:
1. Database table is still named `groups` (MySQL reserved keyword)
2. The running container has old code

## Quick Fix (Do This First)

### Step 1: Rename the Database Table

Open MySQL Workbench and run:

```sql
USE authdb;

-- Check if groups table exists
SHOW TABLES LIKE 'groups';

-- If it exists, rename it
RENAME TABLE groups TO org_groups;

-- Verify
SHOW TABLES LIKE 'org_groups';
```

### Step 2: Restart Auth Service

```bash
cd F:\Deblocker\DevBlockers
docker-compose restart auth-service
```

### Step 3: Check Logs

```bash
docker-compose logs --tail=30 auth-service
```

You should see the service started successfully. If you see errors about `org_groups` table not existing, Hibernate will create it automatically.

## If Build Network Issue Persists

The Docker build is failing due to network connectivity. You can:

1. **Wait and retry** - Network issues are usually temporary
2. **Build locally** (if Maven is installed):
   ```bash
   cd F:\Deblocker\DevBlockers\auth
   mvn clean package -DskipTests
   docker-compose build auth-service
   ```

## After Fix

Test the endpoints:
- `/api/v1/organizations/{orgId}` - Should return 200
- `/api/v1/organizations/{orgId}/groups` - Should return 200
- `/api/v1/organizations/{orgId}/employees` - Should return 200

