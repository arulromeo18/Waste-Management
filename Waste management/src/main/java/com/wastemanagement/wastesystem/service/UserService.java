package com.wastemanagement.wastesystem.service;

import com.wastemanagement.wastesystem.dto.request.ProfileUpdateRequest;
import com.wastemanagement.wastesystem.dto.response.UserResponse;
import com.wastemanagement.wastesystem.exception.BadRequestException;
import com.wastemanagement.wastesystem.exception.DuplicateResourceException;
import com.wastemanagement.wastesystem.exception.ResourceNotFoundException;
import com.wastemanagement.wastesystem.model.Citizen;
import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.model.User;
import com.wastemanagement.wastesystem.model.Vehicle;
import com.wastemanagement.wastesystem.model.Worker;
import com.wastemanagement.wastesystem.model.Zone;
import com.wastemanagement.wastesystem.repository.CitizenRepository;
import com.wastemanagement.wastesystem.repository.UserRepository;
import com.wastemanagement.wastesystem.repository.VehicleRepository;
import com.wastemanagement.wastesystem.repository.WorkerRepository;
import com.wastemanagement.wastesystem.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Handles administrative user-management operations, complementing
 * AuthService (which only ever handles login and citizen self-registration).
 *
 * Worker provisioning lives here rather than in AuthService because it's
 * fundamentally a different operation: an authenticated Super Admin
 * creating an account on behalf of someone else, versus a public,
 * unauthenticated citizen signing themselves up.
 *
 * Every state-changing admin method here (createWorker, setUserActiveStatus,
 * assignWorkerToZoneAndVehicle) logs an entry via AuditLogService at the
 * end of the operation, attributed to the adminUserId threaded through
 * from the controller layer.
 *
 * getMyProfile/updateMyProfile serve the citizen's own self-service
 * Profile.js screen — no admin attribution needed there, since a citizen
 * acting on their own account isn't an "admin action" requiring an audit
 * trail entry.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CitizenRepository citizenRepository;
    private final WorkerRepository workerRepository;
    private final VehicleRepository vehicleRepository;
    private final ZoneRepository zoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    /**
     * Creates a new Worker account: the User (auth/identity) document plus
     * the Worker (profile) document, provisioned by the Super Admin.
     */
    @Transactional
    public UserResponse createWorker(String adminUserId, String fullName, String email, String rawPassword,
                                     String phone, String zoneId, String employeeId, String shiftTiming) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .phone(phone)
                .role(Role.WORKER)
                .active(true)
                .build();
        user = userRepository.save(user);

        Worker worker = Worker.builder()
                .userId(user.getId())
                .zoneId(zoneId)
                .fullName(fullName)
                .phoneNumber(phone)
                .employeeId(employeeId)
                .shiftTiming(shiftTiming)
                .active(true)
                .build();
        workerRepository.save(worker);

        auditLogService.log(adminUserId, "WORKER_CREATED", "User", user.getId(),
                "Created worker account for " + fullName + " (" + email + ")");

        return buildUserResponse(user);
    }

    /**
     * Lists all users of a given role, enriched into UserResponse DTOs.
     */
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::buildUserResponse)
                .toList();
    }

    /**
     * Fetches a single user by id, enriched into a UserResponse.
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return buildUserResponse(user);
    }

    /**
     * Retrieves the authenticated citizen's own profile — backs
     * GET /api/citizen/profile (Profile.js). Always called with the
     * caller's own userId (resolved from the JWT principal by the
     * controller), never an arbitrary id.
     */
    public UserResponse getMyProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return buildUserResponse(user);
    }

    /**
     * Updates the authenticated citizen's own editable profile fields —
     * backs PUT /api/citizen/profile. Per ProfileUpdateRequest's own
     * class-level note, fullName/phone live on User while the address
     * fields live on Citizen, so this method updates both documents for
     * the same citizen in one transactional operation.
     */
    @Transactional
    public UserResponse updateCitizenProfile(String userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        Citizen citizen = citizenRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen profile not found for this account"));

        citizen.setAddress(request.getAddress());
        citizen.setHouseNumber(request.getHouseNumber());
        citizen.setLandmark(request.getLandmark());
        citizen.setPincode(request.getPincode());
        citizenRepository.save(citizen);

        return buildUserResponse(user);
    }

    /**
     * Activates or deactivates a user account.
     */
    public UserResponse setUserActiveStatus(String adminUserId, String userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(active);
        userRepository.save(user);

        if (user.getRole() == Role.WORKER) {
            workerRepository.findByUserId(userId).ifPresent(worker -> {
                worker.setActive(active);
                workerRepository.save(worker);
            });
        }

        String action = active ? "USER_ACTIVATED" : "USER_DEACTIVATED";
        auditLogService.log(adminUserId, action, "User", user.getId(),
                (active ? "Activated" : "Deactivated") + " account for " + user.getFullName());

        return buildUserResponse(user);
    }

    /**
     * Assigns a worker to a zone and, optionally, a vehicle.
     *
     * IMPORTANT: userId here is the worker's own User._id (the id shown
     * throughout ManageWorkers.js) — NOT the separate Worker profile
     * document's own _id. Resolved internally via
     * workerRepository.findByUserId(userId).
     */
    @Transactional
    public UserResponse assignWorkerToZoneAndVehicle(String adminUserId, String userId,
                                                     String zoneId, String vehicleId) {
        Worker worker = workerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found for user id: " + userId));

        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + zoneId));

        if (vehicleId != null && !vehicleId.isBlank()) {
            Vehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

            if (!vehicle.getZoneId().equals(zone.getId())) {
                throw new BadRequestException("This vehicle does not belong to the zone being assigned");
            }

            Optional<Worker> currentHolder = workerRepository.findByVehicleId(vehicleId);
            if (currentHolder.isPresent() && !currentHolder.get().getId().equals(worker.getId())) {
                Worker previousHolder = currentHolder.get();
                previousHolder.setVehicleId(null);
                workerRepository.save(previousHolder);
            }

            worker.setVehicleId(vehicleId);
        } else {
            worker.setVehicleId(null);
        }

        worker.setZoneId(zone.getId());
        workerRepository.save(worker);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        auditLogService.log(adminUserId, "WORKER_ASSIGNED", "Worker", worker.getId(),
                "Assigned " + user.getFullName() + " to zone " + zone.getZoneName()
                        + (vehicleId != null ? " with vehicle " + vehicleId : " with no vehicle"));

        return buildUserResponse(user);
    }

    /**
     * Enriches a User document into a UserResponse, populating
     * role-specific fields by looking up the corresponding profile document.
     */
    private UserResponse buildUserResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt());

        if (user.getRole() == Role.CITIZEN) {
            Optional<Citizen> citizen = citizenRepository.findByUserId(user.getId());
            citizen.ifPresent(c -> builder
                    .zoneId(c.getZoneId())
                    .rewardPoints(c.getRewardPoints())
                    .totalComplaintsFiled(c.getTotalComplaintsFiled()));
        } else if (user.getRole() == Role.WORKER) {
            Optional<Worker> worker = workerRepository.findByUserId(user.getId());
            worker.ifPresent(w -> builder
                    .zoneId(w.getZoneId())
                    .vehicleId(w.getVehicleId())
                    .employeeId(w.getEmployeeId())
                    .shiftTiming(w.getShiftTiming())
                    .totalCollectionsLogged(w.getTotalCollectionsLogged()));
        }

        return builder.build();
    }
}