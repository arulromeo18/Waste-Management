package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.VehicleRequest;
import com.wastemanagement.wastesystem.dto.response.VehicleResponse;
import com.wastemanagement.wastesystem.exception.DuplicateResourceException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Vehicle;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.VehicleRepository;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles administrative CRUD operations for vehicles.
 *
 * Every vehicle must reference a valid, existing zone (Vehicle.zoneId) —
 * this service validates that reference at creation/update time so a
 * typo'd or stale zoneId from the frontend fails with a clean
 * ResourceNotFoundException rather than silently persisting a broken
 * reference that would only surface later as a null zoneName in
 * VehicleResponse.
 *
 * createVehicle, updateVehicle, setVehicleActiveStatus, and logMaintenance
 * each log an AuditLogService entry once persistence succeeds, attributed
 * to the adminUserId threaded through from the controller layer — same
 * pattern established in UserService/ZoneService.
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ZoneRepository zoneRepository;
    private final AuditLogService auditLogService;

    public VehicleResponse createVehicle(String adminUserId, VehicleRequest request) {
        if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new DuplicateResourceException("A vehicle with this registration number already exists");
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));

        Vehicle vehicle = Vehicle.builder()
                .vehicleNumber(request.getVehicleNumber())
                .zoneId(zone.getId())
                .vehicleType(request.getVehicleType())
                .capacity(request.getCapacity())
                .active(true)
                .build();

        vehicle = vehicleRepository.save(vehicle);

        auditLogService.log(adminUserId, "VEHICLE_CREATED", "Vehicle", vehicle.getId(),
                "Registered vehicle " + vehicle.getVehicleNumber() + " in zone " + zone.getZoneName());

        return toResponse(vehicle, zone.getZoneName());
    }

    public VehicleResponse updateVehicle(String adminUserId, String vehicleId, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        if (!vehicle.getVehicleNumber().equals(request.getVehicleNumber())
                && vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new DuplicateResourceException("A vehicle with this registration number already exists");
        }

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + request.getZoneId()));

        vehicle.setVehicleNumber(request.getVehicleNumber());
        vehicle.setZoneId(zone.getId());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setCapacity(request.getCapacity());

        vehicle = vehicleRepository.save(vehicle);

        auditLogService.log(adminUserId, "VEHICLE_UPDATED", "Vehicle", vehicle.getId(),
                "Updated vehicle " + vehicle.getVehicleNumber());

        return toResponse(vehicle, zone.getZoneName());
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::toResponseWithZoneLookup)
                .toList();
    }

    public List<VehicleResponse> getVehiclesByZone(String zoneId) {
        return vehicleRepository.findByZoneId(zoneId).stream()
                .map(this::toResponseWithZoneLookup)
                .toList();
    }

    /**
     * Lists only active (roadworthy) vehicles within a zone — used when
     * assigning a vehicle to a worker, so vehicles under maintenance
     * don't appear as assignable options.
     */
    public List<VehicleResponse> getActiveVehiclesByZone(String zoneId) {
        return vehicleRepository.findByZoneIdAndActiveTrue(zoneId).stream()
                .map(this::toResponseWithZoneLookup)
                .toList();
    }

    public VehicleResponse getVehicleById(String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        return toResponseWithZoneLookup(vehicle);
    }

    /**
     * Marks a vehicle inactive (under repair) or active (back in service).
     * Kept separate from updateVehicle since maintenance status is an
     * operational toggle, not a general field edit — mirroring the same
     * "toggle is its own action" pattern used across Zone/User services.
     */
    public VehicleResponse setVehicleActiveStatus(String adminUserId, String vehicleId, boolean active) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicle.setActive(active);
        vehicle = vehicleRepository.save(vehicle);

        String action = active ? "VEHICLE_ACTIVATED" : "VEHICLE_DEACTIVATED";
        auditLogService.log(adminUserId, action, "Vehicle", vehicle.getId(),
                (active ? "Marked active: " : "Marked under maintenance: ") + vehicle.getVehicleNumber());

        return toResponseWithZoneLookup(vehicle);
    }

    /**
     * Records a maintenance event, timestamping it as now — used by a
     * dedicated "Log Maintenance" action on ManageVehicles.js, separate
     * from the general update flow (VehicleRequest deliberately excludes
     * lastMaintenanceDate, per its class-level note).
     */
    public VehicleResponse logMaintenance(String adminUserId, String vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicle.setLastMaintenanceDate(LocalDateTime.now());
        vehicle = vehicleRepository.save(vehicle);

        auditLogService.log(adminUserId, "VEHICLE_MAINTENANCE_LOGGED", "Vehicle", vehicle.getId(),
                "Logged maintenance for vehicle " + vehicle.getVehicleNumber());

        return toResponseWithZoneLookup(vehicle);
    }

    /**
     * Resolves the zone name for a vehicle via a fresh lookup — used by
     * list/detail methods that don't already have the Zone in hand
     * (unlike createVehicle/updateVehicle, which just validated it).
     */
    private VehicleResponse toResponseWithZoneLookup(Vehicle vehicle) {
        String zoneName = zoneRepository.findById(vehicle.getZoneId())
                .map(Zone::getZoneName)
                .orElse("Unknown Zone");
        return toResponse(vehicle, zoneName);
    }

    private VehicleResponse toResponse(Vehicle vehicle, String zoneName) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .zoneId(vehicle.getZoneId())
                .zoneName(zoneName)
                .vehicleType(vehicle.getVehicleType())
                .capacity(vehicle.getCapacity())
                .active(vehicle.isActive())
                .lastMaintenanceDate(vehicle.getLastMaintenanceDate())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}