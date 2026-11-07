package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record InventoryItemRequest(
        @NotBlank(message = "Item name is required.") String name,
        @NotBlank(message = "Category is required.") String category,
        @NotBlank(message = "Unit is required.") String unit,
        @DecimalMin(value = "0", message = "Stock can't be negative.") BigDecimal stock,
        @DecimalMin(value = "0", message = "Par level can't be negative.") BigDecimal par,
        @DecimalMin(value = "0", message = "Cost can't be negative.") BigDecimal cost,
        /** Days until the item expires; defaults to 7. */
        @Min(value = 0, message = "Shelf life can't be negative.") Integer shelfLife
) {
}
