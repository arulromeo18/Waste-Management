package com.wastemanagement.wastesystem.exception;

import com.wastemanagement.wastesystem.dto.response.ApiResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catches every exception thrown across all @RestController classes in the
 * application and converts it into a consistent ApiResponse JSON envelope
 * with an appropriate HTTP status code.
 *
 * Without this class, an unhandled exception would either leak a raw stack
 * trace (in dev) or Spring Boot's generic Whitelabel Error Page (in prod) —
 * neither of which the React frontend's Axios interceptor could reliably
 * parse. Every handler below guarantees the same { success, message, data }
 * shape (see ApiResponse.java), so the frontend only ever needs one error-
 * handling code path regardless of which exception occurred.
 *
 * Handlers are ordered from most specific to most generic, matching how
 * Spring resolves @ExceptionHandler methods (most specific applicable type
 * wins, but explicit ordering here aids readability).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles missing resources — e.g. penaltyRepository.findById(id) not
     * found. Maps to 404, since the requested thing genuinely doesn't exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles business-rule violations — e.g. waiving an already-settled
     * penalty. Maps to 400, since the request itself was well-formed but
     * invalid given the current state of the data.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles proactive uniqueness-rule violations — e.g.
     * AuthService.registerCitizen() rejecting a registration attempt for an
     * email that already exists. Maps to 409 Conflict, the RESTfully
     * correct status for "this resource already exists" (distinct from the
     * more general 400 used for other business-rule violations above).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles Bean Validation failures on @Valid-annotated request bodies
     * (e.g. RegisterRequest.email failing @Email, ZoneRequest.zoneName
     * failing @NotBlank). Rather than returning just the first error's
     * generic message, this collects every failing field into a map so the
     * React frontend can highlight each invalid field individually in one
     * round trip, instead of the user having to fix and resubmit repeatedly.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message("Validation failed")
                .data(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles MongoDB's own unique-index violation as a last line of
     * defense — every current unique field (User.email, Zone.zoneCode,
     * Citizen.userId, etc.) is already guarded with an existsBy... check
     * in its respective service BEFORE insertion (e.g.
     * DuplicateResourceException above), so this should rarely fire in
     * practice. It exists purely to prevent a race condition (two
     * simultaneous requests both passing the existsBy... check before
     * either has saved) from surfacing as a raw 500 error instead of a
     * clean 409 Conflict.
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateKey(DuplicateKeyException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("A record with this unique value already exists"));
    }

    /**
     * Handles invalid login credentials specifically — thrown by Spring
     * Security's AuthenticationManager during AuthService.login() when the
     * email/password combination doesn't match. Deliberately returns a
     * generic message (not "email not found" vs "wrong password"
     * separately) to avoid leaking which part of the credential pair was
     * incorrect — a standard security practice that prevents email
     * enumeration attacks.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
    }

    /**
     * Handles login attempts against a deactivated account — thrown by
     * Spring Security's DaoAuthenticationProvider when
     * SecurityUserPrincipal.isEnabled() returns false (see
     * UserService.setUserActiveStatus() for how an account becomes
     * deactivated). Maps to 403, distinct from the 401 used for wrong
     * credentials — the password may well be correct here, but the
     * account itself has been suspended by a Super Admin.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledAccount(DisabledException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("This account has been deactivated. Please contact the administrator."));
    }

    /**
     * Handles authorization failures — e.g. a CITIZEN-role JWT hitting an
     * endpoint restricted via @PreAuthorize("hasRole('SUPER_ADMIN')").
     * Maps to 403, distinct from 401 (unauthenticated) since the user IS
     * authenticated here, just not permitted to perform this action.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You do not have permission to perform this action"));
    }

    /**
     * Catch-all fallback for any exception type not explicitly handled
     * above (e.g. a NullPointerException from a genuine bug, an unexpected
     * MongoDB driver exception). Deliberately returns a generic message
     * rather than ex.getMessage() — an uncaught exception's raw message
     * could leak internal implementation details (class names, field
     * names, stack info) to the client, which is a security concern in a
     * production system.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}