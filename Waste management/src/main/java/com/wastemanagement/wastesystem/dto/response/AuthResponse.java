package com.wastemanagement.wastesystem.dto.response;

import com.wastemanagement.wastesystem.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for POST /api/auth/login and POST /api/auth/register.
 *
 * Returned to Login.js / Register.js so the frontend can:
 * 1. Store the JWT (token) and attach it as a Bearer header on all
 *    subsequent requests (see AuthContext.js, upcoming)
 * 2. Immediately route the user to the correct dashboard based on role
 *    (AdminDashboard.js / WorkerDashboard.js / CitizenDashboard.js)
 *    without an extra round-trip to fetch "who am I"
 *
 * Deliberately excludes the password hash and any other sensitive User
 * fields — only what the frontend needs to function post-login is
 * exposed, mirroring the same minimal-surface principle already applied
 * to every request DTO in this system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String userId;

    private String fullName;

    private String email;

    private Role role;

    /**
     * Token type constant, always "Bearer" — included so the frontend's
     * Axios interceptor (axiosInstance.js, upcoming) can construct the
     * Authorization header as `${tokenType} ${token}` without hardcoding
     * the scheme name on the client side.
     */
    @Builder.Default
    private String tokenType = "Bearer";
}