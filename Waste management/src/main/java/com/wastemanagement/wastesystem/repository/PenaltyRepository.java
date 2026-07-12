package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Penalty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for the "penalties" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed for a citizen's penalty history
 * view, Super Admin's penalty management/review screen, and periodic
 * reporting.
 */
@Repository
public interface PenaltyRepository extends MongoRepository<Penalty, String> {

    /**
     * Lists all penalties issued against a citizen, most recent first —
     * used by the citizen's penalty history view and Super Admin's
     * per-citizen detail screen.
     */
    List<Penalty> findByCitizenIdOrderByCreatedAtDesc(String citizenId);

    /**
     * Lists penalties filtered by status — used by Super Admin's
     * "Manage Penalties" screen to narrow the queue to, e.g., only
     * PENDING penalties awaiting settlement.
     */
    List<Penalty> findByStatus(String status);

    /**
     * Lists penalties filtered by both citizen and status — used when a
     * citizen's profile view needs to show only their currently
     * outstanding (PENDING) penalties.
     */
    List<Penalty> findByCitizenIdAndStatus(String citizenId, String status);

    /**
     * Looks up penalties tied to a specific collection record — used to
     * check whether a non-compliant record has already resulted in a
     * penalty before Super Admin reviews it again.
     */
    List<Penalty> findByCollectionRecordId(String collectionRecordId);

    /**
     * Looks up penalties tied to a specific complaint — used when
     * resolving a complaint that led to a penalty, to cross-link the two
     * in the admin UI.
     */
    List<Penalty> findByComplaintId(String complaintId);

    /**
     * Lists penalties issued by a specific admin within a date range —
     * used by monthly/periodic penalty reports (Super Admin's Reports.js).
     */
    List<Penalty> findByIssuedByAndCreatedAtBetween(String issuedBy, LocalDateTime start, LocalDateTime end);

    /**
     * Counts all pending (unsettled) penalties system-wide — used for the
     * Super Admin dashboard's "pending penalties" summary widget.
     */
    long countByStatus(String status);
}