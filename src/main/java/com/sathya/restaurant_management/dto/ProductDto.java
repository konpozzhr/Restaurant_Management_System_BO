package com.sathya.restaurant_management.dto;

import java.math.BigDecimal;

/** category is the category NAME, which is what the React app already works with. */
public record ProductDto(
        String id,
        String name,
        String category,
        BigDecimal price,
        Integer stock,
        String icon,
        String color,
        String photo,
        BigDecimal cost
) {
}
