package com.sathya.restaurant_management.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** status is lower-case ("pending" | "paid" | "cancelled") to match the frontend. */
public record OrderDto(
        String id,
        Long number,
        String invoiceCode,
        List<OrderItemDto> items,
        String customerId,
        String paymentMethod,
        BigDecimal subtotal,
        BigDecimal total,
        String status,
        Instant createdAt,
        Instant paidAt
) {
}
