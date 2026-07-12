package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the actual, historical occurrence of a waste pickup on a
 * specific date — as distinct from CollectionSchedule, which is the
 * recurring plan (which days/time a zone is serviced).
 *
 * A CollectionRecord is created by a sanitation Worker via
 * UploadWasteImages.js after completing a pickup: it captures which
 * schedule it fulfilled, proof-of-collection images, and a per-visit
 * segregation-compliance assessment that feeds both the citizen's reward
 * points (RewardService) and penalty flow (PenaltyService) for improperly
 * segregated waste.
 *
 * Recording is zone/schedule-level, not per-citizen-per-day — matching the
 * spec's collection model where a worker services a zone as a unit rather
 * than checking in at every household individually. Citizen-level
 * complaints about missed/poor collection are handled separately via
 * Complaint.java, which can reference a CollectionRecord.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "collection_records")
public class CollectionRecord {

    @Id
    private String id;

    /**
     * References CollectionSchedule.id — the recurring schedule this pickup
     * fulfilled. Nullable to allow logging an ad-hoc/unscheduled pickup
     * (e.g. special drive, complaint-triggered visit) without requiring a
     * matching schedule to exist.
     */
    private String scheduleId;

    /**
     * References Zone.id — denormalized from the schedule (rather than
     * requiring a join every time) since zone-wise collection analytics
     * and reports are a primary read path for this collection.
     */
    @NotBlank(message = "Zone is required")
    private String zoneId;

    /**
     * References Worker.id — the worker who performed and logged this
     * pickup. Required: every record must be attributable to a worker.
     */
    @NotBlank(message = "Worker is required")
    private String workerId;

    /**
     * References Vehicle.id — the vehicle used for this pickup. Nullable
     * for the rare case a worker logs a manual/on-foot collection without
     * a vehicle.
     */
    private String vehicleId;

    @NotNull(message = "Collection date is required")
    @Indexed
    private LocalDate collectionDate;

    private LocalDateTime collectedAt;

    /**
     * Worker's on-the-spot assessment of whether waste in the zone was
     * properly segregated (wet/dry) at the time of pickup. Drives reward
     * points (compliant) or a flag for potential penalty review
     * (non-compliant) — see RewardService/PenaltyService (upcoming).
     */
    @Builder.Default
    private boolean segregationCompliant = true;

    /**
     * File paths/URLs of proof-of-collection images uploaded by the worker,
     * served from static/uploads per FileStorageService (upcoming). Kept as
     * a simple string list rather than a richer media object, since no
     * additional per-image metadata is required by the spec (Rule 18).
     */
    private List<String> imageUrls;

    private String remarks;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}