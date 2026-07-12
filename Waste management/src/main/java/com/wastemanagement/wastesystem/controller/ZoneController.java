package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.ZoneRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.ZoneResponse;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.ZoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Super Admin zone management endpoints.
 *
 * All CRUD/status-toggle endpoints here live under "/api/admin/zones",
 * which SecurityConfig restricts to SUPER_ADMIN via
 * .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN") — so no
 * additional @PreAuthorize annotations are needed on individual methods
 * in this class; the URL prefix alone enforces the role restriction.
 *
 * The one exception is getActiveZonesPublic(), deliberately mapped under
 * "/api/public/zones" (SecurityConfig's permitAll() matcher) rather than
 * "/api/admin/zones/active" — Register.js needs to populate its zone
 * picker dropdown BEFORE the citizen has an account or JWT, so this one
 * read-only, non-sensitive listing must be reachable without
 * authentication.
 *
 * createZone, updateZone, and setZoneActiveStatus now resolve the acting
 * Super Admin's id from the authenticated principal and pass it through
 * to ZoneService for audit logging.
 */
@RestController
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping("/api/admin/zones")
    public ResponseEntity<ApiResponse<ZoneResponse>> createZone(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody ZoneRequest request) {
        ZoneResponse zone = zoneService.createZone(principal.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Zone created successfully", zone));
    }

    @PutMapping("/api/admin/zones/{zoneId}")
    public ResponseEntity<ApiResponse<ZoneResponse>> updateZone(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String zoneId,
            @Valid @RequestBody ZoneRequest request) {
        ZoneResponse zone = zoneService.updateZone(principal.getUserId(), zoneId, request);
        return ResponseEntity.ok(ApiResponse.success("Zone updated successfully", zone));
    }

    @GetMapping("/api/admin/zones")
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> getAllZones() {
        List<ZoneResponse> zones = zoneService.getAllZones();
        return ResponseEntity.ok(ApiResponse.success("Zones retrieved successfully", zones));
    }

    @GetMapping("/api/admin/zones/{zoneId}")
    public ResponseEntity<ApiResponse<ZoneResponse>> getZoneById(@PathVariable String zoneId) {
        ZoneResponse zone = zoneService.getZoneById(zoneId);
        return ResponseEntity.ok(ApiResponse.success("Zone retrieved successfully", zone));
    }

    @PatchMapping("/api/admin/zones/{zoneId}/status")
    public ResponseEntity<ApiResponse<ZoneResponse>> setZoneActiveStatus(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String zoneId,
            @RequestParam boolean active) {
        ZoneResponse zone = zoneService.setZoneActiveStatus(principal.getUserId(), zoneId, active);
        String message = active ? "Zone activated successfully" : "Zone deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, zone));
    }

    @GetMapping("/api/public/zones")
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> getActiveZonesPublic() {
        List<ZoneResponse> zones = zoneService.getActiveZones();
        return ResponseEntity.ok(ApiResponse.success("Active zones retrieved successfully", zones));
    }
}