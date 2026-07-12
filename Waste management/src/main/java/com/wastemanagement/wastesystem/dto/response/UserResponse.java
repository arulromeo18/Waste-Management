package com.wastemanagement.wastesystem.dto.response;

import com.wastemanagement.wastesystem.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outward-facing representation of a User, returned wherever user data is
 * exposed to the frontend — Super Admin's ManageCitizens.js / ManageWorkers.js
 * listing screens, and a citizen/worker's own Profile.js view.
 *
 * Deliberately excludes User.password (the BCrypt hash must never leave
 * the server) and includes a few role-specific summary fields (zoneId,
 * rewardPoints, employeeId) as optional/nullable, populated only when
 * relevant to the user's role — this avoids needing three near-identical
 * response DTOs (CitizenResponse, WorkerResponse, AdminResponse) for what
 * is fundamentally the same "user row in an admin table" shape, while
 * still letting UserService (upcoming) choose which of these optional
 * fields to populate depending on whether it's building the response for
 * a Citizen, Worker, or plain Admin user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;

    private String fullName;

    private String email;

    private String phone;

    private Role role;

    private boolean active;

    // --- Populated only when role == CITIZEN ---
    private String zoneId;
    private String zoneName;
    private Integer rewardPoints;
    private Integer totalComplaintsFiled;

    // --- Populated only when role == WORKER ---
    private String vehicleId;
    private String employeeId;
    private String shiftTiming;
    private Integer totalCollectionsLogged;

    private LocalDateTime createdAt;
}