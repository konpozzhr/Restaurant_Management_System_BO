package com.sathya.restaurant_management.config;

import com.sathya.restaurant_management.model.AppUser;
import com.sathya.restaurant_management.model.Role;
import com.sathya.restaurant_management.repository.AppUserRepository;
import com.sathya.restaurant_management.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Creates the first admin account when the users table is empty - otherwise nobody could ever sign in.
 * Runs regardless of app.seed.enabled. If ADMIN_PASSWORD isn't set, a random password is generated
 * and printed ONCE in the server log; there is no password in the source code.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    // No look-alike characters (0/O, 1/l/I) so it can be read off a log line without mistakes.
    private static final String ALPHABET = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username:admin}")
    private String username;

    @Value("${app.admin.password:}")
    private String configuredPassword;

    @Value("${app.admin.full-name:Administrator}")
    private String fullName;

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        boolean generated = configuredPassword == null || configuredPassword.isBlank();
        if (!generated && configuredPassword.length() < 8) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 8 characters.");
        }
        String password = generated ? randomPassword(16) : configuredPassword;

        AppUser admin = new AppUser();
        admin.setId(Ids.next("u"));
        admin.setUsername(username.trim().toLowerCase());
        admin.setFullName(fullName);
        admin.setPasswordHash(encoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setCreatedAt(Instant.now());
        users.save(admin);

        if (generated) {
            log.warn("""

                    ============================================================
                     First start - created the admin account:
                       username: {}
                       password: {}
                     Sign in and change the password now. It is not shown again.
                    ============================================================""", admin.getUsername(), password);
        } else {
            log.info("Created admin account '{}' with the password from ADMIN_PASSWORD", admin.getUsername());
        }
    }

    private static String randomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
