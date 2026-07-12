package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.CreateWorkerRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.UserResponse;
import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Super Admin endpoints for managing Worker and Citizen accounts.
 *
 * Every route here lives under "/api/admin/users", which SecurityConfig
 * restricts to SUPER_ADMIN via .requestMatchers("/api/admin/**")
 * .hasRole("SUPER_ADMIN") — no per-method @PreAuthorize annotations are
 * needed, matching the same pattern already established in
 * ZoneController.
 *
 * Citizen accounts are never created here — citizens self-register via
 * AuthController.register(). This controller only ever creates WORKER
 * accounts (see UserService.createWorker()'s class-level note on why
 * admin-provisioned accounts are kept separate from public self-signup).
 *
 * State-changing methods now resolve the acting Super Admin's own id from
 * the authenticated principal and pass it through to UserService, which
 * uses it to attribute the resulting AuditLogService entry to whoever
 * actually performed the action.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /api/admin/users/workers
     * Provisions a new sanitation worker account. Returns the created
     * user's profile (no JWT — the Super Admin created this account, not
     * the worker themselves; the worker will log in separately via
     * AuthController.login()).
     */
    @PostMapping("/workers")
    public ResponseEntity<ApiResponse<UserResponse>> createWorker(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody CreateWorkerRequest request) {
        UserResponse worker = userService.createWorker(
                principal.getUserId(),
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                request.getZoneId(),
                request.getEmployeeId(),
                request.getShiftTiming()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Worker account created successfully", worker));
    }

    /**
     * GET /api/admin/users/workers
     * Lists all worker accounts — the base query behind ManageWorkers.js.
     */
    @GetMapping("/workers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllWorkers() {
        List<UserResponse> workers = userService.getUsersByRole(Role.WORKER);
        return ResponseEntity.ok(ApiResponse.success("Workers retrieved successfully", workers));
    }

    /**
     * GET /api/admin/users/citizens
     * Lists all citizen accounts — the base query behind ManageCitizens.js.
     */
    @GetMapping("/citizens")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllCitizens() {
        List<UserResponse> citizens = userService.getUsersByRole(Role.CITIZEN);
        return ResponseEntity.ok(ApiResponse.success("Citizens retrieved successfully", citizens));
    }

    /**
     * GET /api/admin/users/{userId}
     * Retrieves a single user's full profile — used by both
     * ManageWorkers.js and ManageCitizens.js detail views, since
     * UserResponse's role-specific fields make one endpoint sufficient
     * for either role.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    /**
     * PATCH /api/admin/users/{userId}/status?active=true|false
     * Activates or deactivates a user account. Kept as its own endpoint,
     * mirroring ZoneController's status-toggle pattern, rather than a
     * general profile update — account suspension is a distinct
     * administrative action, not a routine field edit.
     */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> setUserActiveStatus(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String userId,
            @RequestParam boolean active) {
        UserResponse user = userService.setUserActiveStatus(principal.getUserId(), userId, active);
        String message = active ? "Account activated successfully" : "Account deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, user));
    }
}