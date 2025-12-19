package com.devblocker.auth.model;

public enum Role {
    USER,           // Normal user - can post/view public blockers
    ADMIN,          // System admin
    MODERATOR,      // System moderator
    ORG_ADMIN,      // Organization administrator
    EMPLOYEE        // Organization employee
}

