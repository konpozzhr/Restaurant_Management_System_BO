package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Positive = stock in, negative = stock out. */
public record InventoryAdjustRequest(
        @NotNull(message = "delta is required") BigDecimal delta
) {
}
