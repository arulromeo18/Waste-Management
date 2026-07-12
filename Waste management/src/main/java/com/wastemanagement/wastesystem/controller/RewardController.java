package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.AwardBonusRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Reward;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Reward-points endpoints: citizen's own history view, and Super Admin's
 * manual bonus-award action.
 *
 * RewardService returns the Reward domain model directly rather than a
 * dedicated response DTO — consistent with the same "no sensitive fields
 * to strip" reasoning already established for Penalty in PenaltyService;
 * every field on Reward (citizenId, points, reason, createdBy, createdAt)
 * is already safe to expose as-is, so this controller passes it through
 * unchanged rather than introducing a RewardResponse that would just
 * mirror the model 1:1.
 *
 * Automatic per-collection point crediting
 * (RewardService.awardPointsForCompliantCollection) has no endpoint of
 * its own here — it's invoked internally by CollectionRecordService
 * immediately after a compliant pickup is logged, never triggered
 * directly by a client request.
 */
@RestController
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final CitizenRepository citizenRepository;

    /**
     * GET /api/citizen/rewards
     * Retrieves the authenticated citizen's own reward history, most
     * recent first.
     */
    @GetMapping("/api/citizen/rewards")
    public ResponseEntity<ApiResponse<List<Reward>>> getMyRewardHistory(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Citizen citizen = citizenRepository.findByUserId(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));

        List<Reward> rewards = rewardService.getRewardHistoryForCitizen(citizen.getId());
        return ResponseEntity.ok(ApiResponse.success("Reward history retrieved successfully", rewards));
    }

    /**
     * POST /api/admin/rewards/bonus
     * Issues a manual, one-off bonus reward to a citizen — Rewards.js's
     * "Award Bonus Points" action. The awarding admin's own id is resolved
     * from the authenticated principal, never accepted from the request
     * body (see AwardBonusRequest's class-level note).
     */
    @PostMapping("/api/admin/rewards/bonus")
    public ResponseEntity<ApiResponse<Reward>> awardBonus(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody AwardBonusRequest request) {
        Reward reward = rewardService.awardManualBonus(
                request.getCitizenId(),
                request.getPoints(),
                request.getReason(),
                principal.getUserId()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bonus points awarded successfully", reward));
    }
}