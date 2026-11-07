package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.CreateUserRequest;
import com.sathya.restaurant_management.dto.UpdateUserRequest;
import com.sathya.restaurant_management.dto.UserDto;
import com.sathya.restaurant_management.exception.ConflictException;
import com.sathya.restaurant_management.exception.ResourceNotFoundException;
import com.sathya.restaurant_management.model.AppUser;
import com.sathya.restaurant_management.model.Role;
import com.sathya.restaurant_management.repository.AppUserRepository;
import com.sathya.restaurant_management.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Staff accounts - only reachable by ADMIN (see SecurityConfig). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public List<UserDto> findAll() {
        return users.findAllByOrderByCreatedAtAscIdAsc().stream().map(UserService::toDto).toList();
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase();
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("The username \"" + username + "\" is already taken.");
        }
        AppUser user = new AppUser();
        user.setId(Ids.next("u"));
        user.setUsername(username);
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(encoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        return toDto(users.save(user));
    }

    /**
     * Edit a person's name / role / active flag, or reset their password.
     * Safety rails: you can't lock yourself out, and the last active admin can't be removed.
     */
    @Transactional
    public UserDto update(String id, UpdateUserRequest request, String actingUserId) {
        AppUser user = users.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("User", id));

        Role newRole = request.role() != null ? request.role() : user.getRole();
        boolean newEnabled = request.enabled() != null ? request.enabled() : user.isEnabled();

        boolean losesAdmin = user.getRole() == Role.ADMIN && user.isEnabled() && (newRole != Role.ADMIN || !newEnabled);
        if (losesAdmin) {
            if (user.getId().equals(actingUserId)) {
                throw new ConflictException("You can't remove your own admin access or disable your own account.");
            }
            if (users.countByRoleAndEnabledTrue(Role.ADMIN) <= 1) {
                throw new ConflictException("There must always be at least one active admin.");
            }
        }

        boolean mustSignOutEverywhere = newRole != user.getRole() || newEnabled != user.isEnabled();
        if (request.fullName() != null) {
            user.setFullName(request.fullName().trim());
        }
        user.setRole(newRole);
        user.setEnabled(newEnabled);
        if (request.password() != null) {
            user.setPasswordHash(encoder.encode(request.password()));
            mustSignOutEverywhere = true;
        }
        if (mustSignOutEverywhere) {
            user.setTokenVersion(user.getTokenVersion() + 1);
        }
        return toDto(users.save(user));
    }

    public static UserDto toDto(AppUser u) {
        return new UserDto(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name(), u.isEnabled());
    }
}
