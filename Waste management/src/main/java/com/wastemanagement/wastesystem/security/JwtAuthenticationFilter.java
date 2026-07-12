package com.wastemanagement.wastesystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every incoming HTTP request exactly once to authenticate it via JWT.
 *
 * Flow:
 * 1. Reads the "Authorization" header and checks for the "Bearer " prefix.
 * 2. If present, extracts the token and the username (email) embedded in it.
 * 3. If the user is not already authenticated in the current SecurityContext,
 *    loads the full UserDetails via CustomUserDetailsService.
 * 4. Validates the token against the loaded user (signature + expiry + username match).
 * 5. If valid, builds an Authentication object and sets it in the SecurityContext,
 *    allowing Spring Security's authorizeHttpRequests rules (hasRole, etc.) to work.
 *
 * If no token is present or it's invalid, the filter simply passes the request
 * along the chain unauthenticated — Spring Security will then reject it with 401/403
 * if the endpoint requires authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception ex) {
            // Malformed, expired, or tampered token - let the request continue
            // unauthenticated; downstream security rules will reject it appropriately.
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}