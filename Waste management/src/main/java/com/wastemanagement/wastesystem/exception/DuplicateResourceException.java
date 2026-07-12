package com.wastemanagement.wastesystem.exception;

/**
 * Thrown when an operation would violate a known uniqueness constraint —
 * e.g. AuthService.registerCitizen() rejecting a registration attempt for
 * an email that already exists (checked proactively via
 * userRepository.existsByEmail(...) before any insert is attempted).
 *
 * Kept distinct from BadRequestException even though both represent
 * "the request was well-formed but invalid": a duplicate-resource conflict
 * maps to HTTP 409 Conflict, which is the more precise and RESTfully
 * correct status code for "this resource already exists" versus the more
 * general 400 Bad Request used for other business-rule violations (e.g.
 * PenaltyService.waivePenalty() on a non-pending penalty). Giving this its
 * own type lets GlobalExceptionHandler map each to the right status code
 * without inspecting the exception's message string.
 *
 * This is the proactive, application-level counterpart to
 * DuplicateKeyException (already handled in GlobalExceptionHandler) which
 * only catches the rare race-condition case that slips past an
 * existsBy... check at the database level. In normal operation, this
 * exception is what actually fires for duplicate-email registration
 * attempts; DuplicateKeyException is the defense-in-depth fallback.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}