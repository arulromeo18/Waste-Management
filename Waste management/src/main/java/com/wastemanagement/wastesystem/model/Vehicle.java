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
 * Represents a waste collection vehicle allocated to a zone.
 *
 * Vehicles are owned by a Zone (zoneId) and, at any point in time, may be
 * operated by at most one active Worker (Worker.vehicleId points back here
 * rather than this document holding a workerId list — a vehicle's current
 * driver is a Worker-side concern that changes more often than the vehicle's
 * own record, consistent with the ownership direction already used for
 * Citizen -> Zone and Worker -> Zone/Vehicle).
 *
 * A Vehicle is created and managed exclusively by the Super Admin, the same
 * as Zone — workers only ever reference an existing vehicle (by id) once
 * assigned to one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    @NotBlank(message = "Vehicle number is required")
    @Indexed(unique = true)
    private String vehicleNumber;

    /**
     * References Zone.id — the zone this vehicle is allocated to. Drives
     * which zone's schedule/route this vehicle appears on and constrains
     * which workers can be assigned to it (only workers in the same zone).
     */
    @NotBlank(message = "Zone is required")
    private String zoneId;

    /**
     * Free-text vehicle type/category (e.g. "Compactor Truck", "Tricycle",
     * "Mini Truck"). Kept as a plain string rather than an enum since the
     * spec doesn't fix a closed set of vehicle types and municipalities may
     * use locally specific naming — per Rule 18, not over-modeling ahead
     * of a concrete requirement.
     */
    private String vehicleType;

    private String capacity;

    /**
     * Whether this vehicle is currently roadworthy/in service. A vehicle
     * under repair is marked inactive so it's excluded from schedule/route
     * assignment without deleting its record and history.
     */
    @Builder.Default
    private boolean active = true;

    private LocalDateTime lastMaintenanceDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}