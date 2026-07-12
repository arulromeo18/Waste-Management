package com.wastemanagement.wastesystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for POST /api/admin/workers/{workerId}/assign.
 *
 * Submitted from ManageWorkers.js when the Super Admin assigns a worker
 * to a zone and, optionally, a vehicle. Kept as a dedicated, narrow DTO
 * (rather than folding zone/vehicle assignment into a general
 * "update worker profile" request) because assignment is a distinct
 * administrative action with its own audit trail entry (AuditLogService,
 * upcoming, will log "WORKER_ASSIGNED" specifically) — mirroring the same
 * reasoning already applied on ZoneRequest/VehicleRequest for why
 * activation toggling is a separate endpoint from general field updates.
 *
 * workerId itself is taken from the path variable in
 * WorkerController/AdminController (upcoming), not duplicated in this
 * body — this DTO only carries what's actually being assigned TO the
 * worker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignWorkerRequest {

    @NotBlank(message = "Zone is required")
    private String zoneId;

    /**
     * Optional — a worker can be assigned to a zone without an immediate
     * vehicle assignment (e.g. newly onboarded, or their vehicle is under
     * maintenance), matching Worker.java's nullable vehicleId field.
     */
    private String vehicleId;
}