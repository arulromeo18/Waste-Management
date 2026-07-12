package com.wastemanagement.wastesystem.model;

/**
 * Lifecycle states for a Complaint (File 27).
 *
 * Transition flow, driven by ComplaintService (upcoming):
 *
 *   PENDING -> IN_PROGRESS -> RESOLVED
 *                          -> REJECTED
 *
 * A complaint starts as PENDING when a citizen files it (SubmitComplaint.js).
 * Super Admin (via ComplaintsView.js) moves it to IN_PROGRESS once action is
 * being taken, then to either RESOLVED (issue addressed) or REJECTED
 * (invalid/duplicate/not actionable). Kept as a flat, linear set of states
 * rather than a richer workflow engine, since the spec doesn't call for
 * reopening, escalation tiers, or SLA timers (Rule 18) — those can be added
 * later without touching existing complaint documents, since Mongo doesn't
 * enforce a schema on unused fields.
 */
public enum ComplaintStatus {

    /** Filed by the citizen, not yet reviewed by Super Admin. */
    PENDING,

    /** Acknowledged and actively being worked on by Super Admin/worker. */
    IN_PROGRESS,

    /** Issue addressed; resolutionRemarks and resolvedAt are set. */
    RESOLVED,

    /** Deemed invalid, duplicate, or not actionable; resolutionRemarks explains why. */
    REJECTED
}