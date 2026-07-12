package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/auth/reset-password.
 *
 * Submitted from ResetPassword.js after the user receives their OTP via
 * email. AuthService (upcoming) will:
 * 1. Look up the latest unused PasswordResetOtp for this email
 *    (PasswordResetOtpRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc)
 * 2. Verify otpCode matches and expiresAt has not passed
 * 3. BCrypt-hash and update the User's password
 * 4. Mark the OTP document as used = true so it cannot be replayed
 *
 * Kept as a separate DTO from ForgotPasswordRequest (rather than one
 * combined "password reset" DTO with optional fields) since the two
 * requests represent genuinely different steps with different required
 * fields — ForgotPasswordRequest only ever needs an email, this one needs
 * the OTP and new password too.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}