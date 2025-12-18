-- Migration script to update role column to support new role values
-- Run this script on the authdb database

USE authdb;

-- Alter the role column to support longer role names (ORG_ADMIN, EMPLOYEE)
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;

