package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Represents a recurring waste collection schedule for a zone.
 *
 * A schedule defines which days of the week and what time window waste is
 * collected in a given zone, and which worker/vehicle pair is responsible
 * for it. This is distinct from CollectionRecord (upcoming), which logs the
 * actual occurrence of a pickup on a specific date — CollectionSchedule is
 * the recurring plan, CollectionRecord is the historical log entry.
 *
 * A schedule is created and managed exclusively by the Super Admin. Citizens
 * view their zone's schedule read-only (ScheduleView.js); workers view it
 * read-only as "Today's Schedule" / "Assigned Route" filtered to their own
 * zone and the current day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "collection_schedules")
public class CollectionSchedule {

    @Id
    private String id;

    /**
     * References Zone.id — the zone this schedule applies to. One zone may
     * have multiple schedules over time (e.g. revised timings), but only
     * one is expected to be active at once; enforcing that is a service-
     * layer concern, not a document constraint (per Rule 18).
     */
    @NotBlank(message = "Zone is required")
    private String zoneId;

    /**
     * References Worker.id — the worker responsible for executing this
     * schedule. Nullable at creation time: the Super Admin may define a
     * zone's schedule before a worker has been assigned to that zone yet.
     */
    private String workerId;

    /**
     * References Vehicle.id — the vehicle used for this schedule's pickups.
     * Nullable for the same reason as workerId.
     */
    private String vehicleId;

    /**
     * Days of the week this schedule runs, e.g. ["MONDAY", "THURSDAY"].
     * Stored as strings rather than java.time.DayOfWeek directly to avoid
     * MongoDB enum-serialization edge cases and keep the frontend contract
     * (JSON) simple — validated against DayOfWeek.valueOf(...) at the
     * service layer.
     */
    @NotEmpty(message = "At least one collection day is required")
    private List<String> collectionDays;

    /**
     * Expected pickup start time on each scheduled day. A single time
     * window per schedule keeps the model simple (Rule 18) — per-day
     * varying times would need a richer structure the spec doesn't
     * currently call for.
     */
    @NotBlank(message = "Start time is required")
    private LocalTime startTime;

    private LocalTime endTime;

    /**
     * Soft-disable flag. A schedule is deactivated (superseded by a new one)
     * rather than deleted, preserving historical context for why a given
     * CollectionRecord existed under the schedule active at the time.
     */
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}