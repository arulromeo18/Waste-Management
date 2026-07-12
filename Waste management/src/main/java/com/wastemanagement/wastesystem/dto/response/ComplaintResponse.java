package com.wastemanagement.wastesystem.dto.response;

import com.wastemanagement.wastesystem.model.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outward-facing representation of a Complaint, returned by
 * ComplaintController (upcoming) for both ComplaintHistory.js (citizen's
 * own view) and ComplaintsView.js (Super Admin's queue).
 *
 * Enriches the raw Complaint document with human-readable names
 * (citizenName, zoneName, workerName) resolved by ComplaintService
 * (upcoming) via lookups against Citizen/Zone/Worker at read time — the
 * persisted Complaint document itself only stores ids (citizenId, zoneId,
 * workerId) to stay normalized, but the frontend needs display-ready
 * names rather than forcing ComplaintsView.js to make three additional
 * lookups per row just to render a readable table.
 *
 * This "enrich ids into names at the response layer, keep ids in the
 * persisted model" approach is intentionally NOT applied inside the
 * Complaint document itself, consistent with Rule 18: denormalizing
 * display names onto the write model would require updating every
 * historical complaint if a citizen or zone is later renamed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private String id;

    private String citizenId;
    private String citizenName;

    private String zoneId;
    private String zoneName;

    private String collectionRecordId;

    private String workerId;
    private String workerName;

    private String category;

    private String description;

    private List<String> imageUrls;

    private ComplaintStatus status;

    private String resolvedBy;

    private String resolutionRemarks;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}