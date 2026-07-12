package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/auth/register.
 *
 * Only citizens self-register through this endpoint (see Register.js) —
 * Super Admin and Worker accounts are provisioned by the Super Admin
 * through admin-only endpoints (UserService, upcoming), consistent with
 * the class-level note already documented on Worker.java.
 *
 * Combines fields destined for both User.java (auth/identity) and
 * Citizen.java (profile) in one request, since AuthService.registerCitizen
 * (upcoming) creates both documents together as a pair in a single
 * transaction-like operation. Role is never accepted from the client here
 * — AuthService hardcodes Role.CITIZEN for every registration through this
 * endpoint, closing off the same privilege-escalation risk noted on
 * LoginRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotBlank(message = "Zone is required")
    private String zoneId;

    @NotBlank(message = "Address is required")
    private String address;

    private String houseNumber;

    private String landmark;

    private String pincode;
}