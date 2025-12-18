-- IMMEDIATE FIX: Run this in MySQL Workbench NOW
-- This will fix the 500 errors immediately

USE authdb;

-- Step 1: Check current state
SELECT 'Current tables:' AS info;
SHOW TABLES LIKE '%group%';

-- Step 2: Rename groups to org_groups (if groups exists)
SET @groups_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = 'authdb' 
    AND table_name = 'groups'
);

SET @org_groups_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = 'authdb' 
    AND table_name = 'org_groups'
);

-- Execute rename
SET @sql = CASE
    WHEN @groups_exists > 0 AND @org_groups_exists = 0 THEN
        'RENAME TABLE groups TO org_groups'
    WHEN @groups_exists > 0 AND @org_groups_exists > 0 THEN
        'DROP TABLE IF EXISTS groups'
    ELSE
        'SELECT "org_groups table already exists or groups does not exist" AS message'
END;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 3: Verify
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'authdb' AND table_name = 'org_groups')
        THEN '✅ SUCCESS: org_groups table exists'
        ELSE '⚠️ WARNING: org_groups table does not exist'
    END AS result;

-- Step 4: Show final state
SELECT 'Final tables:' AS info;
SHOW TABLES LIKE '%group%';

