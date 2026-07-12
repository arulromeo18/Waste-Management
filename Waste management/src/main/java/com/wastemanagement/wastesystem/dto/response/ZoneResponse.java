package com.wastemanagement.wastesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outward-facing representation of a Zone, returned by ZoneController
 * (upcoming) for:
 * - Super Admin's ManageZones.js listing/detail screens
 * - Register.js's zone picker dropdown (citizens select their zone at
 *   sign-up)
 * - Worker/citizen dashboard views that display their assigned zone's name
 *
 * Unlike UserResponse or ComplaintResponse, this is a near-direct mirror
 * of Zone.java with no fields stripped — a zone has no sensitive or
 * internal-only data, so no separate "safe subset" is needed. It exists
 * as its own DTO (rather than returning the Zone model directly from
 * controllers) purely to keep the same "controllers never return raw
 * documents" convention consistent across every entity in this system,
 * which also gives ZoneService room to add computed/enriched fields later
 * (e.g. an assignedWorkerCount) without changing the persisted model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {

    private String id;

    private String zoneName;

    private String zoneCode;

    private String wardNumber;

    private String description;

    private Long estimatedPopulation;

    private List<String> coveredAreas;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}