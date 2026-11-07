package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CostRequest(
        @NotNull(message = "cost is required") @DecimalMin(value = "0", message = "cost can't be negative") BigDecimal cost
) {
}
