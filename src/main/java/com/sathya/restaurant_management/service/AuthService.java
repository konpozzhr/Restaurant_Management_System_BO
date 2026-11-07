package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.ChangePasswordRequest;
import com.sathya.restaurant_management.dto.LoginRequest;
import com.sathya.restaurant_management.dto.LoginResponse;
import com.sathya.restaurant_management.dto.UserDto;
import com.sathya.restaurant_management.exception.ResourceNotFoundException;
import com.sathya.restaurant_management.exception.UnauthorizedException;
import com.sathya.restaurant_management.model.AppUser;
import com.sathya.restaurant_management.repository.AppUserRepository;
import com.sathya.restaurant_management.security.JwtService;
import com.sathya.restaurant_management.security.LoginAttemptService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;

    /** Compared against when the username doesn't exist, so a wrong username takes as long as a wrong password. */
    private final String dummyHash;

    public AuthService(AppUserRepository users, PasswordEncoder encoder, JwtService jwtService, LoginAttemptService attempts) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.attempts = attempts;
        this.dummyHash = encoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase();
        attempts.checkAllowed(username);

        Optional<AppUser> found = users.findByUsernameIgnoreCase(username);
        boolean passwordOk = encoder.matches(request.password(), found.map(AppUser::getPasswordHash).orElse(dummyHash));
        if (found.isEmpty() || !passwordOk) {
            attempts.recordFailure(username);
            throw new UnauthorizedException("Incorrect username or password.");
        }
        AppUser user = found.get();
        if (!user.isEnabled()) {
            // Only reached with the right password, so this doesn't tell strangers which usernames exist.
            throw new UnauthorizedException("This account is disabled. Ask your manager.");
        }

        attempts.recordSuccess(username);
        user.setLastLoginAt(Instant.now());
        users.save(user);
        return toResponse(user);
    }

    public UserDto me(String userId) {
        return UserService.toDto(find(userId));
    }

    /** Also invalidates every older token (token version bump) and returns a fresh one for this device. */
    @Transactional
    public LoginResponse changePassword(String userId, ChangePasswordRequest request) {
        AppUser user = find(userId);
        if (!encoder.matches(request.currentPassword(), user.getPasswordHash())) {
            // 400, not 401: the person IS signed in, they just typed the wrong current password.
            throw new IllegalArgumentException("Your current password is incorrect.");
        }
        if (encoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Choose a password you haven't used here before.");
        }
        user.setPasswordHash(encoder.encode(request.newPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return toResponse(users.save(user));
    }

    private LoginResponse toResponse(AppUser user) {
        JwtService.IssuedToken issued = jwtService.issue(user);
        return new LoginResponse(issued.token(), issued.expiresAt(), UserService.toDto(user));
    }

    private AppUser find(String id) {
        return users.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("User", id));
    }
}
