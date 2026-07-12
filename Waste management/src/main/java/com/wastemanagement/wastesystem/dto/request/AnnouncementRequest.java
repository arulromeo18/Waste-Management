package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/admin/announcements and
 * PUT /api/admin/announcements/{id}.
 *
 * Submitted from Announcements.js. zoneId is optional here, matching
 * Announcement.java's nullable field: leaving it null creates a
 * system-wide announcement, while providing a zoneId scopes it to that
 * zone — see Announcement.java's class-level note for the full rationale.
 *
 * createdBy is deliberately NOT part of this DTO — AnnouncementController
 * (upcoming) resolves the currently authenticated Super Admin from the
 * JWT-derived SecurityUserPrincipal (principal.getUserId()) rather than
 * trusting a client-supplied author id, consistent with every other DTO
 * in this system that never accepts an identity field the client could
 * spoof (LoginRequest/RegisterRequest never accept role; ComplaintRequest
 * never accepts citizenId/zoneId; CollectionRecordRequest never accepts
 * workerId/zoneId).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String zoneId;

    @Builder.Default
    private String priority = "NORMAL";
}