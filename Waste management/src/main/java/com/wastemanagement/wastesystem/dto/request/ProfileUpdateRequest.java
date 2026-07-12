package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for PUT /api/citizen/profile.
 *
 * Submitted from Profile.js. Deliberately narrow: only the fields a
 * citizen is actually allowed to self-edit are present here. Notably
 * absent, and intentionally so:
 * - email: changing it would break login (User.email is the unique
 *   authentication key) — an email change would need a dedicated,
 *   verification-gated flow, out of scope for the current spec.
 * - password: has its own dedicated ResetPasswordRequest flow.
 * - role, active: security/administrative fields never client-editable.
 * - zoneId: changing zones has downstream effects on schedule/routing
 *   assignment and is treated as an admin-mediated action, not a
 *   citizen self-service edit, consistent with how zone/vehicle
 *   reassignment for workers is a dedicated admin action
 *   (AssignWorkerRequest) rather than a general profile field.
 * - rewardPoints, totalComplaintsFiled: system-maintained counters,
 *   never directly settable by the citizen they belong to.
 *
 * UserService/CitizenService (upcoming) will apply these fields across
 * both the User and Citizen documents for this citizen's userId, since
 * fullName/phone live on User while the address fields live on Citizen.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    private String address;

    private String houseNumber;

    private String landmark;

    private String pincode;
}