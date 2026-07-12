package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/admin/vehicles and PUT /api/admin/vehicles/{id}.
 *
 * Submitted from ManageVehicles.js. Mirrors ZoneRequest's approach: a
 * single DTO shape serves both creation and update since Vehicle's
 * editable fields don't differ between the two operations.
 *
 * zoneId is required — a vehicle must always belong to a zone (see
 * Vehicle.java's class-level note on ownership direction: Vehicle -> Zone,
 * Worker -> Vehicle). VehicleService (upcoming) is responsible for
 * validating that the referenced zoneId actually exists before persisting,
 * translating an invalid reference into a clean validation error.
 *
 * The "active" flag and lastMaintenanceDate are deliberately NOT part of
 * this DTO — marking a vehicle inactive (under repair) or logging a
 * maintenance date are distinct operational actions (dedicated endpoints
 * planned for VehicleController, upcoming) rather than fields silently
 * updatable through a general create/update request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotBlank(message = "Zone is required")
    private String zoneId;

    private String vehicleType;

    private String capacity;
}