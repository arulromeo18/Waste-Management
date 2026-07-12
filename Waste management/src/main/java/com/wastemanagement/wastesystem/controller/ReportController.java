package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.response.ComplaintResponse;
import com.wastemanagement.wastesystem.dto.response.CollectionRecordResponse;
import com.wastemanagement.wastesystem.dto.response.DashboardStatsResponse;
import com.wastemanagement.wastesystem.dto.response.UserResponse;
import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.service.CollectionRecordService;
import com.wastemanagement.wastesystem.service.ComplaintService;
import com.wastemanagement.wastesystem.service.DashboardAnalyticsService;
import com.wastemanagement.wastesystem.service.UserService;
import com.wastemanagement.wastesystem.util.ExportService;
import com.wastemanagement.wastesystem.util.PdfReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Super Admin report/export endpoints — PDF dashboard summary, and
 * CSV/Excel exports for citizens, workers, complaints, and collection
 * records. All routes live under "/api/admin/reports", already restricted
 * to SUPER_ADMIN by SecurityConfig's "/api/admin/**" matcher.
 *
 * Every export endpoint follows the same shape: fetch the data via the
 * relevant existing service (no new query logic here), define an inline
 * column map, delegate to ExportService, and stream the result with a
 * Content-Disposition header so the browser triggers a file download
 * rather than rendering the raw bytes.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final DashboardAnalyticsService dashboardAnalyticsService;
    private final PdfReportGenerator pdfReportGenerator;
    private final ExportService exportService;
    private final UserService userService;
    private final ComplaintService complaintService;
    private final CollectionRecordService collectionRecordService;

    /**
     * GET /api/admin/reports/dashboard/pdf
     * Downloads the current dashboard statistics as a formatted PDF.
     */
    @GetMapping("/dashboard/pdf")
    public ResponseEntity<byte[]> downloadDashboardPdf() {
        DashboardStatsResponse stats = dashboardAnalyticsService.getDashboardStats();
        byte[] pdfBytes = pdfReportGenerator.generateDashboardReport(stats);
        return fileResponse(pdfBytes, MediaType.APPLICATION_PDF, "dashboard-report.pdf");
    }

    /**
     * GET /api/admin/reports/citizens/csv
     * Downloads all citizen accounts as CSV.
     */
    @GetMapping("/citizens/csv")
    public ResponseEntity<byte[]> exportCitizensCsv() {
        List<UserResponse> citizens = userService.getUsersByRole(Role.CITIZEN);
        byte[] csvBytes = exportService.exportToCsv(citizens, citizenColumns());
        return fileResponse(csvBytes, new MediaType("text", "csv"), "citizens.csv");
    }

    /**
     * GET /api/admin/reports/citizens/excel
     * Downloads all citizen accounts as an Excel workbook.
     */
    @GetMapping("/citizens/excel")
    public ResponseEntity<byte[]> exportCitizensExcel() {
        List<UserResponse> citizens = userService.getUsersByRole(Role.CITIZEN);
        byte[] excelBytes = exportService.exportToExcel(citizens, citizenColumns(), "Citizens");
        return fileResponse(excelBytes, excelMediaType(), "citizens.xlsx");
    }

    /**
     * GET /api/admin/reports/workers/csv
     * Downloads all worker accounts as CSV.
     */
    @GetMapping("/workers/csv")
    public ResponseEntity<byte[]> exportWorkersCsv() {
        List<UserResponse> workers = userService.getUsersByRole(Role.WORKER);
        byte[] csvBytes = exportService.exportToCsv(workers, workerColumns());
        return fileResponse(csvBytes, new MediaType("text", "csv"), "workers.csv");
    }

    /**
     * GET /api/admin/reports/workers/excel
     * Downloads all worker accounts as an Excel workbook.
     */
    @GetMapping("/workers/excel")
    public ResponseEntity<byte[]> exportWorkersExcel() {
        List<UserResponse> workers = userService.getUsersByRole(Role.WORKER);
        byte[] excelBytes = exportService.exportToExcel(workers, workerColumns(), "Workers");
        return fileResponse(excelBytes, excelMediaType(), "workers.xlsx");
    }

    /**
     * GET /api/admin/reports/complaints/csv
     * Downloads every complaint system-wide as CSV.
     */
    @GetMapping("/complaints/csv")
    public ResponseEntity<byte[]> exportComplaintsCsv() {
        List<ComplaintResponse> complaints = complaintService.getAllComplaints();
        byte[] csvBytes = exportService.exportToCsv(complaints, complaintColumns());
        return fileResponse(csvBytes, new MediaType("text", "csv"), "complaints.csv");
    }

    /**
     * GET /api/admin/reports/complaints/excel
     * Downloads every complaint system-wide as an Excel workbook.
     */
    @GetMapping("/complaints/excel")
    public ResponseEntity<byte[]> exportComplaintsExcel() {
        List<ComplaintResponse> complaints = complaintService.getAllComplaints();
        byte[] excelBytes = exportService.exportToExcel(complaints, complaintColumns(), "Complaints");
        return fileResponse(excelBytes, excelMediaType(), "complaints.xlsx");
    }

    /**
     * GET /api/admin/reports/collections/csv?zoneId=...&start=YYYY-MM-DD&end=YYYY-MM-DD
     * Downloads a zone's collection records within a date range as CSV —
     * date-range-scoped (rather than a full unfiltered dump) since
     * collection records are the highest-volume collection in the system
     * and this directly matches the "Daily Reports"/"Monthly Reports"
     * features from the master spec.
     */
    @GetMapping("/collections/csv")
    public ResponseEntity<byte[]> exportCollectionsCsv(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CollectionRecordResponse> records = collectionRecordService.getRecordsForZoneInRange(zoneId, start, end);
        byte[] csvBytes = exportService.exportToCsv(records, collectionRecordColumns());
        return fileResponse(csvBytes, new MediaType("text", "csv"), "collection-records.csv");
    }

    /**
     * GET /api/admin/reports/collections/excel?zoneId=...&start=YYYY-MM-DD&end=YYYY-MM-DD
     * Downloads a zone's collection records within a date range as an
     * Excel workbook.
     */
    @GetMapping("/collections/excel")
    public ResponseEntity<byte[]> exportCollectionsExcel(
            @RequestParam String zoneId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<CollectionRecordResponse> records = collectionRecordService.getRecordsForZoneInRange(zoneId, start, end);
        byte[] excelBytes = exportService.exportToExcel(records, collectionRecordColumns(), "Collection Records");
        return fileResponse(excelBytes, excelMediaType(), "collection-records.xlsx");
    }

    // --- Column definitions (one per exportable entity) ---

    private LinkedHashMap<String, Function<UserResponse, Object>> citizenColumns() {
        LinkedHashMap<String, Function<UserResponse, Object>> columns = new LinkedHashMap<>();
        columns.put("Full Name", UserResponse::getFullName);
        columns.put("Email", UserResponse::getEmail);
        columns.put("Phone", UserResponse::getPhone);
        columns.put("Zone ID", UserResponse::getZoneId);
        columns.put("Reward Points", UserResponse::getRewardPoints);
        columns.put("Complaints Filed", UserResponse::getTotalComplaintsFiled);
        columns.put("Active", UserResponse::isActive);
        columns.put("Registered On", UserResponse::getCreatedAt);
        return columns;
    }

    private LinkedHashMap<String, Function<UserResponse, Object>> workerColumns() {
        LinkedHashMap<String, Function<UserResponse, Object>> columns = new LinkedHashMap<>();
        columns.put("Full Name", UserResponse::getFullName);
        columns.put("Email", UserResponse::getEmail);
        columns.put("Phone", UserResponse::getPhone);
        columns.put("Zone ID", UserResponse::getZoneId);
        columns.put("Vehicle ID", UserResponse::getVehicleId);
        columns.put("Employee ID", UserResponse::getEmployeeId);
        columns.put("Shift Timing", UserResponse::getShiftTiming);
        columns.put("Collections Logged", UserResponse::getTotalCollectionsLogged);
        columns.put("Active", UserResponse::isActive);
        columns.put("Onboarded On", UserResponse::getCreatedAt);
        return columns;
    }

    private LinkedHashMap<String, Function<ComplaintResponse, Object>> complaintColumns() {
        LinkedHashMap<String, Function<ComplaintResponse, Object>> columns = new LinkedHashMap<>();
        columns.put("Citizen", ComplaintResponse::getCitizenName);
        columns.put("Zone", ComplaintResponse::getZoneName);
        columns.put("Worker", ComplaintResponse::getWorkerName);
        columns.put("Category", ComplaintResponse::getCategory);
        columns.put("Description", ComplaintResponse::getDescription);
        columns.put("Status", ComplaintResponse::getStatus);
        columns.put("Resolution Remarks", ComplaintResponse::getResolutionRemarks);
        columns.put("Filed On", ComplaintResponse::getCreatedAt);
        columns.put("Resolved On", ComplaintResponse::getResolvedAt);
        return columns;
    }

    private LinkedHashMap<String, Function<CollectionRecordResponse, Object>> collectionRecordColumns() {
        LinkedHashMap<String, Function<CollectionRecordResponse, Object>> columns = new LinkedHashMap<>();
        columns.put("Zone", CollectionRecordResponse::getZoneName);
        columns.put("Worker", CollectionRecordResponse::getWorkerName);
        columns.put("Vehicle", CollectionRecordResponse::getVehicleNumber);
        columns.put("Collection Date", CollectionRecordResponse::getCollectionDate);
        columns.put("Segregation Compliant", CollectionRecordResponse::isSegregationCompliant);
        columns.put("Remarks", CollectionRecordResponse::getRemarks);
        return columns;
    }

    // --- Response-building helpers ---

    private ResponseEntity<byte[]> fileResponse(byte[] content, MediaType mediaType, String filename) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(content);
    }

    private MediaType excelMediaType() {
        return new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}