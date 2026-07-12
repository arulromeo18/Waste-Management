package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.CollectionRecordRequest;
import com.wastemanagement.wastesystem.dto.response.CollectionRecordResponse;
import com.wastemanagement.wastesystem.exception.BadRequestException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.CollectionRecord;
import com.wastemanagement.wastesystem.model.Vehicle;
import com.wastemanagement.wastesystem.model.Worker;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.CollectionRecordRepository;
import com.wastemanagement.wastesystem.repository.VehicleRepository;
import com.wastemanagement.wastesystem.repository.WorkerRepository;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles a sanitation worker logging a completed pickup.
 *
 * The worker's zoneId is always resolved server-side from their own
 * Worker profile (via the authenticated userId passed in), never
 * accepted from the client — matching the same principle already
 * documented on CollectionRecordRequest. This prevents a worker from
 * logging a collection against a zone they aren't actually assigned to.
 *
 * When a record is logged as segregationCompliant = true, this service
 * delegates to RewardService to credit the citizens of that zone —
 * reward crediting logic itself lives entirely in RewardService so this
 * class stays focused on the collection-logging concern alone.
 */
@Service
@RequiredArgsConstructor
public class CollectionRecordService {

    private final CollectionRecordRepository collectionRecordRepository;
    private final WorkerRepository workerRepository;
    private final ZoneRepository zoneRepository;
    private final VehicleRepository vehicleRepository;
    private final RewardService rewardService;

    /**
     * Logs a new collection record on behalf of the given authenticated
     * worker's userId. Rejects a second submission for the same zone on
     * the same date to prevent accidental double-logging (e.g. a form
     * resubmit), per CollectionRecordRepository.existsByZoneIdAndCollectionDate.
     */
    public CollectionRecordResponse logCollection(String workerUserId, CollectionRecordRequest request) {
        Worker worker = workerRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for this account"));

        if (collectionRecordRepository.existsByZoneIdAndCollectionDate(worker.getZoneId(), request.getCollectionDate())) {
            throw new BadRequestException("A collection has already been logged for this zone on this date");
        }

        CollectionRecord record = CollectionRecord.builder()
                .scheduleId(request.getScheduleId())
                .zoneId(worker.getZoneId())
                .workerId(worker.getId())
                .vehicleId(request.getVehicleId())
                .collectionDate(request.getCollectionDate())
                .collectedAt(LocalDateTime.now())
                .segregationCompliant(request.getSegregationCompliant())
                .imageUrls(request.getImageUrls())
                .remarks(request.getRemarks())
                .build();

        record = collectionRecordRepository.save(record);

        // Increment the worker's lightweight activity counter (denormalized
        // on Worker.totalCollectionsLogged, per Worker.java's class-level note).
        worker.setTotalCollectionsLogged(worker.getTotalCollectionsLogged() + 1);
        workerRepository.save(worker);

        if (record.isSegregationCompliant()) {
            rewardService.awardPointsForCompliantCollection(record);
        }

        return toResponse(record);
    }

    public List<CollectionRecordResponse> getRecordsForZone(String zoneId) {
        return collectionRecordRepository.findByZoneId(zoneId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lists a worker's own collection history — resolves the Worker
     * profile from the authenticated userId, mirroring logCollection's
     * approach to identity resolution.
     */
    public List<CollectionRecordResponse> getRecordsForWorker(String workerUserId) {
        Worker worker = workerRepository.findByUserId(workerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for this account"));

        return collectionRecordRepository.findByWorkerId(worker.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CollectionRecordResponse> getRecordsForZoneInRange(String zoneId, LocalDate start, LocalDate end) {
        return collectionRecordRepository.findByZoneIdAndCollectionDateBetween(zoneId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lists non-compliant records within a date range system-wide — feeds
     * PenaltyService's review queue (upcoming) so the Super Admin can
     * decide whether to issue penalties against affected citizens.
     */
    public List<CollectionRecordResponse> getNonCompliantRecords(LocalDate start, LocalDate end) {
        return collectionRecordRepository.findBySegregationCompliantFalseAndCollectionDateBetween(start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    private CollectionRecordResponse toResponse(CollectionRecord record) {
        String zoneName = zoneRepository.findById(record.getZoneId())
                .map(Zone::getZoneName)
                .orElse("Unknown Zone");

        String workerName = workerRepository.findById(record.getWorkerId())
                .map(Worker::getFullName)
                .orElse("Unknown Worker");

        String vehicleNumber = record.getVehicleId() != null
                ? vehicleRepository.findById(record.getVehicleId()).map(Vehicle::getVehicleNumber).orElse(null)
                : null;

        return CollectionRecordResponse.builder()
                .id(record.getId())
                .scheduleId(record.getScheduleId())
                .zoneId(record.getZoneId())
                .zoneName(zoneName)
                .workerId(record.getWorkerId())
                .workerName(workerName)
                .vehicleId(record.getVehicleId())
                .vehicleNumber(vehicleNumber)
                .collectionDate(record.getCollectionDate())
                .collectedAt(record.getCollectedAt())
                .segregationCompliant(record.isSegregationCompliant())
                .imageUrls(record.getImageUrls())
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}