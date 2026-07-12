package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.CollectionSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "collection_schedules" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed to resolve a zone's active
 * schedule (for citizen ScheduleView and worker "Today's Schedule"), list
 * schedules by worker (for the worker dashboard), and list by vehicle (for
 * vehicle reassignment checks).
 */
@Repository
public interface CollectionScheduleRepository extends MongoRepository<CollectionSchedule, String> {

    /**
     * Resolves the currently active schedule for a zone. Used by citizens'
     * ScheduleView.js and to seed a worker's "Today's Schedule" /
     * "Assigned Route" view. Returns Optional since a newly created zone
     * may not have an active schedule defined yet.
     */
    Optional<CollectionSchedule> findByZoneIdAndActiveTrue(String zoneId);

    /**
     * Lists all schedules (active and inactive/superseded) for a zone —
     * used by Super Admin to view a zone's scheduling history when
     * reviewing or revising collection timings.
     */
    List<CollectionSchedule> findByZoneId(String zoneId);

    /**
     * Lists active schedules assigned to a given worker — used by the
     * worker dashboard, since a worker could in principle be responsible
     * for more than one zone's schedule (e.g. covering during a shortage).
     */
    List<CollectionSchedule> findByWorkerIdAndActiveTrue(String workerId);

    /**
     * Lists active schedules using a given vehicle — used when
     * deactivating or reassigning a vehicle, to warn the Super Admin
     * which schedules would be affected.
     */
    List<CollectionSchedule> findByVehicleIdAndActiveTrue(String vehicleId);
}