package com.wastemanagement.wastesystem.model;

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
 * Sanitation Worker profile data, separate from authentication concerns
 * (which live in User.java) — mirrors the Citizen/User split for the same
 * reasons: keep login/JWT/security stable while worker-specific fields
 * (zone assignment, vehicle assignment, shift, performance) evolve
 * independently.
 *
 * Each Worker document references its corresponding User document via
 * userId (one-to-one), and references the Zone it is assigned to collect
 * waste from via zoneId. Vehicle assignment is stored as vehicleId rather
 * than embedding the Vehicle document, since a vehicle can be reassigned
 * between workers over time and should remain a separate collection
 * (consistent with Rule 18 — no premature embedding of independently
 * evolving entities).
 *
 * A Worker is created by the Super Admin (not via public self-registration
 * like Citizen) — see the upcoming UserService/WorkerService admin-onboarding
 * flow — since sanitation staff accounts are provisioned by the municipality,
 * not signed up by the public.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "workers")
public class Worker {

    @Id
    private String id;

    /**
     * References User.id — the single source of truth for login credentials.
     * One-to-one relationship: every Worker has exactly one User, and
     * vice versa for role = WORKER.
     */
    @Indexed(unique = true)
    @NotBlank(message = "User reference is required")
    private String userId;

    /**
     * References Zone.id — the zone this worker is currently responsible
     * for collecting waste in. Drives "Today's Schedule" / "Assigned Route"
     * views on the worker dashboard and determines which citizens' waste
     * images/collection records this worker can log.
     */
    @NotBlank(message = "Zone is required")
    private String zoneId;

    /**
     * References Vehicle.id — the vehicle currently assigned to this worker,
     * if any. Nullable: a worker may be between vehicle assignments (e.g.
     * newly onboarded, or their vehicle is under maintenance).
     */
    private String vehicleId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;

    /**
     * Municipal employee/staff ID issued outside this system — kept as a
     * plain field (not unique-indexed) since not every deployment may
     * assign one at onboarding time; can be tightened later if required.
     */
    private String employeeId;

    /**
     * Free-text shift descriptor (e.g. "Morning", "06:00-14:00"). Kept
     * simple per Rule 18 rather than modeling a full Shift entity until
     * the spec calls for shift-swapping or multi-shift scheduling.
     */
    private String shiftTiming;

    /**
     * Running count of collection records logged by this worker — surfaced
     * on the Super Admin's "Manage Workers" screen as a lightweight
     * performance/activity indicator, without needing a live aggregation
     * query on every page load (same rationale as
     * Citizen.totalComplaintsFiled).
     */
    @Builder.Default
    private int totalCollectionsLogged = 0;

    /**
     * Whether this worker is currently active/on-duty. Allows Super Admin
     * to deactivate a worker (leave, resignation, suspension) without
     * deleting their history — collection records and past assignments
     * remain intact.
     */
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}