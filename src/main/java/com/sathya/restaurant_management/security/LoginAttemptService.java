package com.sathya.restaurant_management.security;

import com.sathya.restaurant_management.exception.TooManyAttemptsException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing: 5 wrong passwords for the same username within 15 minutes
 * locks sign-in for that username for 15 minutes. Kept in memory, so it resets on restart
 * and is per server instance - fine for one restaurant server; use Redis/DB if you ever scale out.
 */
@Component
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK = Duration.ofMinutes(15);
    private static final int MAX_TRACKED = 5_000;

    private record State(int failures, Instant windowStart, Instant lockedUntil) {
    }

    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();

    /** Throws if this username is currently locked. */
    public void checkAllowed(String username) {
        State s = states.get(username);
        Instant now = Instant.now();
        if (s != null && s.lockedUntil() != null && now.isBefore(s.lockedUntil())) {
            long minutes = Math.max(1, (Duration.between(now, s.lockedUntil()).getSeconds() + 59) / 60);
            throw new TooManyAttemptsException("Too many failed attempts. Try again in " + minutes
                    + (minutes == 1 ? " minute." : " minutes."));
        }
    }

    public void recordFailure(String username) {
        Instant now = Instant.now();
        if (states.size() > MAX_TRACKED) {
            states.entrySet().removeIf(e -> now.isAfter(e.getValue().windowStart().plus(WINDOW)));
        }
        states.compute(username, (k, s) -> {
            boolean stale = s == null
                    || now.isAfter(s.windowStart().plus(WINDOW))
                    || (s.lockedUntil() != null && now.isAfter(s.lockedUntil()));
            if (stale) {
                return new State(1, now, null);
            }
            int failures = s.failures() + 1;
            return new State(failures, s.windowStart(), failures >= MAX_FAILURES ? now.plus(LOCK) : null);
        });
    }

    public void recordSuccess(String username) {
        states.remove(username);
    }
}
