-- Migration script to rename 'groups' table to 'org_groups'
-- Run this script on the authdb database

USE authdb;

-- Check if groups table exists and rename it
-- If org_groups already exists, drop groups if it exists
-- If groups exists but org_groups doesn't, rename groups to org_groups

SET @table_exists = (
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

-- If groups exists and org_groups doesn't, rename it
SET @sql = IF(@table_exists > 0 AND @org_groups_exists = 0,
    'RENAME TABLE groups TO org_groups',
    IF(@table_exists > 0 AND @org_groups_exists > 0,
        'DROP TABLE IF EXISTS groups',
        'SELECT "No migration needed" AS message'
    )
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify the change
SELECT 
    CASE 
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'authdb' AND table_name = 'org_groups')
        THEN 'org_groups table exists - Migration successful'
        ELSE 'org_groups table does not exist - Migration may have failed'
    END AS migration_status;

