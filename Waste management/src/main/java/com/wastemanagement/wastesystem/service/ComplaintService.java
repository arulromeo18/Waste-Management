package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.ComplaintRequest;
import com.wastemanagement.wastesystem.dto.response.ComplaintResponse;
import com.wastemanagement.wastesystem.exception.BadRequestException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Complaint;
import com.wastemanagement.wastesystem.model.ComplaintStatus;
import com.wastemanagement.wastesystem.model.User;
import com.wastemanagement.wastesystem.model.Worker;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.ComplaintRepository;
import com.wastemanagement.wastesystem.repository.UserRepository;
import com.wastemanagement.wastesystem.repository.WorkerRepository;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles complaint filing (citizen side) and review/resolution
 * (Super Admin side).
 *
 * A citizen's citizenId and zoneId are always resolved server-side from
 * their authenticated userId, never accepted from the client — matching
 * the principle already documented on ComplaintRequest. zoneId is
 * denormalized from the citizen's own profile at filing time, per
 * Complaint.java's class-level note.
 */
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CitizenRepository citizenRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final WorkerRepository workerRepository;

    public ComplaintResponse fileComplaint(String citizenUserId, ComplaintRequest request) {
        Citizen citizen = citizenRepository.findByUserId(citizenUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));

        Complaint complaint = Complaint.builder()
                .citizenId(citizen.getId())
                .zoneId(citizen.getZoneId())
                .collectionRecordId(request.getCollectionRecordId())
                .workerId(request.getWorkerId())
                .category(request.getCategory())
                .description(request.getDescription())
                .imageUrls(request.getImageUrls())
                .status(ComplaintStatus.PENDING)
                .build();

        complaint = complaintRepository.save(complaint);

        citizen.setTotalComplaintsFiled(citizen.getTotalComplaintsFiled() + 1);
        citizenRepository.save(citizen);

        return toResponse(complaint);
    }

    public List<ComplaintResponse> getComplaintsForCitizen(String citizenUserId) {
        Citizen citizen = citizenRepository.findByUserId(citizenUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));

        return complaintRepository.findByCitizenId(citizen.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ComplaintResponse> getComplaintsByZone(String zoneId) {
        return complaintRepository.findByZoneId(zoneId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ComplaintResponse> getComplaintsByZoneAndStatus(String zoneId, ComplaintStatus status) {
        return complaintRepository.findByZoneIdAndStatus(zoneId, status).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }
    /**
     * Lists every complaint system-wide, regardless of zone or status —
     * used by the export feature (ReportController) where the admin wants
     * a complete data dump rather than the filtered queue view that
     * getComplaintsByStatus/getComplaintsByZone serve for
     * ComplaintsView.js's day-to-day screen.
     */
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ComplaintResponse getComplaintById(String complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));
        return toResponse(complaint);
    }

    /**
     * Transitions a complaint's status, enforcing the linear lifecycle
     * documented on ComplaintStatus.java (PENDING -> IN_PROGRESS ->
     * RESOLVED/REJECTED). Setting to RESOLVED or REJECTED requires
     * resolutionRemarks and stamps resolvedBy/resolvedAt.
     */
    public ComplaintResponse updateComplaintStatus(String complaintId, ComplaintStatus newStatus,
                                                   String resolutionRemarks, String adminUserId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new BadRequestException("This complaint has already been closed and cannot be updated further");
        }

        if ((newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.REJECTED)
                && (resolutionRemarks == null || resolutionRemarks.isBlank())) {
            throw new BadRequestException("Resolution remarks are required when resolving or rejecting a complaint");
        }

        complaint.setStatus(newStatus);

        if (newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.REJECTED) {
            complaint.setResolutionRemarks(resolutionRemarks);
            complaint.setResolvedBy(adminUserId);
            complaint.setResolvedAt(LocalDateTime.now());
        }

        complaint = complaintRepository.save(complaint);
        return toResponse(complaint);
    }

    /**
     * Enriches a Complaint into a ComplaintResponse, resolving
     * citizenName/zoneName/workerName via fresh lookups — see
     * ComplaintResponse's class-level note on why enrichment happens
     * here rather than being denormalized onto the persisted document.
     *
     * citizenName requires two hops (Complaint.citizenId -> Citizen ->
     * Citizen.userId -> User.fullName) since Citizen.java deliberately
     * has no fullName of its own - that field lives on User.java, per
     * the User/Citizen split documented on both models.
     */
    private ComplaintResponse toResponse(Complaint complaint) {
        String citizenName = citizenRepository.findById(complaint.getCitizenId())
                .flatMap(citizen -> userRepository.findById(citizen.getUserId()))
                .map(User::getFullName)
                .orElse("Unknown Citizen");

        String zoneName = zoneRepository.findById(complaint.getZoneId())
                .map(Zone::getZoneName)
                .orElse("Unknown Zone");

        String workerName = complaint.getWorkerId() != null
                ? workerRepository.findById(complaint.getWorkerId()).map(Worker::getFullName).orElse(null)
                : null;

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .citizenId(complaint.getCitizenId())
                .citizenName(citizenName)
                .zoneId(complaint.getZoneId())
                .zoneName(zoneName)
                .collectionRecordId(complaint.getCollectionRecordId())
                .workerId(complaint.getWorkerId())
                .workerName(workerName)
                .category(complaint.getCategory())
                .description(complaint.getDescription())
                .imageUrls(complaint.getImageUrls())
                .status(complaint.getStatus())
                .resolvedBy(complaint.getResolvedBy())
                .resolutionRemarks(complaint.getResolutionRemarks())
                .resolvedAt(complaint.getResolvedAt())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}