-- Simple migration: Rename groups table to org_groups
-- Run this in MySQL Workbench or command line

USE authdb;

-- Check if groups table exists
SELECT 'Checking for groups table...' AS status;

-- Rename groups to org_groups if groups exists
-- If org_groups already exists, we'll drop the old groups table
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

-- Execute migration
SET @sql = CASE
    WHEN @groups_exists > 0 AND @org_groups_exists = 0 THEN
        'RENAME TABLE groups TO org_groups'
    WHEN @groups_exists > 0 AND @org_groups_exists > 0 THEN
        'DROP TABLE groups'
    ELSE
        'SELECT "No migration needed - org_groups table already exists or groups table does not exist" AS message'
END;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'authdb' AND table_name = 'org_groups')
        THEN 'SUCCESS: org_groups table exists'
        ELSE 'WARNING: org_groups table does not exist - Hibernate will create it on next startup'
    END AS result;

