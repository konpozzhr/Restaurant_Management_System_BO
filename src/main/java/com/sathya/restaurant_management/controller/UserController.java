package com.sathya.restaurant_management.controller;

import com.sathya.restaurant_management.dto.CreateUserRequest;
import com.sathya.restaurant_management.dto.UpdateUserRequest;
import com.sathya.restaurant_management.dto.UserDto;
import com.sathya.restaurant_management.security.AuthUser;
import com.sathya.restaurant_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Staff accounts. ADMIN only. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.findAll();
    }

    /** Body: { "username": "dara", "fullName": "Dara Vong", "password": "...", "role": "CASHIER" } */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    /** Any of: fullName, role, enabled, password (= reset). Only the fields you send are changed. */
    @PatchMapping("/{id}")
    public UserDto update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request,
                          @AuthenticationPrincipal AuthUser principal) {
        return userService.update(id, request, principal.id());
    }
}
