package com.wastemanagement.wastesystem.util;
import java.io.IOException;
import com.wastemanagement.wastesystem.exception.BadRequestException;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.wastemanagement.wastesystem.dto.response.DashboardStatsResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generates a formatted PDF summary report from DashboardStatsResponse —
 * backs the Super Admin's "Download PDF Reports" action (Reports.js).
 *
 * Deliberately consumes the SAME DashboardStatsResponse that
 * DashboardController.getDashboardStats() already returns as JSON, rather
 * than independently re-querying repositories — this guarantees the PDF
 * report and the on-screen dashboard can never show different numbers for
 * the same moment in time, and keeps all aggregation logic living in
 * exactly one place (DashboardAnalyticsService).
 *
 * Registered as a Spring @Component (constructor-injectable, even though
 * it currently has no dependencies of its own) rather than a static
 * utility class, for consistency with OtpGenerator's precedent and to
 * keep the door open for future dependencies (e.g. a logo image resource)
 * without changing how callers obtain an instance.
 */
@Component
public class PdfReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /**
     * Builds the full dashboard summary PDF as an in-memory byte array,
     * ready to be streamed directly in an HTTP response
     * (application/pdf) — no temp file written to disk.
     */
    public byte[] generateDashboardReport(DashboardStatsResponse stats) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {

            addTitle(document);
            addSectionHeading(document, "Headline Summary");
            addKeyValueTable(document, Map.of(
                    "Total Citizens", String.valueOf(stats.getTotalCitizens()),
                    "Total Workers", String.valueOf(stats.getTotalWorkers()),
                    "Total Vehicles", String.valueOf(stats.getTotalVehicles()),
                    "Total Zones", String.valueOf(stats.getTotalZones())
            ));

            addSectionHeading(document, "Complaint Overview");
            addKeyValueTable(document, Map.of(
                    "Pending", String.valueOf(stats.getPendingComplaints()),
                    "In Progress", String.valueOf(stats.getInProgressComplaints()),
                    "Resolved", String.valueOf(stats.getResolvedComplaints()),
                    "Rejected", String.valueOf(stats.getRejectedComplaints())
            ));

            addSectionHeading(document, "Collection & Segregation (This Month)");
            addKeyValueTable(document, Map.of(
                    "Total Collections", String.valueOf(stats.getTotalCollectionsThisMonth()),
                    "Compliant Collections", String.valueOf(stats.getCompliantCollectionsThisMonth()),
                    "Non-Compliant Collections", String.valueOf(stats.getNonCompliantCollectionsThisMonth()),
                    "Segregation Compliance", stats.getSegregationCompliancePercentage() + "%",
                    "Collection Efficiency", stats.getCollectionEfficiencyPercentage() + "%"
            ));

            addSectionHeading(document, "Rewards & Penalties");
            addKeyValueTable(document, Map.of(
                    "Reward Points Issued (This Month)", String.valueOf(stats.getTotalRewardPointsIssued()),
                    "Pending Penalties", String.valueOf(stats.getPendingPenalties())
            ));

            if (stats.getComplaintsByZone() != null && !stats.getComplaintsByZone().isEmpty()) {
                addSectionHeading(document, "Complaints by Zone");
                addZoneBreakdownTable(document, stats.getComplaintsByZone());
            }

            if (stats.getComplianceByZone() != null && !stats.getComplianceByZone().isEmpty()) {
                addSectionHeading(document, "Segregation Compliance by Zone");
                Map<String, String> formatted = new java.util.LinkedHashMap<>();
                stats.getComplianceByZone().forEach((zone, percent) -> formatted.put(zone, percent + "%"));
                addKeyValueTable(document, formatted);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Failed to generate PDF report");
        }

        return outputStream.toByteArray();
    }
    private void addTitle(Document document) {
        document.add(new Paragraph("Waste Segregation Monitoring System")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Dashboard Summary Report")
                .setFontSize(13)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(2));

        document.add(new Paragraph("Generated: " + LocalDateTime.now().format(TIMESTAMP_FORMAT))
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    private void addSectionHeading(Document document, String heading) {
        document.add(new Paragraph(heading)
                .setFontSize(13)
                .setBold()
                .setMarginTop(14)
                .setMarginBottom(6));
    }

    private void addKeyValueTable(Document document, Map<String, String> data) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        data.forEach((key, value) -> {
            table.addCell(labelCell(key));
            table.addCell(valueCell(value));
        });

        document.add(table);
    }

    private void addZoneBreakdownTable(Document document, Map<String, Long> zoneData) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        zoneData.forEach((zone, count) -> {
            table.addCell(labelCell(zone));
            table.addCell(valueCell(String.valueOf(count)));
        });

        document.add(table);
    }

    private Cell labelCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setBorder(null)
                .setPadding(4);
    }

    private Cell valueCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10).setBold())
                .setBorder(null)
                .setPadding(4)
                .setTextAlignment(TextAlignment.RIGHT);
    }
}