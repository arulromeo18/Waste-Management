package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * Request payload for POST /api/admin/schedules and PUT /api/admin/schedules/{id}.
 *
 * Submitted from ManageSchedules.js. workerId and vehicleId are optional
 * here (matching CollectionSchedule.java's nullable fields), since the
 * Super Admin may define a zone's schedule before a worker/vehicle has
 * been assigned to that zone yet.
 *
 * collectionDays is validated as a list of raw strings at this DTO layer
 * (@NotEmpty only); ScheduleService (upcoming) is responsible for the
 * deeper validation that each entry parses via DayOfWeek.valueOf(...),
 * translating an invalid day name (e.g. a frontend typo) into a clean
 * validation error rather than a raw IllegalArgumentException — matching
 * the same "DTO does shape validation, service does semantic validation"
 * pattern already used for zoneId existence checks in VehicleRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    @NotBlank(message = "Zone is required")
    private String zoneId;

    private String workerId;

    private String vehicleId;

    @NotEmpty(message = "At least one collection day is required")
    private List<String> collectionDays;

    @NotBlank(message = "Start time is required")
    private String startTime;

    private String endTime;
}