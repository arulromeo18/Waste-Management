package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.Role;
import com.wastemanagement.wastesystem.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "users" collection — the single source of truth for
 * authentication across all three roles (Super Admin, Worker, Citizen).
 *
 * Extends MongoRepository to inherit standard CRUD operations (save, findById,
 * findAll, deleteById, etc.) and declares additional query-derived methods
 * needed by the authentication flow and admin management screens.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Looks up a user by email — the unique "username" used throughout the
     * system. Used by CustomUserDetailsService during login and JWT validation.
     */
    Optional<User> findByEmail(String email);

    /**
     * Used during registration to reject duplicate sign-ups before hitting
     * the unique index constraint at the database level (gives a clean
     * validation error instead of a raw MongoDB duplicate-key exception).
     */
    boolean existsByEmail(String email);

    /**
     * Used by Super Admin's "Manage Citizens" / "Manage Workers" screens to
     * list users filtered by role, with pagination handled by the caller.
     */
    List<User> findByRole(Role role);

    /**
     * Used to filter active vs suspended accounts within a role — e.g.
     * listing only currently-active sanitation workers when assigning routes.
     */
    List<User> findByRoleAndActive(Role role, boolean active);
}