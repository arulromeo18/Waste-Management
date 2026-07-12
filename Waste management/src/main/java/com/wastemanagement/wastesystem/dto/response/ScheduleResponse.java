package com.wastemanagement.wastesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Outward-facing representation of a CollectionSchedule, returned by
 * ScheduleController (upcoming) for ManageSchedules.js (Super Admin),
 * ScheduleView.js (citizen, read-only), and TodaySchedule.js /
 * AssignedRoute.js (worker, read-only, filtered to the current day).
 *
 * Enriches the raw document with zoneName/workerName/vehicleNumber,
 * resolved by ScheduleService at read time — the same "enrich ids into
 * names at the response layer" pattern already established in
 * ComplaintResponse and VehicleResponse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private String id;

    private String zoneId;
    private String zoneName;

    private String workerId;
    private String workerName;

    private String vehicleId;
    private String vehicleNumber;

    private List<String> collectionDays;

    private LocalTime startTime;

    private LocalTime endTime;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}