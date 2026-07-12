package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for POST /api/worker/collection-records.
 *
 * Submitted from UploadWasteImages.js after a worker completes a pickup.
 * workerId and zoneId are deliberately NOT part of this DTO — matching the
 * same principle already applied in ComplaintRequest: CollectionRecordController
 * (upcoming) resolves the currently authenticated worker from the
 * JWT-derived SecurityUserPrincipal, and CollectionRecordService looks up
 * that worker's assigned zoneId to denormalize onto the new record, rather
 * than trusting a client-supplied zone that might not match the worker's
 * actual assignment.
 *
 * scheduleId and vehicleId remain optional here (matching
 * CollectionRecord.java's nullable fields) to support ad-hoc/unscheduled
 * pickups and manual/on-foot collections without a vehicle, as documented
 * on the model itself.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRecordRequest {

    private String scheduleId;

    private String vehicleId;

    @NotNull(message = "Collection date is required")
    private LocalDate collectionDate;

    @NotNull(message = "Segregation compliance status is required")
    private Boolean segregationCompliant;

    private List<String> imageUrls;

    private String remarks;
}