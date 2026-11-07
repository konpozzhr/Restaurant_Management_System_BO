package com.sathya.restaurant_management.dto;

/** What the browser may know about a user - never the password hash. role is ADMIN | MANAGER | CASHIER | WAITER | KITCHEN. */
public record UserDto(String id, String username, String fullName, String role, boolean enabled) {
}
