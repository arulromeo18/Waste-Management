package com.wastemanagement.wastesystem.security;

import com.wastemanagement.wastesystem.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts our domain User model to Spring Security's UserDetails contract.
 *
 * This wrapper is what actually flows through Spring Security's internals
 * (SecurityContext, Authentication object, etc.) rather than exposing our
 * raw MongoDB User document directly to the framework.
 *
 * Role mapping: Spring Security's hasRole("X") checks expect an authority
 * literally named "ROLE_X". Our User.role field stores the plain role name
 * (e.g. "SUPER_ADMIN"), so we prefix it with "ROLE_" here when building the
 * GrantedAuthority - this keeps the plain enum clean everywhere else in the
 * codebase (DTOs, JWT claims, database) while still satisfying Spring's
 * convention at the security boundary.
 *
 * isEnabled() reflects User.active — this is what actually enforces the
 * Super Admin's account suspension feature (UserService.setUserActiveStatus)
 * at the authentication layer. When false, Spring Security's
 * DaoAuthenticationProvider rejects the login attempt with a
 * DisabledException before AuthService's own logic even runs, and any
 * JWT-based request from an already-issued token for that user will also
 * be rejected by JwtAuthenticationFilter's downstream authorization checks,
 * since a UserDetails with isEnabled() = false is treated by Spring
 * Security as unauthenticatable regardless of how it was obtained.
 *
 * The other three account-status flags (isAccountNonExpired,
 * isAccountNonLocked, isCredentialsNonExpired) remain hardcoded true,
 * since account expiry and credential expiry are not part of the current
 * feature set — only active/inactive suspension is. They stay as explicit
 * overrides (not removed) so it's trivial to wire in real logic for either
 * later without changing this class's shape.
 */
@Getter
public class SecurityUserPrincipal implements UserDetails {

    private final User user;

    public SecurityUserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * Exposes the underlying user's unique ID for use in controllers/
     * services without needing to re-query the database (e.g. "who is the
     * currently logged-in citizen submitting this complaint").
     */
    public String getUserId() {
        return user.getId();
    }
}