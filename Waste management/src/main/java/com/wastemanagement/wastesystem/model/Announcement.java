package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a notice broadcast by the Super Admin to citizens and/or workers.
 *
 * An announcement may be system-wide (zoneId left null — e.g. "Municipal
 * holiday, no collection tomorrow") or scoped to a single zone (e.g.
 * "Vehicle breakdown in Zone 3, collection delayed"). Citizens and workers
 * only ever read announcements (Notifications.js / worker dashboard);
 * only the Super Admin creates, edits, or deactivates them.
 *
 * Kept as a standalone collection (rather than folded into Notification)
 * since announcements are broadcast content authored by an admin, while
 * Notification.java (upcoming) represents individual, often system-generated
 * per-user alerts — the two have different authorship and audience models
 * despite both being "things a user sees."
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "announcements")
public class Announcement {

    @Id
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    /**
     * References Zone.id. Null means this announcement is system-wide and
     * visible to all citizens/workers regardless of zone.
     */
    private String zoneId;

    /**
     * References User.id of the Super Admin who created this announcement —
     * kept as userId (not a role-specific profile id) since only Super Admin
     * authors announcements, and User is the common identity root, matching
     * the same pattern used for Complaint.resolvedBy.
     */
    @NotBlank(message = "Author is required")
    private String createdBy;

    /**
     * Free-text priority indicator (e.g. "NORMAL", "URGENT") used purely for
     * UI styling (badge color) on the frontend. Kept as a plain string rather
     * than an enum since it doesn't drive any backend logic or authorization
     * decision (Rule 18).
     */
    @Builder.Default
    private String priority = "NORMAL";

    /**
     * Soft-disable flag. An announcement is deactivated rather than deleted
     * once no longer relevant, preserving it for historical audit purposes
     * (e.g. "what was announced during the Zone 3 breakdown last month").
     */
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}