package com.sathya.restaurant_management.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(String type, String id) {
        return new ResourceNotFoundException(type + " not found with id: " + id);
    }
}
