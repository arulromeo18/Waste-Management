package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.DashboardStatsResponse;
import com.wastemanagement.wastesystem.service.DashboardAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/**
 * Super Admin dashboard analytics endpoint.
 *
 * Lives under "/api/admin/dashboard", already restricted to SUPER_ADMIN by
 * SecurityConfig's "/api/admin/**" matcher — no per-method
 * @PreAuthorize needed, consistent with every other admin-scoped
 * controller in this codebase.
 *
 * Deliberately a single GET endpoint returning one aggregated
 * DashboardStatsResponse, rather than several smaller endpoints per
 * widget — see DashboardStatsResponse's own class-level note on why one
 * response, one dashboard render is the intended shape.
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAnalyticsService dashboardAnalyticsService;

    /**
     * GET /api/admin/dashboard/stats
     * Returns the full aggregated statistics payload for AdminDashboard.js.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardAnalyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }
}