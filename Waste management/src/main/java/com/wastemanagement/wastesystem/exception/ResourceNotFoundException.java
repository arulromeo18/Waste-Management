package com.wastemanagement.wastesystem.exception;

/**
 * Thrown when a requested resource (by id, email, code, or any other lookup
 * key) does not exist in the database.
 *
 * This is a plain unchecked exception rather than a checked one — Spring
 * Boot service methods are already full of database calls that could fail
 * for this reason, and forcing every caller up the chain to declare
 * "throws ResourceNotFoundException" would add noise without adding safety
 * (the caller can't meaningfully "handle" a missing resource inline; it
 * needs to bubble all the way up to GlobalExceptionHandler regardless).
 *
 * Used consistently across every service in this codebase, e.g.:
 *   penaltyRepository.findById(penaltyId)
 *       .orElseThrow(() -> new ResourceNotFoundException("Penalty not found with id: " + penaltyId));
 *
 * GlobalExceptionHandler (next file) catches this exception type globally
 * and translates it into a 404 NOT_FOUND HTTP response, so individual
 * services/controllers never need to manually set a status code for this
 * common case.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}