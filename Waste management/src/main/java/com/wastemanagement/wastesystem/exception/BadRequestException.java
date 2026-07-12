package com.wastemanagement.wastesystem.exception;

/**
 * Thrown when a request is structurally valid (passes all Bean Validation
 * annotations like @NotBlank/@Email) but violates a business rule or is
 * invalid given the current state of the data.
 *
 * Examples already in use across this codebase:
 *   - PenaltyService.waivePenalty(): waiving a penalty that isn't PENDING
 *   - PenaltyService.settlePenalty(): settling a penalty that isn't PENDING
 *   - (upcoming) AuthService.register(): registering an email that already exists
 *
 * This is deliberately kept separate from Bean Validation failures.
 * Field-level validation (is the email well-formed, is the name blank) is
 * handled automatically by Spring via @Valid + MethodArgumentNotValidException,
 * before a request even reaches service-layer code. BadRequestException
 * covers everything Bean Validation annotations structurally cannot express —
 * rules that depend on looking up existing data or checking current state
 * (e.g. "is this the right status for this transition", "does this email
 * already belong to another account").
 *
 * Like ResourceNotFoundException, this is unchecked so it doesn't force
 * "throws" declarations across every service method that might encounter
 * an invalid business state — it bubbles straight up to
 * GlobalExceptionHandler.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}