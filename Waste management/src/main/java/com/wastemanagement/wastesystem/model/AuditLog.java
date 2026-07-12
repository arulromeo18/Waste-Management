package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Records a single significant action taken within the system, forming an
 * immutable, append-only audit trail for accountability and traceability.
 *
 * Examples of actions logged: a Super Admin deactivating a worker, issuing
 * a penalty, verifying a complaint, creating a zone, or a citizen's account
 * being suspended. This is distinct from Spring's own request logging
 * (logging.level.* in application.properties) — AuditLog captures
 * business-meaningful actions in a structured, queryable form that can be
 * surfaced in a Super Admin "Audit Logs" screen, whereas application logs
 * are unstructured text for developer debugging.
 *
 * Entries are never updated or deleted through normal application flow —
 * only created — which is why this document has no @LastModifiedDate
 * field, unlike most other models in this system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    /**
     * References User.id of the actor who performed this action. Kept as
     * userId (the common identity root) rather than a role-specific
     * profile id, since actions can be performed by any of the three roles
     * (e.g. a worker updating collection status is also audit-worthy).
     */
    @NotBlank(message = "Actor is required")
    @Indexed
    private String performedBy;

    /**
     * Free-text action code (e.g. "WORKER_DEACTIVATED", "PENALTY_ISSUED",
     * "COMPLAINT_RESOLVED", "ZONE_CREATED"). Kept as a plain string rather
     * than an enum since the set of auditable actions spans every service
     * in the system and will grow over time; constraining it to a fixed
     * enum here would require touching this core model every time a new
     * service adds a new auditable action (Rule 18).
     */
    @NotBlank(message = "Action is required")
    @Indexed
    private String action;

    /**
     * Name of the entity type affected (e.g. "Worker", "Complaint",
     * "Zone") — used together with entityId to let the Super Admin UI
     * deep-link from an audit entry to the affected record.
     */
    private String entityType;

    /**
     * ID of the specific entity affected by this action, if applicable.
     * Nullable — some actions (e.g. a failed login attempt) aren't tied
     * to a single entity.
     */
    private String entityId;

    /**
     * Free-text human-readable summary of what happened (e.g. "Deactivated
     * worker John Doe due to extended leave"), rendered directly in the
     * Audit Logs screen so admins don't need to interpret raw field
     * changes themselves.
     */
    private String description;

    /**
     * IP address the action was performed from, captured for basic
     * security traceability (e.g. investigating suspicious admin activity).
     * Nullable since not every code path (e.g. internal scheduled jobs)
     * has a request context to extract this from.
     */
    private String ipAddress;

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;
}