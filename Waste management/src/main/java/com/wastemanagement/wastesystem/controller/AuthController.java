package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.ForgotPasswordRequest;
import com.wastemanagement.wastesystem.dto.request.LoginRequest;
import com.wastemanagement.wastesystem.dto.request.RegisterRequest;
import com.wastemanagement.wastesystem.dto.request.ResetPasswordRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.AuthResponse;
import com.wastemanagement.wastesystem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints — login, citizen self-registration, and
 * the forgot/reset password OTP flow.
 *
 * Every endpoint here is listed under SecurityConfig's permitAll() matcher
 * ("/api/auth/**"), since a client cannot possibly hold a JWT before
 * authenticating in the first place. All business logic (credential
 * verification, OTP generation/validation, password hashing) lives in
 * AuthService — this class only translates HTTP requests into service
 * calls and wraps the result in the standard ApiResponse envelope.
 *
 * @Valid on every @RequestBody triggers Bean Validation on the DTO's
 * annotated fields (e.g. RegisterRequest's @Email, @Size) before the
 * request ever reaches AuthService; any failure is caught globally by
 * GlobalExceptionHandler.handleValidationErrors(), so no manual validation
 * checks are needed here.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Authenticates a user (any of the three roles) by email/password and
     * returns a JWT alongside basic profile info for immediate role-based
     * routing on the frontend.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * POST /api/auth/register
     * Self-registers a new citizen account (Worker/Super Admin accounts
     * are created separately by an existing Super Admin — see
     * AuthService's class-level note). Returns a JWT immediately so the
     * new citizen is logged in without a separate login round-trip.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.registerCitizen(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
    }

    /**
     * POST /api/auth/forgot-password
     * Triggers OTP generation and email delivery if the submitted email
     * belongs to a registered account. Always returns the same generic
     * success message regardless of whether the email exists, to prevent
     * account enumeration (enforced inside AuthService, not here).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("If an account exists with this email, a password reset OTP has been sent.")
        );
    }

    /**
     * POST /api/auth/reset-password
     * Verifies the submitted OTP against the latest unused one issued for
     * the email, and if valid, updates the account's password.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully. Please log in."));
    }
}