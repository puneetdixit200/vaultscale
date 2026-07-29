package com.vaultscale.organization.entity;

// Enum = a fixed set of allowed values.
// OWNER  -> created the org, full control, can delete org
// ADMIN  -> can manage members and collections, cannot delete org
// MEMBER -> can create/edit collections, cannot manage members
// VIEWER -> read-only access
public enum Role {
    OWNER,
    ADMIN,
    MEMBER,
    VIEWER
}
