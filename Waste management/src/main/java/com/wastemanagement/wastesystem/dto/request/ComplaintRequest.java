package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for POST /api/citizen/complaints.
 *
 * Submitted from SubmitComplaint.js. The citizenId and zoneId are
 * deliberately NOT part of this DTO — they are never trusted from the
 * client. Instead, ComplaintController (upcoming) resolves the currently
 * authenticated citizen from the JWT-derived SecurityUserPrincipal
 * (principal.getUserId() -> Citizen lookup by userId), and ComplaintService
 * denormalizes that citizen's zoneId onto the new Complaint document —
 * mirroring the same "never trust client-supplied identity fields"
 * principle already applied in RegisterRequest (role is hardcoded
 * server-side, not accepted from the client).
 *
 * collectionRecordId and workerId are optional, matching the nullable
 * fields on Complaint.java for complaints that aren't tied to a specific
 * logged pickup or worker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    private List<String> imageUrls;

    /**
     * Optional reference to the CollectionRecord this complaint relates
     * to (e.g. "the pickup logged on this date didn't happen at my
     * address").
     */
    private String collectionRecordId;

    /**
     * Optional reference to a specific worker this complaint concerns
     * (e.g. conduct-related complaints).
     */
    private String workerId;
}