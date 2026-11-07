package com.sathya.restaurant_management.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentMethodRequest(
        @NotBlank(message = "paymentMethod is required") String paymentMethod
) {
}
