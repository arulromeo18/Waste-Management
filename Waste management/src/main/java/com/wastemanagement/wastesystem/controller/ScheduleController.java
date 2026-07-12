package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.ScheduleRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.ScheduleResponse;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Worker;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.WorkerRepository;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Collection schedule endpoints across all three roles:
 * - Super Admin: full CRUD under "/api/admin/schedules" (SecurityConfig
 *   restricts this prefix to SUPER_ADMIN).
 * - Citizen: read-only view of their own zone's active schedule under
 *   "/api/citizen/schedule" (restricted to CITIZEN).
 * - Worker: read-only view of their own assigned schedule(s), optionally
 *   filtered to today, under "/api/worker/schedule" (restricted to WORKER).
 *
 * ScheduleService's worker-facing methods take a workerId (Worker.id), not
 * a userId — unlike ComplaintService, which accepts citizenUserId directly.
 * This controller bridges that gap by resolving Worker.id from the
 * authenticated principal's userId via WorkerRepository before delegating,
 * and similarly resolves the citizen's own zoneId via CitizenRepository.
 */
@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final CitizenRepository citizenRepository;
    private final WorkerRepository workerRepository;

    /**
     * POST /api/admin/schedules
     * Creates a new schedule for a zone, automatically superseding any
     * existing active schedule for that zone (see
     * ScheduleService.createSchedule()).
     */
    @PostMapping("/api/admin/schedules")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse schedule = scheduleService.createSchedule(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Schedule created successfully", schedule));
    }

    /**
     * PUT /api/admin/schedules/{scheduleId}
     * Updates an existing schedule's worker/vehicle assignment and timing,
     * without superseding it as a new schedule (see
     * ScheduleService.updateSchedule()).
     */
    @PutMapping("/api/admin/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse schedule = scheduleService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success("Schedule updated successfully", schedule));
    }

    /**
     * GET /api/admin/schedules/zone/{zoneId}/history
     * Lists every schedule (active and superseded) ever defined for a
     * zone — ManageSchedules.js's history view.
     */
    @GetMapping("/api/admin/schedules/zone/{zoneId}/history")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getScheduleHistoryForZone(
            @PathVariable String zoneId) {
        List<ScheduleResponse> history = scheduleService.getScheduleHistoryForZone(zoneId);
        return ResponseEntity.ok(ApiResponse.success("Schedule history retrieved successfully", history));
    }

    /**
     * GET /api/admin/schedules/zone/{zoneId}/active
     * Retrieves a zone's current active schedule, if one exists. Returns
     * a null data payload (still a 200 success) rather than a 404 when
     * no schedule is defined yet, per ScheduleService's documented
     * "no schedule yet is a normal state" behavior.
     */
    @GetMapping("/api/admin/schedules/zone/{zoneId}/active")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getActiveScheduleForZoneAdmin(
            @PathVariable String zoneId) {
        ScheduleResponse schedule = scheduleService.getActiveScheduleForZone(zoneId);
        return ResponseEntity.ok(ApiResponse.success("Active schedule retrieved successfully", schedule));
    }

    /**
     * GET /api/citizen/schedule
     * Retrieves the authenticated citizen's own zone's active collection
     * schedule — ScheduleView.js.
     */
    @GetMapping("/api/citizen/schedule")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getMyZoneSchedule(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Citizen citizen = citizenRepository.findByUserId(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));

        ScheduleResponse schedule = scheduleService.getActiveScheduleForZone(citizen.getZoneId());
        return ResponseEntity.ok(ApiResponse.success("Schedule retrieved successfully", schedule));
    }

    /**
     * GET /api/worker/schedule?todayOnly=true|false
     * Retrieves the authenticated worker's own assigned schedule(s).
     * todayOnly=true (default) filters to schedules running today —
     * TodaySchedule.js / AssignedRoute.js; todayOnly=false returns every
     * active schedule assigned to this worker regardless of day.
     */
    @GetMapping("/api/worker/schedule")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getMySchedule(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @RequestParam(required = false, defaultValue = "true") boolean todayOnly) {
        Worker worker = workerRepository.findByUserId(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for this account"));

        List<ScheduleResponse> schedules = todayOnly
                ? scheduleService.getTodayScheduleForWorker(worker.getId())
                : scheduleService.getSchedulesForWorker(worker.getId());

        return ResponseEntity.ok(ApiResponse.success("Schedule retrieved successfully", schedules));
    }
}