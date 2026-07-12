package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.AssignWorkerRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.UserResponse;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Super Admin's worker assignment endpoint.
 *
 * Kept as its own small controller (rather than folded into
 * UserController) since assignment is a distinct administrative action
 * with its own dedicated DTO and its own audit-worthy event
 * ("WORKER_ASSIGNED"), matching the reasoning already documented on
 * AssignWorkerRequest.
 *
 * The route lives under "/api/admin/workers", already restricted to
 * SUPER_ADMIN by SecurityConfig's "/api/admin/**" matcher - no
 * per-method @PreAuthorize needed.
 */
@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final UserService userService;

    /**
     * POST /api/admin/workers/{workerId}/assign
     * Assigns the given worker to a zone and, optionally, a vehicle.
     * The authenticated Super Admin's id is passed through for audit
     * logging, matching the same pattern used by every other admin
     * action in UserController.
     */
    /**
     * POST /api/admin/workers/{userId}/assign
     * Assigns the worker identified by their User id (the id shown
     * throughout ManageWorkers.js) to a zone and, optionally, a vehicle.
     */
    @PostMapping("/api/admin/workers/{userId}/assign")
    public ResponseEntity<ApiResponse<UserResponse>> assignWorker(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String userId,
            @Valid @RequestBody AssignWorkerRequest request) {
        UserResponse worker = userService.assignWorkerToZoneAndVehicle(
                principal.getUserId(), userId, request.getZoneId(), request.getVehicleId());
        return ResponseEntity.ok(ApiResponse.success("Worker assigned successfully", worker));
    }
}
