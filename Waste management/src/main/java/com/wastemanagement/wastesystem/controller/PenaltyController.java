package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.IssuePenaltyRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Penalty;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.PenaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Penalty issuance/review (Super Admin) and history view (citizen)
 * endpoints.
 *
 * waivePenalty and settlePenalty now resolve the acting Super Admin's id
 * from the authenticated principal and pass it through to PenaltyService
 * for audit logging. issuePenalty already resolved adminUserId this way
 * from the start (see IssuePenaltyRequest's class-level note).
 */
@RestController
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;
    private final CitizenRepository citizenRepository;

    @PostMapping("/api/admin/penalties")
    public ResponseEntity<ApiResponse<Penalty>> issuePenalty(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody IssuePenaltyRequest request) {
        Penalty penalty = penaltyService.issuePenalty(
                request.getCitizenId(),
                request.getCollectionRecordId(),
                request.getComplaintId(),
                request.getReason(),
                request.getFineAmount(),
                principal.getUserId()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Penalty issued successfully", penalty));
    }

    @PatchMapping("/api/admin/penalties/{penaltyId}/waive")
    public ResponseEntity<ApiResponse<Penalty>> waivePenalty(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String penaltyId,
            @RequestParam String waivedReason) {
        Penalty penalty = penaltyService.waivePenalty(principal.getUserId(), penaltyId, waivedReason);
        return ResponseEntity.ok(ApiResponse.success("Penalty waived successfully", penalty));
    }

    @PatchMapping("/api/admin/penalties/{penaltyId}/settle")
    public ResponseEntity<ApiResponse<Penalty>> settlePenalty(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String penaltyId) {
        Penalty penalty = penaltyService.settlePenalty(principal.getUserId(), penaltyId);
        return ResponseEntity.ok(ApiResponse.success("Penalty settled successfully", penalty));
    }

    @GetMapping("/api/admin/penalties/pending")
    public ResponseEntity<ApiResponse<List<Penalty>>> getPendingPenalties() {
        List<Penalty> penalties = penaltyService.getPendingPenalties();
        return ResponseEntity.ok(ApiResponse.success("Pending penalties retrieved successfully", penalties));
    }

    @GetMapping("/api/admin/penalties/{penaltyId}")
    public ResponseEntity<ApiResponse<Penalty>> getPenaltyById(@PathVariable String penaltyId) {
        Penalty penalty = penaltyService.getPenaltyById(penaltyId);
        return ResponseEntity.ok(ApiResponse.success("Penalty retrieved successfully", penalty));
    }

    @GetMapping("/api/citizen/penalties")
    public ResponseEntity<ApiResponse<List<Penalty>>> getMyPenalties(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Citizen citizen = citizenRepository.findByUserId(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));

        List<Penalty> penalties = penaltyService.getPenaltiesForCitizen(citizen.getId());
        return ResponseEntity.ok(ApiResponse.success("Penalties retrieved successfully", penalties));
    }
}