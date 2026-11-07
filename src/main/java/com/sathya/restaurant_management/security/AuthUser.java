package com.sathya.restaurant_management.security;

import com.sathya.restaurant_management.model.Role;

/** The signed-in person as seen by controllers: inject with @AuthenticationPrincipal AuthUser. */
public record AuthUser(String id, String username, String fullName, Role role) {
}
