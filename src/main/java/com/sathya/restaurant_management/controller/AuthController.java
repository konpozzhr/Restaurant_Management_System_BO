package com.sathya.restaurant_management.controller;

import com.sathya.restaurant_management.dto.ChangePasswordRequest;
import com.sathya.restaurant_management.dto.LoginRequest;
import com.sathya.restaurant_management.dto.LoginResponse;
import com.sathya.restaurant_management.dto.UserDto;
import com.sathya.restaurant_management.security.AuthUser;
import com.sathya.restaurant_management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Body: { "username": "admin", "password": "..." } -> { token, expiresAt, user } */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Who does this token belong to? Used by the frontend on page load. */
    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AuthUser principal) {
        return authService.me(principal.id());
    }

    /** Body: { "currentPassword": "...", "newPassword": "..." } -> a fresh token (the old ones stop working). */
    @PostMapping("/change-password")
    public LoginResponse changePassword(@AuthenticationPrincipal AuthUser principal,
                                        @Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(principal.id(), request);
    }
}
