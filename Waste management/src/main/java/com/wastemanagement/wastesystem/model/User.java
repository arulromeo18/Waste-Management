package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Core authentication document for every user of the system, regardless of role.
 *
 * A single "users" collection is used (rather than three separate collections)
 * so that login, password reset, and JWT issuance share one code path — see
 * CustomUserDetailsService, which looks up users purely by email.
 *
 * This document intentionally holds ONLY authentication/identity concerns:
 * email, password, role, and account status. Role-specific profile data
 * (e.g. a Worker's assigned zone/vehicle, a Citizen's address/reward points,
 * an Admin's designation) lives in separate profile documents that store
 * this document's id as a foreign-key-style reference (userId). This keeps
 * the security layer decoupled from evolving business/profile fields.
 *
 * Password is always stored BCrypt-hashed (see SecurityConfig.passwordEncoder());
 * plaintext passwords are never persisted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phone;

    /**
     * Determines the user's permissions and which profile document
     * (Citizen / Worker / Admin) holds their role-specific details.
     */
    private Role role;

    /**
     * Soft-disable flag. When false, the user cannot authenticate even with
     * correct credentials — used for admin-initiated account suspension
     * without deleting historical data (complaints, collection records, etc.).
     */
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}