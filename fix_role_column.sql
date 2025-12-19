-- Fix role column size to support ORG_ADMIN and EMPLOYEE roles
-- This script updates the users table in authdb to support longer role names

USE authdb;

-- Alter the role column to support longer role names (ORG_ADMIN = 9 chars, EMPLOYEE = 8 chars)
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;

-- Verify the change
DESCRIBE users;

