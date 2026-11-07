package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Enter your username") @Size(max = 60, message = "Username is too long") String username,
        @NotBlank(message = "Enter your password") @Size(max = 72, message = "Password is too long") String password
) {
}
