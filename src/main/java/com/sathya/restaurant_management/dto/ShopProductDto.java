package com.sathya.restaurant_management.dto;

import java.math.BigDecimal;

/** What the public storefront may see of a product - deliberately has no food cost. */
public record ShopProductDto(
        String id,
        String name,
        String category,
        BigDecimal price,
        Integer stock,
        String icon,
        String color,
        String photo
) {
}
