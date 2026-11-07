package com.sathya.restaurant_management.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryLogDto(Long id, Instant at, String name, BigDecimal delta, String unit, String kind) {
}
