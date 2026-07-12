package com.wastemanagement.wastesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outward-facing representation of a Vehicle, returned by
 * VehicleController (upcoming) for ManageVehicles.js.
 *
 * Enriches the raw Vehicle document with the owning zone's name
 * (zoneName), resolved by VehicleService at read time — the same
 * "enrich ids into names at the response layer" pattern already
 * established in ComplaintResponse, so ManageVehicles.js can render a
 * readable table (vehicle number, zone name) without an extra client-side
 * lookup per row.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private String id;

    private String vehicleNumber;

    private String zoneId;
    private String zoneName;

    private String vehicleType;

    private String capacity;

    private boolean active;

    private LocalDateTime lastMaintenanceDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}