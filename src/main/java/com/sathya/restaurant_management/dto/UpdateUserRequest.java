package com.sathya.restaurant_management.dto;

import com.sathya.restaurant_management.model.Role;
import jakarta.validation.constraints.Size;

/** Every field is optional - only the ones that are sent are changed. A non-null password is a reset. */
public record UpdateUserRequest(
        @Size(min = 1, max = 120, message = "Name can't be empty") String fullName,
        Role role,
        Boolean enabled,
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password
) {
}
