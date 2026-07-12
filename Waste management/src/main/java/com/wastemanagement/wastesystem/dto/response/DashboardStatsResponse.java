package com.wastemanagement.wastesystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Aggregated statistics payload for GET /api/admin/dashboard/stats,
 * powering AdminDashboard.js's summary cards and charts.
 *
 * Computed by DashboardAnalyticsService (upcoming) via a combination of
 * repository count/aggregation queries (e.g.
 * UserRepository.findByRole, ComplaintRepository.findByStatus,
 * CollectionRecordRepository custom aggregations) rather than the
 * frontend stitching together multiple separate API calls — one endpoint,
 * one response, one dashboard render.
 *
 * Kept as a single flat-ish DTO (with a couple of small Map fields for
 * breakdowns) rather than a deeply nested object graph, since the
 * dashboard's charts (Charts, per the master feature list) consume
 * simple key-value breakdowns directly compatible with common charting
 * libraries on the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // --- Headline counts ---
    private long totalCitizens;
    private long totalWorkers;
    private long totalVehicles;
    private long totalZones;

    // --- Complaint statistics ---
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long rejectedComplaints;

    // --- Collection & segregation statistics ---
    private long totalCollectionsThisMonth;
    private long compliantCollectionsThisMonth;
    private long nonCompliantCollectionsThisMonth;

    /**
     * Percentage (0-100) of this month's logged collections marked
     * segregationCompliant = true — the headline "Waste Segregation
     * Statistics" figure called out in the master feature list.
     */
    private double segregationCompliancePercentage;

    /**
     * Percentage (0-100) representing how many scheduled collections
     * were actually logged as completed this month — the "Collection
     * Efficiency" figure from the master feature list.
     */
    private double collectionEfficiencyPercentage;

    // --- Rewards & penalties ---
    private long totalRewardPointsIssued;
    private long pendingPenalties;

    /**
     * Zone-wise complaint volume breakdown (zoneName -> complaint count),
     * used to render a bar chart identifying underperforming zones,
     * consistent with Zone.java's class-level note on why analytics are
     * broken down zone-wise.
     */
    private Map<String, Long> complaintsByZone;

    /**
     * Zone-wise segregation compliance percentage (zoneName -> percentage),
     * used to render a comparative chart across zones.
     */
    private Map<String, Double> complianceByZone;
}