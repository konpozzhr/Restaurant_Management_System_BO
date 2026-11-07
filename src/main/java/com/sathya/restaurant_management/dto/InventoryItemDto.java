package com.sathya.restaurant_management.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryItemDto(
        String id,
        String name,
        String category,
        String unit,
        BigDecimal stock,
        BigDecimal par,
        BigDecimal critical,
        BigDecimal cost,
        Instant expiresAt,
        Instant updatedAt
) {
}
