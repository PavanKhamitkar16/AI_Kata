package com.hospital.patientmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper used by every endpoint.
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "data": { ... },           // null fields are excluded from JSON
 *   "timestamp": "2026-09-01T10:00:00"
 * }
 * </pre>
 *
 * @param <T> type of the payload carried in {@code data}
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean       success;
    private final String        message;
    private final T             data;
    private final LocalDateTime timestamp;

    // ------------------------------------------------------------------ //
    //  Factory helpers
    // ------------------------------------------------------------------ //

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
