-- Migration script to rename 'groups' table to 'org_groups'
-- Run this script on the authdb database

USE authdb;

-- Rename the groups table to org_groups to avoid MySQL reserved keyword conflict
RENAME TABLE groups TO org_groups;

-- Verify the change
SHOW TABLES LIKE 'org_groups';

