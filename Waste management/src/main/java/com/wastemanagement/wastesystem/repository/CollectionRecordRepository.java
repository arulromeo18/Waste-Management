package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.CollectionRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for the "collection_records" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed to list a zone's/worker's
 * collection history, check whether a pickup was already logged for a given
 * zone/date (to prevent duplicate logging), and support date-range queries
 * for reporting and analytics.
 */
@Repository
public interface CollectionRecordRepository extends MongoRepository<CollectionRecord, String> {

    /**
     * Lists all collection records for a zone, most relevant for the
     * citizen-facing collection history view and Super Admin zone reports.
     * Ordering/pagination is left to the caller (Pageable overload can be
     * added later per Rule 18).
     */
    List<CollectionRecord> findByZoneId(String zoneId);

    /**
     * Lists all collection records logged by a given worker — used on the
     * worker dashboard to show their own collection history/activity.
     */
    List<CollectionRecord> findByWorkerId(String workerId);

    /**
     * Checks whether a pickup has already been logged for a zone on a given
     * date — used by UploadWasteImages.js submission flow to warn/prevent
     * a worker from double-logging the same day's collection.
     */
    boolean existsByZoneIdAndCollectionDate(String zoneId, LocalDate collectionDate);

    /**
     * Lists collection records for a zone within a date range — the core
     * query for Super Admin's Reports.js (collection frequency/efficiency
     * per zone) and DashboardAnalyticsService.
     */
    List<CollectionRecord> findByZoneIdAndCollectionDateBetween(
            String zoneId, LocalDate startDate, LocalDate endDate);

    /**
     * Lists collection records flagged as non-compliant (improper
     * segregation) within a date range — feeds PenaltyService's review
     * queue and Super Admin's segregation-compliance analytics.
     */
    List<CollectionRecord> findBySegregationCompliantFalseAndCollectionDateBetween(
            LocalDate startDate, LocalDate endDate);
}