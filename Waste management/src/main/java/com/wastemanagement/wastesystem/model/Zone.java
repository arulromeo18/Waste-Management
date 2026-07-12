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
import java.util.List;

/**
 * Represents a geographic zone/ward managed by the Urban Local Body.
 *
 * Zones are the central organizing unit of the system: sanitation workers are
 * assigned to a zone, vehicles are allocated per zone, collection schedules
 * are defined per zone, and analytics/reports (collection efficiency,
 * segregation compliance, complaint volume) are broken down zone-wise so the
 * Super Admin can identify underperforming areas.
 *
 * A zone is created and managed exclusively by the Super Admin — workers and
 * citizens only ever reference an existing zone (by id), never create one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "zones")
public class Zone {

    @Id
    private String id;

    @NotBlank(message = "Zone name is required")
    @Indexed(unique = true)
    private String zoneName;

    @NotBlank(message = "Zone code is required")
    @Indexed(unique = true)
    private String zoneCode;

    @NotBlank(message = "Ward number is required")
    private String wardNumber;

    private String description;

    /**
     * Approximate population served by this zone — used in per-capita
     * waste generation analytics and to help the Super Admin gauge whether
     * a zone is adequately staffed/resourced.
     */
    private Long estimatedPopulation;

    /**
     * List of area/locality names covered under this zone (e.g. street names,
     * colonies) so citizens can identify which zone their household falls
     * under during registration.
     */
    private List<String> coveredAreas;

    /**
     * Soft-disable flag. A zone is deactivated rather than deleted if it's
     * merged/restructured, preserving historical collection records and
     * reports tied to its id.
     */
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}