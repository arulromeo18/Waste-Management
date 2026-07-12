package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload for POST /api/admin/penalties.
 *
 * Submitted from Penalties.js after the Super Admin reviews a
 * non-compliant CollectionRecord or a substantiated Complaint and decides
 * to issue a penalty. Like AwardBonusRequest, citizenId is deliberately
 * included as a client-supplied field — the Super Admin is always acting
 * on someone else's account, so there's no "self" identity to infer from
 * the admin's own JWT. issuedBy (the admin's own id) is instead resolved
 * server-side by PenaltyController from the authenticated principal.
 *
 * collectionRecordId and complaintId are both optional, mirroring
 * Penalty.java's nullable fields — a penalty can originate from either,
 * both, or neither (e.g. a general conduct-based warning).
 *
 * fineAmount is optional: omitting it (or submitting null) represents a
 * warning-only penalty with no financial component, consistent with
 * Penalty.java's class-level note on fineAmount being nullable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuePenaltyRequest {

    @NotBlank(message = "Citizen is required")
    private String citizenId;

    private String collectionRecordId;

    private String complaintId;

    @NotBlank(message = "Reason is required")
    private String reason;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fine amount cannot be negative")
    private BigDecimal fineAmount;
}