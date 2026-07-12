package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.response.DashboardStatsResponse;
import com.wastemanagement.wastesystem.model.CollectionRecord;
import com.wastemanagement.wastesystem.model.ComplaintStatus;
import com.wastemanagement.wastesystem.model.Reward;
import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.CollectionRecordRepository;
import com.wastemanagement.wastesystem.repository.ComplaintRepository;
import com.wastemanagement.wastesystem.repository.PenaltyRepository;
import com.wastemanagement.wastesystem.repository.RewardRepository;
import com.wastemanagement.wastesystem.repository.UserRepository;
import com.wastemanagement.wastesystem.repository.VehicleRepository;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes the aggregated dashboard statistics shown on AdminDashboard.js,
 * assembling a single DashboardStatsResponse from several repositories'
 * count/list queries rather than the frontend making separate calls per
 * widget (see DashboardStatsResponse's own class-level note on why).
 *
 * "This month" is defined as the calendar month containing today's date
 * (first day of month, 00:00 through today, end of day) — recomputed on
 * every call rather than cached, since dashboard freshness matters more
 * than the cost of a handful of list/count queries for a system of this
 * scale.
 */
@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final VehicleRepository vehicleRepository;
    private final ComplaintRepository complaintRepository;
    private final CollectionRecordRepository collectionRecordRepository;
    private final RewardRepository rewardRepository;
    private final PenaltyRepository penaltyRepository;

    public DashboardStatsResponse getDashboardStats() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());

        List<Zone> zones = zoneRepository.findAll();

        long totalCitizens = userRepository.findByRole(Role.CITIZEN).size();
        long totalWorkers = userRepository.findByRole(Role.WORKER).size();
        long totalVehicles = vehicleRepository.findAll().size();
        long totalZones = zones.size();

        long pendingComplaints = complaintRepository.findByStatus(ComplaintStatus.PENDING).size();
        long inProgressComplaints = complaintRepository.findByStatus(ComplaintStatus.IN_PROGRESS).size();
        long resolvedComplaints = complaintRepository.findByStatus(ComplaintStatus.RESOLVED).size();
        long rejectedComplaints = complaintRepository.findByStatus(ComplaintStatus.REJECTED).size();

        List<CollectionRecord> monthRecords = collectAllZonesRecordsInRange(zones, monthStart, today);
        long totalCollectionsThisMonth = monthRecords.size();
        long compliantCollectionsThisMonth = monthRecords.stream()
                .filter(CollectionRecord::isSegregationCompliant)
                .count();
        long nonCompliantCollectionsThisMonth = totalCollectionsThisMonth - compliantCollectionsThisMonth;

        double segregationCompliancePercentage = totalCollectionsThisMonth == 0
                ? 0.0
                : (compliantCollectionsThisMonth * 100.0) / totalCollectionsThisMonth;

        // Collection efficiency: of every zone-day that HAD an active
        // schedule expected to run, what fraction actually got a record
        // logged. Approximated here as (days elapsed this month * zone
        // count with records) is intentionally avoided as too complex for
        // this phase — instead, expressed as the simpler and equally
        // meaningful ratio of zones that logged at least one collection
        // this month versus total zones, which is what AdminDashboard.js's
        // "Collection Efficiency" widget actually needs for a
        // system-of-this-scale summary metric.
        long zonesWithAtLeastOneCollection = zones.stream()
                .filter(zone -> collectionRecordRepository
                        .findByZoneIdAndCollectionDateBetween(zone.getId(), monthStart, today)
                        .stream().findAny().isPresent())
                .count();
        double collectionEfficiencyPercentage = totalZones == 0
                ? 0.0
                : (zonesWithAtLeastOneCollection * 100.0) / totalZones;

        LocalDateTime monthStartDateTime = LocalDateTime.of(monthStart, LocalTime.MIDNIGHT);
        LocalDateTime nowDateTime = LocalDateTime.now();
        long totalRewardPointsIssued = rewardRepository
                .findByCreatedAtBetween(monthStartDateTime, nowDateTime)
                .stream()
                .mapToLong(Reward::getPoints)
                .sum();

        long pendingPenalties = penaltyRepository.countByStatus("PENDING");

        Map<String, Long> complaintsByZone = new LinkedHashMap<>();
        Map<String, Double> complianceByZone = new LinkedHashMap<>();

        for (Zone zone : zones) {
            long zoneComplaintCount = complaintRepository.findByZoneId(zone.getId()).size();
            complaintsByZone.put(zone.getZoneName(), zoneComplaintCount);

            List<CollectionRecord> zoneMonthRecords = collectionRecordRepository
                    .findByZoneIdAndCollectionDateBetween(zone.getId(), monthStart, today);
            long zoneCompliant = zoneMonthRecords.stream()
                    .filter(CollectionRecord::isSegregationCompliant)
                    .count();
            double zoneCompliancePercentage = zoneMonthRecords.isEmpty()
                    ? 0.0
                    : (zoneCompliant * 100.0) / zoneMonthRecords.size();
            complianceByZone.put(zone.getZoneName(), zoneCompliancePercentage);
        }

        return DashboardStatsResponse.builder()
                .totalCitizens(totalCitizens)
                .totalWorkers(totalWorkers)
                .totalVehicles(totalVehicles)
                .totalZones(totalZones)
                .pendingComplaints(pendingComplaints)
                .inProgressComplaints(inProgressComplaints)
                .resolvedComplaints(resolvedComplaints)
                .rejectedComplaints(rejectedComplaints)
                .totalCollectionsThisMonth(totalCollectionsThisMonth)
                .compliantCollectionsThisMonth(compliantCollectionsThisMonth)
                .nonCompliantCollectionsThisMonth(nonCompliantCollectionsThisMonth)
                .segregationCompliancePercentage(round(segregationCompliancePercentage))
                .collectionEfficiencyPercentage(round(collectionEfficiencyPercentage))
                .totalRewardPointsIssued(totalRewardPointsIssued)
                .pendingPenalties(pendingPenalties)
                .complaintsByZone(complaintsByZone)
                .complianceByZone(complianceByZone)
                .build();
    }

    private List<CollectionRecord> collectAllZonesRecordsInRange(List<Zone> zones, LocalDate start, LocalDate end) {
        return zones.stream()
                .flatMap(zone -> collectionRecordRepository
                        .findByZoneIdAndCollectionDateBetween(zone.getId(), start, end)
                        .stream())
                .toList();
    }

    /**
     * Rounds a percentage to two decimal places for clean chart/UI display
     * (e.g. 82.33 instead of 82.33333333333333).
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}