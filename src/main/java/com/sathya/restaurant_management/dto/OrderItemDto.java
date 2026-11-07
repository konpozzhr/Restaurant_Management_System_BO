package com.sathya.restaurant_management.dto;

import java.math.BigDecimal;

public record OrderItemDto(String productId, String name, BigDecimal price, Integer qty) {
}
