package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.AnnouncementRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Announcement;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.model.Worker;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.WorkerRepository;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Announcement management (Super Admin) and combined read-only feed
 * (Citizen + Worker) endpoints.
 *
 * AnnouncementService returns the Announcement domain model directly
 * rather than a dedicated response DTO — see its own class-level note on
 * why (no cross-entity name enrichment needed, unlike Complaint).
 *
 * The feed endpoint is shared by both CITIZEN and WORKER roles (per
 * SecurityConfig's existing hasAnyRole("SUPER_ADMIN", "WORKER", "CITIZEN")
 * rule for "/api/announcements/**"), so it must resolve the caller's
 * zoneId differently depending on which of the two roles is authenticated
 * — a citizen's zone lives on their Citizen profile, a worker's on their
 * Worker profile.
 */
@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final CitizenRepository citizenRepository;
    private final WorkerRepository workerRepository;

    /**
     * POST /api/admin/announcements
     * Creates a new announcement. The authoring admin's own id is
     * resolved from the authenticated principal, never accepted from the
     * request body (see AnnouncementRequest's class-level note).
     */
    @PostMapping("/api/admin/announcements")
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody AnnouncementRequest request) {
        Announcement announcement = announcementService.createAnnouncement(request, principal.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created successfully", announcement));
    }

    /**
     * PUT /api/admin/announcements/{announcementId}
     * Updates an existing announcement's content/scope/priority.
     */
    @PutMapping("/api/admin/announcements/{announcementId}")
    public ResponseEntity<ApiResponse<Announcement>> updateAnnouncement(
            @PathVariable String announcementId,
            @Valid @RequestBody AnnouncementRequest request) {
        Announcement announcement = announcementService.updateAnnouncement(announcementId, request);
        return ResponseEntity.ok(ApiResponse.success("Announcement updated successfully", announcement));
    }

    /**
     * DELETE /api/admin/announcements/{announcementId}
     * Deactivates (soft-disables) an announcement — mapped to DELETE
     * since that's the semantically expected verb from the frontend's
     * perspective ("remove this announcement"), even though the
     * underlying operation preserves the record per Announcement.java's
     * class-level note.
     */
    @DeleteMapping("/api/admin/announcements/{announcementId}")
    public ResponseEntity<ApiResponse<Announcement>> deactivateAnnouncement(@PathVariable String announcementId) {
        Announcement announcement = announcementService.deactivateAnnouncement(announcementId);
        return ResponseEntity.ok(ApiResponse.success("Announcement deactivated successfully", announcement));
    }

    /**
     * GET /api/admin/announcements
     * Lists all announcements (active and inactive) — Super Admin's
     * default management view.
     */
    @GetMapping("/api/admin/announcements")
    public ResponseEntity<ApiResponse<List<Announcement>>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncements();
        return ResponseEntity.ok(ApiResponse.success("Announcements retrieved successfully", announcements));
    }

    /**
     * GET /api/admin/announcements/{announcementId}
     * Retrieves a single announcement's full detail.
     */
    @GetMapping("/api/admin/announcements/{announcementId}")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncementById(@PathVariable String announcementId) {
        Announcement announcement = announcementService.getAnnouncementById(announcementId);
        return ResponseEntity.ok(ApiResponse.success("Announcement retrieved successfully", announcement));
    }

    /**
     * GET /api/announcements/feed
     * Returns the combined system-wide + own-zone announcement feed for
     * the authenticated citizen or worker. Resolves the caller's zoneId
     * from whichever profile (Citizen or Worker) matches their role,
     * since this endpoint is reachable by both.
     */
    @GetMapping("/api/announcements/feed")
    public ResponseEntity<ApiResponse<List<Announcement>>> getMyFeed(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {

        String zoneId = resolveZoneIdForCaller(principal);
        List<Announcement> feed = announcementService.getFeedForZone(zoneId);
        return ResponseEntity.ok(ApiResponse.success("Announcement feed retrieved successfully", feed));
    }

    /**
     * Resolves the zoneId to build a feed for, based on the authenticated
     * caller's role. Super Admin has no zone of their own and isn't
     * expected to call this endpoint in practice (they use
     * getAllAnnouncements() instead) — if they did, there is simply no
     * zone-specific feed to build, so this throws rather than silently
     * guessing.
     */
    private String resolveZoneIdForCaller(SecurityUserPrincipal principal) {
        Role role = principal.getUser().getRole();

        if (role == Role.CITIZEN) {
            Citizen citizen = citizenRepository.findByUserId(principal.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));
            return citizen.getZoneId();
        }

        if (role == Role.WORKER) {
            Worker worker = workerRepository.findByUserId(principal.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for this account"));
            return worker.getZoneId();
        }

        throw new ResourceNotFoundException("No zone-specific announcement feed applies to this account type");
    }
}