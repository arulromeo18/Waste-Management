package com.wastemanagement.wastesystem.security;

import com.wastemanagement.wastesystem.model.User;
import com.wastemanagement.wastesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user-specific data for Spring Security's authentication process.
 *
 * This service is invoked in two places:
 * 1. During login (via AuthenticationManager -> DaoAuthenticationProvider),
 *    to verify the submitted email/password against the stored user.
 * 2. During JWT validation (via JwtAuthenticationFilter), to load the full
 *    user details for the email extracted from a valid token.
 *
 * Email is used as the unique "username" throughout the system since citizens,
 * workers, and admins all register/login with an email address rather than
 * a separate username field.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));

        return new SecurityUserPrincipal(user);
    }
}