package com.sathya.restaurant_management.security;

import com.sathya.restaurant_management.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Who may call what. The first matching rule wins, so keep specific rules above general ones.
 *
 *   POST /api/auth/login .................. anyone (that's how you sign in)
 *   /api/shop/** .......................... anyone (public storefront: menu + place/settle an order)
 *   /api/users/** ......................... ADMIN
 *   orders: create ........................ ADMIN, MANAGER, CASHIER, WAITER
 *   orders: pay / cancel / payment method . ADMIN, MANAGER, CASHIER
 *   customers: create ..................... ADMIN, MANAGER, CASHIER, WAITER
 *   any other GET under /api .............. any signed-in user
 *   any other write under /api ............ ADMIN, MANAGER  (menu, categories, inventory, costs)
 *   everything that isn't /api ............ public (the built frontend files)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   AppUserRepository users) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // token in a header, no cookies -> nothing for CSRF to abuse
                .cors(Customizer.withDefaults())       // uses the rules in CorsConfig
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/shop/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("ADMIN", "MANAGER", "CASHIER", "WAITER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/pay", "/api/orders/*/cancel")
                        .hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/*/payment-method")
                        .hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasAnyRole("ADMIN", "MANAGER", "CASHIER", "WAITER")
                        .requestMatchers("/api/auth/**").authenticated() // /me, /change-password
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "MANAGER")
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeError(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                                        "Please sign in to continue."))
                        .accessDeniedHandler((req, res, ex) ->
                                writeError(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                                        "You don't have permission to do that.")))
                .addFilterBefore(new JwtAuthFilter(jwtService, users), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Same JSON shape as ApiError. Written by hand so this file needs no JSON library. */
    private static void writeError(HttpServletResponse res, int status, String error, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status
                + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }
}
