package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a penalty (warning or fine) issued to a citizen for
 * non-compliant waste segregation, mirroring Reward.java's ledger
 * approach for the negative-consequence side of the same compliance
 * flow: a worker's CollectionRecord.segregationCompliant = false
 * assessment can lead Super Admin to issue a Penalty against that
 * citizen after review.
 *
 * Kept as its own collection (rather than a negative Reward entry)
 * since penalties carry a distinct lifecycle — they must be reviewed
 * and can be waived by the Super Admin — whereas rewards are credited
 * outright with no review step. Modeling them separately keeps each
 * document's fields and status semantics honest rather than forcing
 * one shape to serve two different real-world processes (Rule 18).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "penalties")
public class Penalty {

    @Id
    private String id;

    /**
     * References Citizen.id — the citizen this penalty is issued against.
     * Required: every penalty must be attributable to a citizen.
     */
    @NotBlank(message = "Citizen is required")
    @Indexed
    private String citizenId;

    /**
     * References CollectionRecord.id that triggered this penalty review
     * (typically one with segregationCompliant = false). Nullable — a
     * penalty may also be issued for a reason unrelated to a specific
     * logged collection (e.g. a substantiated complaint).
     */
    private String collectionRecordId;

    /**
     * References Complaint.id, if this penalty arose from a citizen
     * complaint against another party or a worker-conduct finding rather
     * than a direct segregation-compliance failure. Nullable.
     */
    private String complaintId;

    @NotBlank(message = "Reason is required")
    private String reason;

    /**
     * Optional monetary fine amount. Nullable/zero for a warning-only
     * penalty with no financial component — kept as BigDecimal (not
     * double) since this represents currency and must avoid floating
     * point rounding errors.
     */
    private BigDecimal fineAmount;

    /**
     * Lifecycle status of this penalty: "PENDING" (issued, awaiting citizen
     * acknowledgement/payment), "WAIVED" (Super Admin cancelled it after
     * review), or "SETTLED" (acknowledged/paid). Kept as a plain string
     * rather than an enum here since, unlike ComplaintStatus (which drives
     * branching service logic), this status is primarily informational
     * and the set of values may need admin-side tuning without a code
     * change (Rule 18) — revisit as an enum if branching logic grows.
     */
    @Builder.Default
    private String status = "PENDING";

    /**
     * References User.id of the Super Admin who issued this penalty.
     * Required: every penalty is admin-authored after review, unlike
     * Reward which supports a fully automated path.
     */
    @NotBlank(message = "Issuer is required")
    private String issuedBy;

    private String waivedReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}