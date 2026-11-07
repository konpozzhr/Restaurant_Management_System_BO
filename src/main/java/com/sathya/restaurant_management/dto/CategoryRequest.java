package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name can't be empty.")
        @Size(max = 100, message = "Category name is too long.")
        String name
) {
}
