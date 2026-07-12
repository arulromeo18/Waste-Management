package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.ZoneRequest;
import com.wastemanagement.wastesystem.dto.response.ZoneResponse;
import com.wastemanagement.wastesystem.exception.DuplicateResourceException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles administrative CRUD operations for zones — the central
 * organizing unit of the system (see Zone.java's class-level note).
 *
 * Zone creation and updates are exclusively a Super Admin action;
 * workers/citizens only ever reference an existing zone by id (enforced
 * at the controller layer via SecurityConfig's role-based URL rules,
 * not duplicated here in the service).
 *
 * createZone, updateZone, and setZoneActiveStatus each log an
 * AuditLogService entry once persistence succeeds, attributed to the
 * adminUserId threaded through from the controller layer — same pattern
 * established in UserService.
 */
@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final AuditLogService auditLogService;

    /**
     * Creates a new zone after verifying zoneName and zoneCode are both
     * unique — translating what would otherwise be a raw MongoDB
     * duplicate-key exception (from the @Indexed(unique = true)
     * constraints on Zone.java) into a clean, client-friendly validation
     * error before the write is even attempted.
     */
    public ZoneResponse createZone(String adminUserId, ZoneRequest request) {
        if (zoneRepository.existsByZoneName(request.getZoneName())) {
            throw new DuplicateResourceException("A zone with this name already exists");
        }
        if (zoneRepository.existsByZoneCode(request.getZoneCode())) {
            throw new DuplicateResourceException("A zone with this code already exists");
        }

        Zone zone = Zone.builder()
                .zoneName(request.getZoneName())
                .zoneCode(request.getZoneCode())
                .wardNumber(request.getWardNumber())
                .description(request.getDescription())
                .estimatedPopulation(request.getEstimatedPopulation())
                .coveredAreas(request.getCoveredAreas())
                .active(true)
                .build();

        zone = zoneRepository.save(zone);

        auditLogService.log(adminUserId, "ZONE_CREATED", "Zone", zone.getId(),
                "Created zone " + zone.getZoneName() + " (" + zone.getZoneCode() + ")");

        return toResponse(zone);
    }

    /**
     * Updates an existing zone's editable fields. Re-checks uniqueness
     * only when the name/code actually changed, so an update that leaves
     * them untouched doesn't falsely reject against the zone's own
     * existing record.
     */
    public ZoneResponse updateZone(String adminUserId, String zoneId, ZoneRequest request) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));

        if (!zone.getZoneName().equals(request.getZoneName())
                && zoneRepository.existsByZoneName(request.getZoneName())) {
            throw new DuplicateResourceException("A zone with this name already exists");
        }
        if (!zone.getZoneCode().equals(request.getZoneCode())
                && zoneRepository.existsByZoneCode(request.getZoneCode())) {
            throw new DuplicateResourceException("A zone with this code already exists");
        }

        zone.setZoneName(request.getZoneName());
        zone.setZoneCode(request.getZoneCode());
        zone.setWardNumber(request.getWardNumber());
        zone.setDescription(request.getDescription());
        zone.setEstimatedPopulation(request.getEstimatedPopulation());
        zone.setCoveredAreas(request.getCoveredAreas());

        zone = zoneRepository.save(zone);

        auditLogService.log(adminUserId, "ZONE_UPDATED", "Zone", zone.getId(),
                "Updated zone " + zone.getZoneName() + " (" + zone.getZoneCode() + ")");

        return toResponse(zone);
    }

    /**
     * Lists all zones regardless of active status — Super Admin needs
     * visibility into deactivated zones too (e.g. to reactivate one that
     * was merged back, or to review historical reports tied to it).
     */
    public List<ZoneResponse> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lists only active zones — used by Register.js's zone picker
     * dropdown, since a citizen should never be able to register into a
     * deactivated zone.
     */
    public List<ZoneResponse> getActiveZones() {
        return zoneRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public ZoneResponse getZoneById(String zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));
        return toResponse(zone);
    }

    /**
     * Activates or deactivates a zone. See Zone.java's class-level note:
     * a zone is soft-disabled rather than deleted, preserving historical
     * collection records and reports tied to its id.
     */
    public ZoneResponse setZoneActiveStatus(String adminUserId, String zoneId, boolean active) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));

        zone.setActive(active);
        zone = zoneRepository.save(zone);

        String action = active ? "ZONE_ACTIVATED" : "ZONE_DEACTIVATED";
        auditLogService.log(adminUserId, action, "Zone", zone.getId(),
                (active ? "Activated" : "Deactivated") + " zone " + zone.getZoneName());

        return toResponse(zone);
    }

    private ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
                .id(zone.getId())
                .zoneName(zone.getZoneName())
                .zoneCode(zone.getZoneCode())
                .wardNumber(zone.getWardNumber())
                .description(zone.getDescription())
                .estimatedPopulation(zone.getEstimatedPopulation())
                .coveredAreas(zone.getCoveredAreas())
                .active(zone.isActive())
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .build();
    }
}