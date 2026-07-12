package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the "announcements" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed to build a citizen/worker's
 * combined announcement feed (system-wide + their own zone), as well as
 * the Super Admin's management screen.
 */
@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, String> {

    /**
     * Lists all currently active announcements — used as the base query
     * for Super Admin's "Manage Announcements" screen default view.
     */
    List<Announcement> findByActiveTrue();

    /**
     * Lists active, system-wide announcements (zoneId is null) — combined
     * with findByZoneIdAndActiveTrue at the service layer to build a
     * citizen/worker's full visible announcement feed.
     */
    List<Announcement> findByZoneIdIsNullAndActiveTrue();

    /**
     * Lists active announcements scoped to a specific zone — used together
     * with findByZoneIdIsNullAndActiveTrue so a citizen/worker sees both
     * system-wide notices and ones specific to their own zone.
     */
    List<Announcement> findByZoneIdAndActiveTrue(String zoneId);

    /**
     * Lists all announcements (active and inactive) created by a specific
     * admin user — used for audit/history purposes on the admin side.
     */
    List<Announcement> findByCreatedBy(String createdBy);
}