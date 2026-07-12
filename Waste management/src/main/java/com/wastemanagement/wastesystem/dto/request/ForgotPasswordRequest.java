package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/auth/forgot-password.
 *
 * Submitted from ForgotPassword.js. Triggers AuthService (upcoming) to:
 * 1. Verify a User with this email exists
 * 2. Generate a numeric OTP via OtpGenerator (upcoming utility)
 * 3. Persist it as a PasswordResetOtp document
 * 4. Email it to the user via EmailService (upcoming)
 *
 * Deliberately returns a generic success response regardless of whether
 * the email exists in the system (enforced in AuthService, not here) to
 * avoid leaking which email addresses are registered — a standard
 * account-enumeration protection for password reset flows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;
}