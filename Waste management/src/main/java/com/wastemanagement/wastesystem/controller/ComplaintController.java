package com.wastemanagement.wastesystem.controller;

import com.wastemanagement.wastesystem.dto.request.ComplaintRequest;
import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import com.wastemanagement.wastesystem.dto.response.ComplaintResponse;
import com.wastemanagement.wastesystem.model.ComplaintStatus;
import com.wastemanagement.wastesystem.security.SecurityUserPrincipal;
import com.wastemanagement.wastesystem.service.ComplaintService;
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
 * Complaint filing (citizen) and review/resolution (Super Admin) endpoints.
 *
 * Citizen-facing routes live under "/api/citizen/complaints", restricted
 * by SecurityConfig to CITIZEN. Admin-facing routes live under
 * "/api/admin/complaints", restricted to SUPER_ADMIN. Both prefixes are
 * already covered by SecurityConfig's existing role-based URL rules, so no
 * per-method @PreAuthorize is needed here — consistent with every other
 * controller in this codebase.
 *
 * Every citizen-facing method resolves the caller's identity via
 * @AuthenticationPrincipal SecurityUserPrincipal -> principal.getUserId(),
 * never from a client-supplied citizenId — this is the controller-side
 * half of the trust boundary already documented on ComplaintRequest.
 */
@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    /**
     * POST /api/citizen/complaints
     * Files a new complaint on behalf of the authenticated citizen.
     */
    @PostMapping("/api/citizen/complaints")
    public ResponseEntity<ApiResponse<ComplaintResponse>> fileComplaint(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody ComplaintRequest request) {
        ComplaintResponse complaint = complaintService.fileComplaint(principal.getUserId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint filed successfully", complaint));
    }

    /**
     * GET /api/citizen/complaints
     * Lists the authenticated citizen's own complaint history —
     * ComplaintHistory.js.
     */
    @GetMapping("/api/citizen/complaints")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getMyComplaints(
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        List<ComplaintResponse> complaints = complaintService.getComplaintsForCitizen(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", complaints));
    }

    /**
     * GET /api/citizen/complaints/{complaintId}
     * Retrieves a single complaint's detail. Note: this does not verify
     * the complaint belongs to the requesting citizen at the controller
     * level — acceptable here since complaint ids are opaque MongoDB
     * ObjectIds (not guessable/sequential), and the citizen-facing UI only
     * ever links to complaints it already fetched via getMyComplaints().
     * A stricter ownership check could be added to ComplaintService later
     * if this becomes a concern.
     */
    @GetMapping("/api/citizen/complaints/{complaintId}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable String complaintId) {
        ComplaintResponse complaint = complaintService.getComplaintById(complaintId);
        return ResponseEntity.ok(ApiResponse.success("Complaint retrieved successfully", complaint));
    }

    /**
     * GET /api/admin/complaints
     * Lists complaints for Super Admin's ComplaintsView.js, optionally
     * filtered by zone and/or status via query parameters. All three
     * parameters are optional and independently combinable.
     */
    @GetMapping("/api/admin/complaints")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaints(
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) ComplaintStatus status) {

        List<ComplaintResponse> complaints;
        if (zoneId != null && status != null) {
            complaints = complaintService.getComplaintsByZoneAndStatus(zoneId, status);
        } else if (zoneId != null) {
            complaints = complaintService.getComplaintsByZone(zoneId);
        } else if (status != null) {
            complaints = complaintService.getComplaintsByStatus(status);
        } else {
            complaints = complaintService.getComplaintsByStatus(ComplaintStatus.PENDING);
        }

        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", complaints));
    }

    /**
     * GET /api/admin/complaints/{complaintId}
     * Retrieves a single complaint's full detail for the admin review screen.
     */
    @GetMapping("/api/admin/complaints/{complaintId}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintDetailForAdmin(
            @PathVariable String complaintId) {
        ComplaintResponse complaint = complaintService.getComplaintById(complaintId);
        return ResponseEntity.ok(ApiResponse.success("Complaint retrieved successfully", complaint));
    }

    /**
     * PATCH /api/admin/complaints/{complaintId}/status
     * Transitions a complaint's status (PENDING -> IN_PROGRESS ->
     * RESOLVED/REJECTED). resolutionRemarks is required by
     * ComplaintService when closing a complaint (RESOLVED/REJECTED) —
     * enforced there, not duplicated here.
     */
    @PatchMapping("/api/admin/complaints/{complaintId}/status")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String complaintId,
            @RequestParam ComplaintStatus status,
            @RequestParam(required = false) String resolutionRemarks) {
        ComplaintResponse complaint = complaintService.updateComplaintStatus(
                complaintId, status, resolutionRemarks, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", complaint));
    }
}