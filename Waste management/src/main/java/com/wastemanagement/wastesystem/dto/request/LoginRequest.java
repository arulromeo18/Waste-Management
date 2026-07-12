package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/auth/login.
 *
 * Carries the credentials submitted from Login.js. Kept as a plain DTO
 * (rather than reusing User.java directly) so the API contract stays
 * stable and minimal even if the User model grows additional fields —
 * only email and password are ever needed to authenticate, regardless of
 * which of the three roles is logging in.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}