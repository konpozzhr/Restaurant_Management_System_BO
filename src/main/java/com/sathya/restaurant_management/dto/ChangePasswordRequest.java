package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Enter your current password") String currentPassword,
        @NotBlank(message = "Enter a new password")
        @Size(min = 8, max = 72, message = "New password must be 8-72 characters") String newPassword
) {
}
