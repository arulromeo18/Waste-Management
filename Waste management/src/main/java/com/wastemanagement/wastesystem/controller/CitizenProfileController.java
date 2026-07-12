package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.ProfileUpdateRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.UserResponse;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Citizen self-service profile endpoints — backs Profile.js on the
 * frontend.
 *
 * This controller was missing entirely in an earlier pass (flagged during
 * project audit): ProfileUpdateRequest and the corresponding
 * UserService methods already existed, but no route ever exposed them,
 * so the frontend's Profile page had nothing to call.
 *
 * Lives under "/api/citizen/**", already restricted to CITIZEN by
 * SecurityConfig's role-based matcher — no per-method @PreAuthorize
 * needed, matching every other controller in this system.
 *
 * Both methods resolve the acting citizen's own userId from the
 * JWT-derived SecurityUserPrincipal rather than accepting it as a path
 * or body parameter — a citizen can only ever view or edit their own
 * profile, never another user's, by construction rather than by an
 * extra authorization check.
 */
@RestController
@RequestMapping("/api/citizen/profile")
@RequiredArgsConstructor
public class CitizenProfileController {

    private final UserService userService;

    /**
     * GET /api/citizen/profile
     * Returns the authenticated citizen's own profile.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        UserResponse profile = userService.getMyProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * PUT /api/citizen/profile
     * Updates the authenticated citizen's editable profile fields.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserResponse profile = userService.updateCitizenProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }
}
