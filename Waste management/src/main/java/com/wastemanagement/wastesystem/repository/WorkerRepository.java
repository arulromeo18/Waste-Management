package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "workers" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed to resolve a worker's profile
 * from their authenticated userId, list workers per zone (for assignment
 * and Super Admin's "Manage Workers" screen), and look up the worker
 * currently holding a given vehicle.
 */
@Repository
public interface WorkerRepository extends MongoRepository<Worker, String> {

    /**
     * Resolves a Worker's profile from the authenticated User's id.
     * Used constantly in controllers via @AuthenticationPrincipal
     * SecurityUserPrincipal -> principal.getUserId() -> this lookup,
     * to load the full worker profile (zone, vehicle, shift) for the
     * currently logged-in worker.
     */
    Optional<Worker> findByUserId(String userId);

    /**
     * Used during worker onboarding (admin-created, unlike Citizen
     * self-registration) to guard against creating a duplicate Worker
     * profile for a User that already has one.
     */
    boolean existsByUserId(String userId);

    /**
     * Lists all workers within a given zone — used by Super Admin's
     * "Manage Workers" screen (zone filter) and when determining which
     * worker(s) are responsible for a zone's collection schedule.
     */
    List<Worker> findByZoneId(String zoneId);

    /**
     * Lists only active workers within a zone — used when assigning a
     * collection schedule or route, so deactivated/on-leave workers are
     * excluded from the assignment pool without needing to filter results
     * in the service layer.
     */
    List<Worker> findByZoneIdAndActiveTrue(String zoneId);

    /**
     * Resolves the worker currently assigned a given vehicle — used when
     * reassigning a vehicle (to confirm/clear the previous holder) and by
     * the Vehicle management screen to show "assigned to" info.
     */
    Optional<Worker> findByVehicleId(String vehicleId);

    /**
     * Lists all active workers system-wide — used for Super Admin overview
     * counts and any cross-zone workforce reporting.
     */
    List<Worker> findByActiveTrue();
}