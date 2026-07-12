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
 * Citizen-specific profile data, separate from authentication concerns
 * (which live in User.java).
 *
 * Each Citizen document references its corresponding User document via
 * userId — a one-to-one link. This separation keeps the authentication
 * layer (User) stable and lean while allowing citizen-specific fields
 * (address, zone, reward points) to evolve independently without touching
 * login/JWT/security code.
 *
 * A Citizen is created immediately after User registration completes
 * (see AuthService.registerCitizen, upcoming) — the two documents are
 * always created together as a pair.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "citizens")
public class Citizen {

    @Id
    private String id;

    /**
     * References User.id — the single source of truth for login credentials.
     * One-to-one relationship: every Citizen has exactly one User, and
     * vice versa for role = CITIZEN.
     */
    @Indexed(unique = true)
    @NotBlank(message = "User reference is required")
    private String userId;

    /**
     * References Zone.id — determines which collection schedule, assigned
     * worker, and zone-wise announcements apply to this citizen.
     */
    @NotBlank(message = "Zone is required")
    private String zoneId;

    @NotBlank(message = "Address is required")
    private String address;

    private String houseNumber;

    private String landmark;

    private String pincode;

    /**
     * Accumulated reward points earned for consistent proper waste
     * segregation, as verified by sanitation workers during collection.
     * Redeemable per the Rewards System (upcoming Reward model/service).
     */
    @Builder.Default
    private int rewardPoints = 0;

    /**
     * Running count of complaints filed by this citizen — surfaced in the
     * Super Admin's citizen management view to flag chronically dissatisfied
     * households, without needing a live aggregation query on every page load.
     */
    @Builder.Default
    private int totalComplaintsFiled = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}