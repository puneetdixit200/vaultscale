package com.vaultscale.common.exception;

// Thrown when a user's role does not have permission for an action.
// Example: a MEMBER trying to delete an organization (only OWNER can).
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
