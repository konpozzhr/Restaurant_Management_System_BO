package com.sathya.restaurant_management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Only ids and quantities are trusted from the browser - prices are re-read from the menu. */
public record OrderRequest(
        String customerId,
        @NotEmpty(message = "An order needs at least one item") List<@Valid Line> items
) {
    public record Line(
            @NotBlank(message = "productId is required") String productId,
            @NotNull(message = "qty is required") @Min(value = 1, message = "qty must be at least 1") Integer qty
    ) {
    }
}
