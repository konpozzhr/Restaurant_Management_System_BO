package com.sathya.restaurant_management.dto;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt, UserDto user) {
}
