package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.AppUser;
import com.sathya.restaurant_management.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<AppUser> findAllByOrderByCreatedAtAscIdAsc();

    long countByRoleAndEnabledTrue(Role role);
}
