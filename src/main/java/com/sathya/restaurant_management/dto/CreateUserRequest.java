package com.sathya.restaurant_management.dto;

import com.sathya.restaurant_management.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,40}$", message = "Username: 3-40 letters, digits, dot, dash or underscore") String username,
        @NotBlank(message = "Full name is required") @Size(max = 120, message = "Name is too long") String fullName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password,
        @NotNull(message = "Choose a role") Role role
) {
}
