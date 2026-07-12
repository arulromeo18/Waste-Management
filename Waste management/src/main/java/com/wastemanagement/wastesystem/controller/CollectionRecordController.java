package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.CollectionRecordRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.CollectionRecordResponse;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.CollectionRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Collection record logging (worker) and reporting (Super Admin) endpoints.
 *
 * Worker-facing routes live under "/api/worker/collection-records",
 * restricted by SecurityConfig to WORKER. Admin-facing reporting routes
 * live under "/api/admin/collection-records", restricted to SUPER_ADMIN.
 *
 * CollectionRecordService already accepts the authenticated worker's
 * userId directly (resolving the Worker profile and zone internally),
 * so — unlike ScheduleController — no repository lookups are needed here;
 * this controller stays a thin HTTP boundary, mirroring ComplaintController's
 * pattern.
 */
@RestController
@RequiredArgsConstructor
public class CollectionRecordController {

    private final CollectionRecordService collectionRecordService;

    /**
     * POST /api/worker/collection-records
     * Logs a completed pickup on behalf of the authenticated worker.
     * CollectionRecordService rejects a duplicate submission for the same
     * zone/date and awards reward points automatically when
     * segregationCompliant = true.
     */
    @PostMapping("/api/worker/collection-records")
    public ResponseEntity<ApiResponse<CollectionRecordResponse>> logCollection(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody CollectionRecordRequest request) {
        CollectionRecordResponse record = collectionRecordService.logCollection(principal.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collection logged successfully", record));
    }

    /**
     * GET /api/worker/collection-records
     * Lists the authenticated worker's own collection history.
     */
    @GetMapping("/api/worker/collection-records")
    public ResponseEntity<ApiResponse<List<CollectionRecordResponse>>> getMyCollectionRecords(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        List<CollectionRecordResponse> records = collectionRecordService.getRecordsForWorker(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Collection records retrieved successfully", records));
    }

    /**
     * GET /api/admin/collection-records/zone/{zoneId}
     * Lists every collection record for a zone — Super Admin's zone-wise
     * collection history view.
     */
    @GetMapping("/api/admin/collection-records/zone/{zoneId}")
    public ResponseEntity<ApiResponse<List<CollectionRecordResponse>>> getRecordsForZone(
            @PathVariable String zoneId) {
        List<CollectionRecordResponse> records = collectionRecordService.getRecordsForZone(zoneId);
        return ResponseEntity.ok(ApiResponse.success("Collection records retrieved successfully", records));
    }

    /**
     * GET /api/admin/collection-records/zone/{zoneId}/range?start=YYYY-MM-DD&end=YYYY-MM-DD
     * Lists a zone's collection records within a date range — feeds
     * Super Admin's daily/monthly collection efficiency reports.
     * @DateTimeFormat(iso = DATE) lets Spring bind plain "YYYY-MM-DD"
     * query parameters directly into LocalDate without a custom converter.
     */
    @GetMapping("/api/admin/collection-records/zone/{zoneId}/range")
    public ResponseEntity<ApiResponse<List<CollectionRecordResponse>>> getRecordsForZoneInRange(
            @PathVariable String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CollectionRecordResponse> records =
                collectionRecordService.getRecordsForZoneInRange(zoneId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Collection records retrieved successfully", records));
    }

    /**
     * GET /api/admin/collection-records/non-compliant?start=YYYY-MM-DD&end=YYYY-MM-DD
     * Lists system-wide non-compliant records within a date range — feeds
     * the Super Admin's penalty-review queue (see
     * CollectionRecordService's class-level note on this feeding
     * PenaltyService).
     */
    @GetMapping("/api/admin/collection-records/non-compliant")
    public ResponseEntity<ApiResponse<List<CollectionRecordResponse>>> getNonCompliantRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CollectionRecordResponse> records = collectionRecordService.getNonCompliantRecords(start, end);
        return ResponseEntity.ok(ApiResponse.success("Non-compliant records retrieved successfully", records));
    }
}