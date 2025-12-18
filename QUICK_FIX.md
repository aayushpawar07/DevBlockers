# Quick Fix for 500 Errors

## Problem
The database table is still named `groups` but the code expects `org_groups`. This causes 500 errors on organization endpoints.

## Solution

### Step 1: Run the Database Migration

Open MySQL Workbench or command line and run:

```sql
USE authdb;

-- Simple fix: Rename the table
RENAME TABLE groups TO org_groups;
```

**OR** run the migration script:
- Open `fix_groups_table.sql` in MySQL Workbench
- Execute it

### Step 2: Restart the Auth Service

```bash
cd F:\Deblocker\DevBlockers
docker-compose restart auth-service
```

### Step 3: Verify

Check the logs:
```bash
docker-compose logs --tail=20 auth-service
```

You should see:
- No SQL errors about "groups" table
- Service started successfully
- No "Table doesn't exist" errors

### Step 4: Test the Endpoints

1. Open your frontend application
2. Try accessing the organization dashboard
3. The 500 errors should be resolved

## If Migration Fails

If the `groups` table doesn't exist (maybe it was never created), Hibernate will automatically create `org_groups` on the next service restart. Just restart the auth-service:

```bash
docker-compose restart auth-service
```

## Verification Query

Run this to check if the table exists with the correct name:

```sql
USE authdb;
SHOW TABLES LIKE 'org_groups';
```

If you see `org_groups` in the results, the migration was successful!

