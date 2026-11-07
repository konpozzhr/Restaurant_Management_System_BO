package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required") String name,
        @NotBlank(message = "Choose a category") String category,
        @NotNull(message = "Enter a valid price") @DecimalMin(value = "0", message = "Enter a valid price") BigDecimal price,
        @NotNull(message = "Enter a valid stock count") @Min(value = 0, message = "Enter a valid stock count") Integer stock,
        String icon,
        String color,
        String photo
) {
}
