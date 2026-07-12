package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/admin/rewards/bonus.
 *
 * Submitted from Rewards.js's "Award Bonus Points" action, feeding
 * RewardService.awardManualBonus(...) — see Reward.java's class-level
 * note on manual, Super-Admin-issued bonus awards (e.g. a community
 * cleanliness recognition), as distinct from the automatic per-collection
 * crediting that RewardService.awardPointsForCompliantCollection performs
 * with no client-facing endpoint of its own.
 *
 * citizenId is deliberately included here (unlike ComplaintRequest/
 * CollectionRecordRequest, which resolve identity from the JWT) since the
 * Super Admin is awarding points to a DIFFERENT person than themselves —
 * there is no "self" to infer from the admin's own authenticated identity.
 * createdBy (the admin's own id) is instead resolved server-side by
 * RewardController from the authenticated principal, consistent with how
 * ComplaintController resolves adminUserId for updateComplaintStatus.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardBonusRequest {

    @NotBlank(message = "Citizen is required")
    private String citizenId;

    @NotNull(message = "Points is required")
    @Positive(message = "Points must be a positive number")
    private Integer points;

    @NotBlank(message = "Reason is required")
    private String reason;
}