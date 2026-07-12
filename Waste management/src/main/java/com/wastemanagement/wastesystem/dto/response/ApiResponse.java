package com.wastemanagement.wastesystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response envelope used across the API for simple success/error
 * acknowledgements and for wrapping arbitrary response payloads that don't
 * warrant their own dedicated response DTO (e.g. "Zone created
 * successfully", "Complaint status updated", a delete confirmation).
 *
 * Specialized responses (AuthResponse, ComplaintResponse,
 * DashboardStatsResponse, etc.) are still used wherever the frontend needs
 * a well-defined, strongly-typed shape — this generic wrapper is reserved
 * for simple acknowledgement-style endpoints and for GlobalExceptionHandler
 * (upcoming) to return a consistent error shape across the entire API,
 * so the frontend's Axios interceptor can handle both success and error
 * responses with one predictable envelope structure.
 *
 * The generic type parameter T lets this same class wrap a single object,
 * a list, a plain string, or nothing at all (data omitted from the JSON
 * entirely, via @JsonInclude, rather than serialized as a null field).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * Convenience factory for a successful response carrying a data
     * payload — avoids repeating .builder().success(true)... at every
     * call site across every controller.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Convenience factory for a successful response with no data payload
     * (e.g. a delete or deactivate acknowledgement).
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Convenience factory for an error response — used by
     * GlobalExceptionHandler (upcoming) to build a consistent error shape
     * for every exception type it handles.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}