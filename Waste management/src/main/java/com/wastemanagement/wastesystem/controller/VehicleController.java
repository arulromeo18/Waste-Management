package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.VehicleRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.VehicleResponse;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.VehicleService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Super Admin vehicle management endpoints.
 *
 * All routes live under "/api/admin/vehicles", already restricted to
 * SUPER_ADMIN by SecurityConfig's "/api/admin/**" matcher — no
 * per-method @PreAuthorize needed, consistent with every other
 * admin-scoped controller in this codebase (ZoneController, UserController).
 *
 * createVehicle, updateVehicle, setVehicleActiveStatus, and logMaintenance
 * now resolve the acting Super Admin's id from the authenticated
 * principal and pass it through to VehicleService for audit logging.
 */
@RestController
@RequestMapping("/api/admin/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse vehicle = vehicleService.createVehicle(principal.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered successfully", vehicle));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String vehicleId,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse vehicle = vehicleService.updateVehicle(principal.getUserId(), vehicleId, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", vehicle));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehicles(
            @RequestParam(required = false) String zoneId) {
        List<VehicleResponse> vehicles = zoneId != null
                ? vehicleService.getVehiclesByZone(zoneId)
                : vehicleService.getAllVehicles();
        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", vehicles));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getActiveVehiclesByZone(
            @RequestParam String zoneId) {
        List<VehicleResponse> vehicles = vehicleService.getActiveVehiclesByZone(zoneId);
        return ResponseEntity.ok(ApiResponse.success("Active vehicles retrieved successfully", vehicles));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable String vehicleId) {
        VehicleResponse vehicle = vehicleService.getVehicleById(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved successfully", vehicle));
    }

    @PatchMapping("/{vehicleId}/status")
    public ResponseEntity<ApiResponse<VehicleResponse>> setVehicleActiveStatus(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String vehicleId,
            @RequestParam boolean active) {
        VehicleResponse vehicle = vehicleService.setVehicleActiveStatus(principal.getUserId(), vehicleId, active);
        String message = active ? "Vehicle marked active" : "Vehicle marked under maintenance";
        return ResponseEntity.ok(ApiResponse.success(message, vehicle));
    }

    @PatchMapping("/{vehicleId}/maintenance")
    public ResponseEntity<ApiResponse<VehicleResponse>> logMaintenance(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String vehicleId) {
        VehicleResponse vehicle = vehicleService.logMaintenance(principal.getUserId(), vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance logged successfully", vehicle));
    }
}