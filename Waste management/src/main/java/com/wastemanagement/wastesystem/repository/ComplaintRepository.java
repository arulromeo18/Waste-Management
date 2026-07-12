package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Complaint;
import com.wastemanagement.wastesystem.model.ComplaintStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for the "complaints" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed for a citizen's own complaint
 * history (ComplaintHistory.js), Super Admin's complaint queue
 * (ComplaintsView.js) filtered by zone and/or status, and worker-conduct
 * lookups.
 */
@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {

    /**
     * Lists all complaints filed by a citizen — used by ComplaintHistory.js
     * so a citizen can track their own submissions and their statuses.
     */
    List<Complaint> findByCitizenId(String citizenId);

    /**
     * Lists all complaints within a zone — the base query for Super Admin's
     * ComplaintsView.js zone filter and zone-wise complaint-volume
     * analytics.
     */
    List<Complaint> findByZoneId(String zoneId);

    /**
     * Lists complaints within a zone filtered by status — used by
     * ComplaintsView.js when the Super Admin narrows the queue to, e.g.,
     * only PENDING complaints needing action.
     */
    List<Complaint> findByZoneIdAndStatus(String zoneId, ComplaintStatus status);

    /**
     * Lists complaints system-wide filtered by status — used for the Super
     * Admin dashboard's "pending complaints" count/widget without a zone
     * filter applied.
     */
    List<Complaint> findByStatus(ComplaintStatus status);

    /**
     * Lists complaints implicating a specific worker — used when reviewing
     * a worker's conduct history (e.g. before a performance review or
     * penalty decision).
     */
    List<Complaint> findByWorkerId(String workerId);

    /**
     * Counts complaints filed by a citizen — a cheaper alternative to
     * fetching the full list when only the count is needed (e.g.
     * cross-checking against Citizen.totalComplaintsFiled for data
     * integrity, or a summary view).
     */
    long countByCitizenId(String citizenId);
}