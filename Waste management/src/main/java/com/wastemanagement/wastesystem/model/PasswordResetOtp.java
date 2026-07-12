package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Stores a short-lived, single-use OTP (One-Time Password) issued during
 * the "Forgot Password" flow, emailed to the user via EmailService
 * (upcoming) and verified before ResetPassword.js allows setting a new
 * password.
 *
 * A fresh document is created each time a user requests a password reset
 * (ForgotPassword.js) rather than updating a single per-user OTP field on
 * User.java — this keeps OTP history/expiry logic isolated from the core
 * authentication document and lets multiple outstanding requests be
 * tracked/invalidated independently if needed.
 *
 * Expiry is enforced at the service layer (AuthService, upcoming) by
 * comparing expiresAt against the current time — MongoDB TTL indexes could
 * auto-delete expired documents, but that's a deployment/infra concern
 * left for the Deployment phase rather than baked into the model itself
 * (Rule 18).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_otps")
public class PasswordResetOtp {

    @Id
    private String id;

    /**
     * Email address the OTP was issued for — matched against User.email
     * during verification. Indexed since every lookup during the reset
     * flow queries by this field.
     */
    @NotBlank(message = "Email is required")
    @Indexed
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    /**
     * Whether this OTP has already been successfully used to reset a
     * password. Prevents replaying the same OTP a second time even if it
     * hasn't technically expired yet.
     */
    @Builder.Default
    private boolean used = false;

    @NotNull(message = "Expiry is required")
    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;
}