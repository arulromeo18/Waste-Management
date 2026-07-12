package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Citizen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "citizens" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed to resolve a citizen's profile
 * from their authenticated userId, list citizens per zone, and support
 * reward-point-based sorting for leaderboard/analytics features.
 */
@Repository
public interface CitizenRepository extends MongoRepository<Citizen, String> {

    /**
     * Resolves a Citizen's profile from the authenticated User's id.
     * Used constantly in controllers via @AuthenticationPrincipal
     * SecurityUserPrincipal -> principal.getUserId() -> this lookup,
     * to load the full citizen profile (address, zone, reward points)
     * for the currently logged-in user.
     */
    Optional<Citizen> findByUserId(String userId);

    /**
     * Used during registration to guard against creating a duplicate
     * Citizen profile for a User that already has one.
     */
    boolean existsByUserId(String userId);

    /**
     * Lists all citizens within a given zone — used by Super Admin's
     * "Manage Citizens" screen (zone filter) and when broadcasting
     * zone-scoped announcements or collection schedule changes.
     */
    List<Citizen> findByZoneId(String zoneId);

    /**
     * Returns citizens sorted by reward points, descending, for the
     * Rewards System leaderboard feature. Pagination is handled by the
     * caller (service/controller layer) via Spring Data's Pageable overload
     * if needed later; kept simple here per Rule 18.
     */
    List<Citizen> findByZoneIdOrderByRewardPointsDesc(String zoneId);
}