package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.exception.BadRequestException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Penalty;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.PenaltyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles penalty issuance, waiving, and settlement.
 *
 * Unlike RewardService (which can auto-credit points the moment a
 * compliant collection is logged), every Penalty requires a Super Admin
 * to explicitly issue it after review — see Penalty.java's class-level
 * note on why non-compliance is only ever auto-flagged
 * (CollectionRecord.segregationCompliant = false), never auto-penalized.
 *
 * Returns the Penalty domain model directly rather than a dedicated
 * response DTO, consistent with the same "no sensitive fields to strip"
 * reasoning already applied to Reward — every field here (reason,
 * fineAmount, status, issuedBy) is already safe to expose as-is.
 *
 * issuePenalty, waivePenalty, and settlePenalty each log an
 * AuditLogService entry once persistence succeeds, attributed to the
 * adminUserId threaded through from the controller layer — same pattern
 * established across UserService/ZoneService/VehicleService.
 */
@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final PenaltyRepository penaltyRepository;
    private final CitizenRepository citizenRepository;
    private final AuditLogService auditLogService;

    /**
     * Issues a new penalty against a citizen following Super Admin review
     * of a non-compliant collection record or a substantiated complaint.
     * fineAmount is optional — a warning-only penalty is valid (see
     * Penalty.java's class-level note on fineAmount being nullable).
     */
    public Penalty issuePenalty(String citizenId, String collectionRecordId, String complaintId,
                                String reason, BigDecimal fineAmount, String adminUserId) {
        if (!citizenRepository.existsById(citizenId)) {
            throw new ResourceNotFoundException("Citizen not found with id: " + citizenId);
        }

        Penalty penalty = Penalty.builder()
                .citizenId(citizenId)
                .collectionRecordId(collectionRecordId)
                .complaintId(complaintId)
                .reason(reason)
                .fineAmount(fineAmount)
                .status("PENDING")
                .issuedBy(adminUserId)
                .build();

        penalty = penaltyRepository.save(penalty);

        auditLogService.log(adminUserId, "PENALTY_ISSUED", "Penalty", penalty.getId(),
                "Issued penalty against citizen " + citizenId + ": " + reason);

        return penalty;
    }

    /**
     * Waives a pending penalty, recording why — used when the Super Admin
     * decides, on further review or citizen appeal, that the penalty
     * shouldn't stand. Only a PENDING penalty can be waived; an already
     * SETTLED one is historical and shouldn't be silently reopened.
     */
    public Penalty waivePenalty(String adminUserId, String penaltyId, String waivedReason) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Penalty not found with id: " + penaltyId));

        if (!"PENDING".equals(penalty.getStatus())) {
            throw new BadRequestException("Only a pending penalty can be waived");
        }

        penalty.setStatus("WAIVED");
        penalty.setWaivedReason(waivedReason);

        penalty = penaltyRepository.save(penalty);

        auditLogService.log(adminUserId, "PENALTY_WAIVED", "Penalty", penalty.getId(),
                "Waived penalty for citizen " + penalty.getCitizenId() + ": " + waivedReason);

        return penalty;
    }

    /**
     * Marks a pending penalty as settled — used once a citizen has
     * acknowledged a warning or paid an associated fine.
     */
    public Penalty settlePenalty(String adminUserId, String penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Penalty not found with id: " + penaltyId));

        if (!"PENDING".equals(penalty.getStatus())) {
            throw new BadRequestException("Only a pending penalty can be settled");
        }

        penalty.setStatus("SETTLED");

        penalty = penaltyRepository.save(penalty);

        auditLogService.log(adminUserId, "PENALTY_SETTLED", "Penalty", penalty.getId(),
                "Settled penalty for citizen " + penalty.getCitizenId());

        return penalty;
    }

    public List<Penalty> getPenaltiesForCitizen(String citizenId) {
        return penaltyRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId);
    }

    /**
     * Lists pending penalties system-wide — the base query for Super
     * Admin's Penalties.js management/review queue.
     */
    public List<Penalty> getPendingPenalties() {
        return penaltyRepository.findByStatus("PENDING");
    }

    public Penalty getPenaltyById(String penaltyId) {
        return penaltyRepository.findById(penaltyId)
                .orElseThrow(() -> new ResourceNotFoundException("Penalty not found with id: " + penaltyId));
    }
}