package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Zone;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "zones" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed for zone uniqueness validation
 * (during Super Admin zone creation) and active-zone listing (used across
 * Worker assignment, Vehicle allocation, and Citizen registration dropdowns).
 */
@Repository
public interface ZoneRepository extends MongoRepository<Zone, String> {

    /**
     * Used during zone creation to reject duplicate zone codes before hitting
     * the unique index constraint at the database level.
     */
    boolean existsByZoneCode(String zoneCode);

    /**
     * Used during zone creation to reject duplicate zone names before hitting
     * the unique index constraint at the database level.
     */
    boolean existsByZoneName(String zoneName);

    /**
     * Looks up a zone by its unique code — used when resolving a zone from
     * a short identifier (e.g. in CSV imports or QR-code check-ins).
     */
    Optional<Zone> findByZoneCode(String zoneCode);

    /**
     * Returns only active zones — used to populate dropdowns during Citizen
     * registration and Worker/Vehicle assignment, excluding deactivated
     * (merged/restructured) zones that shouldn't accept new assignments.
     */
    List<Zone> findByActiveTrue();
}