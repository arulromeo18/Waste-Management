package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.AnnouncementRequest;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Announcement;
import com.wastemanagement.wastesystem.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles Super Admin management of announcements and building the
 * combined announcement feed a citizen/worker sees.
 *
 * Returns the Announcement domain model directly rather than a dedicated
 * response DTO — like Reward and Penalty, every field here is already
 * safe to expose as-is, and Announcement.java's own fields (zoneId,
 * createdBy) are id references the frontend already has enough context
 * to display without further enrichment (unlike Complaint, which needed
 * multi-entity name resolution).
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public Announcement createAnnouncement(AnnouncementRequest request, String adminUserId) {
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .zoneId(request.getZoneId())
                .createdBy(adminUserId)
                .priority(request.getPriority())
                .active(true)
                .build();

        return announcementRepository.save(announcement);
    }

    public Announcement updateAnnouncement(String announcementId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setZoneId(request.getZoneId());
        announcement.setPriority(request.getPriority());

        return announcementRepository.save(announcement);
    }

    /**
     * Deactivates an announcement rather than deleting it, preserving it
     * for historical audit purposes, per Announcement.java's class-level
     * note.
     */
    public Announcement deactivateAnnouncement(String announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));

        announcement.setActive(false);
        return announcementRepository.save(announcement);
    }

    /**
     * Lists all announcements (active and inactive) — Super Admin's
     * default "Manage Announcements" view.
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    /**
     * Builds a citizen/worker's combined announcement feed: system-wide
     * active announcements plus ones scoped to their own zone — see
     * AnnouncementRepository's class-level note on why this is split
     * into two queries rather than one OR-with-null query.
     */
    public List<Announcement> getFeedForZone(String zoneId) {
        List<Announcement> feed = new ArrayList<>();
        feed.addAll(announcementRepository.findByZoneIdIsNullAndActiveTrue());
        feed.addAll(announcementRepository.findByZoneIdAndActiveTrue(zoneId));
        feed.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return feed;
    }

    public Announcement getAnnouncementById(String announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + announcementId));
    }
}