package com.sathya.restaurant_management.model;

/** What a signed-in person is allowed to do. See SecurityConfig for the rules per endpoint. */
public enum Role {
    ADMIN,
    MANAGER,
    CASHIER,
    WAITER,
    KITCHEN
}
