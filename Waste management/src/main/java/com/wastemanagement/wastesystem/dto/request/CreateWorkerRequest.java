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
 * Request payload for POST /api/admin/workers.
 *
 * Submitted from ManageWorkers.js when the Super Admin onboards a new
 * sanitation worker. Unlike RegisterRequest (public citizen self-signup),
 * this is an admin-only action — the account is created on behalf of
 * someone else, and no JWT is issued in response (see UserService's
 * class-level note on why worker provisioning lives separately from
 * AuthService).
 *
 * Field set mirrors what UserService.createWorker(...) already accepts as
 * raw parameters (fullName, email, rawPassword, phone, zoneId, employeeId,
 * shiftTiming) — this DTO simply gives that operation a validated,
 * documented HTTP-facing shape rather than a controller method with seven
 * unchecked @RequestParams.
 *
 * vehicleId is deliberately absent: vehicle assignment happens through a
 * dedicated action later (consistent with AssignWorkerRequest's existing
 * pattern for zone/vehicle reassignment), not at initial account creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkerRequest {

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

    private String employeeId;

    private String shiftTiming;
}