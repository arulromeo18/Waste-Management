package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "vehicles" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed to look up a vehicle by its
 * registration number, list vehicles per zone (for Super Admin's "Manage
 * Vehicles" screen and worker-vehicle assignment), and filter by active
 * status for schedule/route assignment.
 */
@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    /**
     * Resolves a Vehicle by its registration number — used when the Super
     * Admin adds a vehicle, to guard against duplicate entries for the same
     * physical vehicle, and for quick lookup/search on the management screen.
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    /**
     * Guard against creating a duplicate Vehicle record for the same
     * registration number.
     */
    boolean existsByVehicleNumber(String vehicleNumber);

    /**
     * Lists all vehicles allocated to a given zone — used by Super Admin's
     * "Manage Vehicles" screen (zone filter) and when assigning a vehicle
     * to a worker (only vehicles within the worker's zone are eligible).
     */
    List<Vehicle> findByZoneId(String zoneId);

    /**
     * Lists only active (roadworthy) vehicles within a zone — used when
     * assigning a vehicle to a worker or building a collection schedule,
     * so vehicles under maintenance are excluded from the assignment pool
     * without filtering in the service layer.
     */
    List<Vehicle> findByZoneIdAndActiveTrue(String zoneId);
}