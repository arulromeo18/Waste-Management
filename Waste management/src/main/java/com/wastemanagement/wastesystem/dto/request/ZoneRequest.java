package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for POST /api/admin/zones and PUT /api/admin/zones/{id}.
 *
 * Submitted from ManageZones.js. Used for both creation and update — the
 * same shape works for both operations since a zone's editable fields
 * don't differ between the two (unlike, say, RegisterRequest vs a profile
 * update, which intentionally differ). ZoneService (upcoming) is
 * responsible for enforcing zoneName/zoneCode uniqueness before persisting,
 * translating a duplicate-key violation into a clean validation error
 * rather than a raw MongoDB exception surfacing to the client.
 *
 * The "active" flag is deliberately NOT part of this DTO — deactivating a
 * zone is a distinct administrative action (see the dedicated toggle
 * endpoint planned for ZoneController, upcoming) rather than something
 * silently flippable through a general update request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneRequest {

    @NotBlank(message = "Zone name is required")
    private String zoneName;

    @NotBlank(message = "Zone code is required")
    private String zoneCode;

    @NotBlank(message = "Ward number is required")
    private String wardNumber;

    private String description;

    @NotNull(message = "Estimated population is required")
    @Positive(message = "Estimated population must be a positive number")
    private Long estimatedPopulation;

    private List<String> coveredAreas;
}