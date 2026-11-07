package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "Customer name is required") String name,
        String phone
) {
}
