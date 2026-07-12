package com.wastemanagement.wastesystem.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a complaint filed by a citizen — e.g. missed pickup, improper
 * collection, worker conduct, or a general civic waste-management issue.
 *
 * A Complaint may optionally reference the CollectionRecord it relates to
 * (e.g. "the pickup logged on this date didn't actually happen at my
 * address"), but is not required to — citizens can also file complaints
 * that aren't tied to any specific logged collection (e.g. an overflowing
 * public bin). Status transitions (SubmitComplaint.js -> ComplaintsView.js
 * -> resolution) are tracked via the ComplaintStatus enum (File 28).
 *
 * Filing increments Citizen.totalComplaintsFiled (File 17) as a lightweight
 * denormalized counter — see ComplaintService (upcoming) for where that
 * increment happens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "complaints")
public class Complaint {

    @Id
    private String id;

    /**
     * References Citizen.id — the citizen who filed this complaint.
     * Required: every complaint must be attributable to a citizen.
     */
    @NotBlank(message = "Citizen is required")
    @Indexed
    private String citizenId;

    /**
     * References Zone.id — denormalized from the citizen's profile at
     * filing time, so zone-wise complaint volume can be queried directly
     * without joining through Citizen. Used by Super Admin's zone-wise
     * complaint analytics and ComplaintsView.js filters.
     */
    @NotBlank(message = "Zone is required")
    private String zoneId;

    /**
     * References CollectionRecord.id, if this complaint relates to a
     * specific logged pickup. Nullable — see class-level note.
     */
    private String collectionRecordId;

    /**
     * References Worker.id, if the complaint is about a specific worker's
     * conduct or a specific worker's serviced pickup. Nullable — not every
     * complaint (e.g. "public bin overflowing") implicates a worker.
     */
    private String workerId;

    /**
     * Free-text category (e.g. "Missed Pickup", "Improper Collection",
     * "Worker Conduct", "Overflowing Bin"). Kept as a plain string rather
     * than an enum since the spec doesn't fix a closed category list and
     * Super Admin may want to adjust categories without a code change
     * (Rule 18).
     */
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    /**
     * Optional photo evidence uploaded by the citizen when filing, same
     * storage convention as CollectionRecord.imageUrls.
     */
    private List<String> imageUrls;

    @NotNull(message = "Status is required")
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.PENDING;

    /**
     * References User.id of the admin who resolved/closed this complaint —
     * kept as userId (not a Citizen/Worker id) since only Super Admin
     * resolves complaints, and User is the common identity root.
     */
    private String resolvedBy;

    private String resolutionRemarks;

    private LocalDateTime resolvedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}