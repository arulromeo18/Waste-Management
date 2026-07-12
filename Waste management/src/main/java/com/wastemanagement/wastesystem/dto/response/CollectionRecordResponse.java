package com.wastemanagement.wastesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Outward-facing representation of a CollectionRecord, returned by
 * CollectionRecordController (upcoming) for a worker's own collection
 * history and Super Admin's zone-wise collection reports.
 *
 * Enriches the raw document with zoneName/workerName/vehicleNumber,
 * resolved by CollectionRecordService at read time — the same
 * "enrich ids into names at the response layer" pattern already
 * established in ComplaintResponse, VehicleResponse, and ScheduleResponse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRecordResponse {

    private String id;

    private String scheduleId;

    private String zoneId;
    private String zoneName;

    private String workerId;
    private String workerName;

    private String vehicleId;
    private String vehicleNumber;

    private LocalDate collectionDate;

    private LocalDateTime collectedAt;

    private boolean segregationCompliant;

    private List<String> imageUrls;

    private String remarks;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}