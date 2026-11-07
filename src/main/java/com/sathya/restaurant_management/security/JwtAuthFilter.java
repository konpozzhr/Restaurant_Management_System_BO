package com.sathya.restaurant_management.security;

import com.sathya.restaurant_management.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the Bearer token, and if it is valid AND the account is still enabled AND the password
 * hasn't been changed since, marks the request as signed-in. Otherwise it does nothing and the
 * authorization rules in SecurityConfig answer with 401.
 *
 * Deliberately NOT a @Component: Spring Boot would then also register it as a plain servlet filter
 * and it would run twice per request. SecurityConfig creates it.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository users;

    public JwtAuthFilter(JwtService jwtService, AppUserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            jwtService.parse(header.substring(7).trim()).ifPresent(claims ->
                    users.findById(claims.getSubject())
                            .filter(u -> u.isEnabled())
                            .filter(u -> Integer.valueOf(u.getTokenVersion()).equals(claims.get("tv", Integer.class)))
                            .ifPresent(u -> {
                                AuthUser principal = new AuthUser(u.getId(), u.getUsername(), u.getFullName(), u.getRole());
                                var auth = new UsernamePasswordAuthenticationToken(
                                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }));
        }
        chain.doFilter(request, response);
    }
}
