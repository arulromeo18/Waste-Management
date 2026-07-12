package com.wastemanagement.wastesystem.config;

import com.wastemanagement.wastesystem.security.CustomUserDetailsService;
import com.wastemanagement.wastesystem.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Central Spring Security configuration for the Waste Segregation Monitoring System.
 *
 * Responsibilities:
 * - Disables CSRF protection since this is a stateless, token-based REST API.
 * - Enforces STATELESS session management (no server-side HTTP sessions; JWT carries auth state).
 * - Defines which endpoints are public (auth, password reset) vs role-protected
 *   (ADMIN, WORKER, CITIZEN specific routes).
 * - Registers the custom JWT filter before Spring's default username/password filter.
 * - Exposes the BCrypt password encoder and the AuthenticationManager bean
 *   used by AuthService during login.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/uploads/**"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                        // Worker-only endpoints
                        .requestMatchers("/api/worker/**").hasRole("WORKER")

                        // Citizen-only endpoints
                        .requestMatchers("/api/citizen/**").hasRole("CITIZEN")

                        // Shared endpoints accessible by multiple authenticated roles
                        .requestMatchers("/api/notifications/**")
                        .hasAnyRole("SUPER_ADMIN", "WORKER", "CITIZEN")
                        .requestMatchers("/api/announcements/**")
                        .hasAnyRole("SUPER_ADMIN", "WORKER", "CITIZEN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}