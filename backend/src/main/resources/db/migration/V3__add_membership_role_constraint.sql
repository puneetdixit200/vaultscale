-- V3__add_membership_role_constraint.sql
-- Adds a CHECK constraint so only valid roles can exist in org_memberships.
-- This is a database-level safety net for RBAC (Role-Based Access Control).

ALTER TABLE org_memberships
ADD CONSTRAINT chk_membership_role
CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER'));
