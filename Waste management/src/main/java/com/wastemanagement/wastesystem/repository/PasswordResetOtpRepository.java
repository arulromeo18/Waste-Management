package com.wastemanagement.wastesystem.repository;

import com.wastemanagement.wastesystem.model.PasswordResetOtp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the "password_reset_otps" collection.
 *
 * Extends MongoRepository to inherit standard CRUD operations and declares
 * additional query-derived methods needed for the Forgot Password / Reset
 * Password flow in AuthService (upcoming).
 */
@Repository
public interface PasswordResetOtpRepository extends MongoRepository<PasswordResetOtp, String> {

    /**
     * Finds the most recently issued, not-yet-used OTP for an email —
     * used during ResetPassword.js verification to check the code the
     * user submitted against the latest outstanding request, ignoring
     * any older/already-used OTPs from previous requests.
     */
    Optional<PasswordResetOtp> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    /**
     * Lists all OTP records for an email — used when invalidating every
     * outstanding OTP for a user (e.g. once one is successfully used, or
     * if a new request should supersede all previous ones).
     */
    List<PasswordResetOtp> findByEmail(String email);
}