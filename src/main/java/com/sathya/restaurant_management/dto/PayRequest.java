package com.sathya.restaurant_management.dto;

/** Optional body for POST /orders/{id}/pay - lets the card flow set the method and pay in one call. */
public record PayRequest(String paymentMethod) {
}
