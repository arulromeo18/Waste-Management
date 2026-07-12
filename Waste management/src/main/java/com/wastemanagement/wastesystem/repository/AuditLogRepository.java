package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for the "audit_logs" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations (note: only
 * save/find operations are ever actually used in practice, since audit
 * entries are append-only — see AuditLog.java class-level note) and
 * declares additional query-derived methods needed for the Super Admin's
 * Audit Logs screen.
 */
@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    /**
     * Lists all actions performed by a specific user, most recent first —
     * used to review a single admin/worker's activity history.
     */
    List<AuditLog> findByPerformedByOrderByCreatedAtDesc(String performedBy);

    /**
     * Lists all audit entries for a given action type, most recent first —
     * used when the Super Admin filters the Audit Logs screen to, e.g.,
     * only "PENALTY_ISSUED" events.
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Lists all audit entries affecting a specific entity — used to view
     * the full history of changes made to a single record (e.g. every
     * action ever taken on a particular Worker document).
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);

    /**
     * Lists audit entries within a date range, most recent first — used
     * as the base query for the Super Admin's Audit Logs screen default
     * time-bounded view (e.g. "last 30 days").
     */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}