package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.AuditLog;
import com.wastemanagement.wastesystem.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles creation and retrieval of AuditLog entries — the system's
 * append-only accountability trail (see AuditLog.java's class-level note).
 *
 * Every other service that performs a business-meaningful action (issuing a
 * penalty, verifying a complaint, deactivating a worker, creating a zone,
 * etc.) calls log(...) here at the end of the operation, rather than
 * injecting AuditLogRepository directly into itself. Centralizing this in
 * one service keeps the "how an entry is shaped and saved" logic in exactly
 * one place, so if the audit format ever changes (e.g. adding a new field),
 * only this class needs to change — not every calling service.
 *
 * Entries are never updated or deleted through this service, consistent
 * with AuditLog being append-only; there is deliberately no update/delete
 * method here.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Records a single audit entry. entityType/entityId/ipAddress are all
     * optional — pass null where not applicable (e.g. a failed login
     * attempt has no single entity affected).
     */
    public AuditLog log(String performedBy, String action, String entityType,
                        String entityId, String description, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .performedBy(performedBy)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .build();

        return auditLogRepository.save(auditLog);
    }

    /**
     * Convenience overload for the common case where the calling service
     * doesn't have access to the current HttpServletRequest (e.g. an
     * internal scheduled job) — ipAddress is simply omitted.
     */
    public AuditLog log(String performedBy, String action, String entityType,
                        String entityId, String description) {
        return log(performedBy, action, entityType, entityId, description, (String) null);
    }

    /**
     * Convenience overload that extracts the caller's IP directly from the
     * current request — used by controllers/services that DO have request
     * context, so they don't need to duplicate IP-extraction logic
     * themselves.
     */
    public AuditLog log(String performedBy, String action, String entityType,
                        String entityId, String description, HttpServletRequest request) {
        String ipAddress = extractIpAddress(request);
        return log(performedBy, action, entityType, entityId, description, ipAddress);
    }

    /**
     * Reviews a single user's (admin/worker) full activity history, most
     * recent first — used on the Super Admin's Audit Logs screen when
     * drilling into one actor's actions.
     */
    public List<AuditLog> getLogsByUser(String performedBy) {
        return auditLogRepository.findByPerformedByOrderByCreatedAtDesc(performedBy);
    }

    /**
     * Filters the Audit Logs screen down to a single action type
     * (e.g. "PENALTY_ISSUED", "ZONE_CREATED").
     */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }

    /**
     * Retrieves the full change history for a single entity (e.g. every
     * audit entry ever recorded against one specific Worker document) —
     * used for a "history" tab on an individual record's detail view.
     */
    public List<AuditLog> getLogsForEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Default time-bounded view for the Super Admin's Audit Logs screen
     * (e.g. "last 30 days"), most recent first.
     */
    public List<AuditLog> getLogsBetween(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    public AuditLog getLogById(String auditLogId) {
        return auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + auditLogId));
    }

    /**
     * Reads the originating client IP from standard proxy/load-balancer
     * headers first (X-Forwarded-For), falling back to the raw remote
     * address — necessary because in production this app typically sits
     * behind a reverse proxy/load balancer, which would otherwise make
     * request.getRemoteAddr() always return the proxy's own IP rather
     * than the real client's.
     */
    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // X-Forwarded-For can contain a comma-separated chain of proxies;
            // the first entry is the original client.
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}