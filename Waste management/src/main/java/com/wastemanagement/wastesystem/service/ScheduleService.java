package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.ScheduleRequest;
import com.wastemanagement.wastesystem.dto.response.ScheduleResponse;
import com.wastemanagement.wastesystem.exception.BadRequestException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.CollectionSchedule;
import com.wastemanagement.wastesystem.model.Vehicle;
import com.wastemanagement.wastesystem.model.Worker;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.CollectionScheduleRepository;
import com.wastemanagement.wastesystem.repository.VehicleRepository;
import com.wastemanagement.wastesystem.repository.WorkerRepository;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Manages recurring collection schedules for zones.
 *
 * A zone is expected to have at most one active schedule at a time
 * (documented as a service-layer concern on CollectionSchedule.java,
 * not a document constraint). This service enforces that by
 * auto-deactivating any existing active schedule for the zone whenever
 * a new one is created, rather than requiring the Super Admin to
 * manually deactivate the old one first as a separate step.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CollectionScheduleRepository scheduleRepository;
    private final ZoneRepository zoneRepository;
    private final WorkerRepository workerRepository;
    private final VehicleRepository vehicleRepository;

    public ScheduleResponse createSchedule(ScheduleRequest request) {
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));

        validateCollectionDays(request.getCollectionDays());
        LocalTime startTime = parseTime(request.getStartTime(), "Start time");
        LocalTime endTime = request.getEndTime() != null ? parseTime(request.getEndTime(), "End time") : null;

        // Supersede any existing active schedule for this zone.
        scheduleRepository.findByZoneIdAndActiveTrue(zone.getId())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    scheduleRepository.save(existing);
                });

        CollectionSchedule schedule = CollectionSchedule.builder()
                .zoneId(zone.getId())
                .workerId(request.getWorkerId())
                .vehicleId(request.getVehicleId())
                .collectionDays(request.getCollectionDays())
                .startTime(startTime)
                .endTime(endTime)
                .active(true)
                .build();

        schedule = scheduleRepository.save(schedule);
        return toResponse(schedule);
    }

    /**
     * Updates the worker/vehicle assignment and timing on an existing
     * schedule, without going through the deactivate-and-recreate flow
     * used by createSchedule — this is for correcting/refining the
     * current active schedule, not superseding it with a new plan.
     */
    public ScheduleResponse updateSchedule(String scheduleId, ScheduleRequest request) {
        CollectionSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        validateCollectionDays(request.getCollectionDays());
        LocalTime startTime = parseTime(request.getStartTime(), "Start time");
        LocalTime endTime = request.getEndTime() != null ? parseTime(request.getEndTime(), "End time") : null;

        schedule.setWorkerId(request.getWorkerId());
        schedule.setVehicleId(request.getVehicleId());
        schedule.setCollectionDays(request.getCollectionDays());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        schedule = scheduleRepository.save(schedule);
        return toResponse(schedule);
    }

    /**
     * Resolves the active schedule for a zone — used by citizens'
     * ScheduleView.js. Returns null if none exists yet (a newly created
     * zone may have no schedule defined) rather than throwing, since "no
     * schedule yet" is a normal, displayable state on the frontend, not
     * an error condition.
     */
    public ScheduleResponse getActiveScheduleForZone(String zoneId) {
        return scheduleRepository.findByZoneIdAndActiveTrue(zoneId)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<ScheduleResponse> getScheduleHistoryForZone(String zoneId) {
        return scheduleRepository.findByZoneId(zoneId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Resolves a worker's active schedule(s) filtered to today's day of
     * week — the core query behind TodaySchedule.js and AssignedRoute.js.
     */
    public List<ScheduleResponse> getTodayScheduleForWorker(String workerId) {
        String today = DayOfWeek.from(java.time.LocalDate.now()).name();
        return scheduleRepository.findByWorkerIdAndActiveTrue(workerId).stream()
                .filter(schedule -> schedule.getCollectionDays().contains(today))
                .map(this::toResponse)
                .toList();
    }

    public List<ScheduleResponse> getSchedulesForWorker(String workerId) {
        return scheduleRepository.findByWorkerIdAndActiveTrue(workerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateCollectionDays(List<String> days) {
        for (String day : days) {
            try {
                DayOfWeek.valueOf(day.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid collection day: " + day
                        + ". Must be one of MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY.");
            }
        }
    }

    private LocalTime parseTime(String time, String fieldLabel) {
        try {
            return LocalTime.parse(time);
        } catch (Exception ex) {
            throw new BadRequestException(fieldLabel + " must be in HH:mm format (e.g. 08:00)");
        }
    }

    /**
     * Enriches a CollectionSchedule into a ScheduleResponse, resolving
     * zoneName/workerName/vehicleNumber via fresh lookups — nullable
     * worker/vehicle references (per CollectionSchedule.java's class-level
     * note) are handled gracefully rather than assuming they're always set.
     */
    private ScheduleResponse toResponse(CollectionSchedule schedule) {
        String zoneName = zoneRepository.findById(schedule.getZoneId())
                .map(Zone::getZoneName)
                .orElse("Unknown Zone");

        String workerName = schedule.getWorkerId() != null
                ? workerRepository.findById(schedule.getWorkerId()).map(Worker::getFullName).orElse(null)
                : null;

        String vehicleNumber = schedule.getVehicleId() != null
                ? vehicleRepository.findById(schedule.getVehicleId()).map(Vehicle::getVehicleNumber).orElse(null)
                : null;

        return ScheduleResponse.builder()
                .id(schedule.getId())
                .zoneId(schedule.getZoneId())
                .zoneName(zoneName)
                .workerId(schedule.getWorkerId())
                .workerName(workerName)
                .vehicleId(schedule.getVehicleId())
                .vehicleNumber(vehicleNumber)
                .collectionDays(schedule.getCollectionDays())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .active(schedule.isActive())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}