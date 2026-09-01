package com.hospital.patientmanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structured error payload returned by the
 * {@link GlobalExceptionHandler} for all error responses.
 *
 * <pre>
 * {
 *   "status":  401,
 *   "error":   "Unauthorized",
 *   "message": "Invalid credentials",
 *   "path":    "/api/auth/login",
 *   "timestamp": "...",
 *   "fieldErrors": { "username": "Username is required" }  // validation only
 * }
 * </pre>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int                 status;
    private final String              error;
    private final String              message;
    private final String              path;
    private final LocalDateTime       timestamp;
    private final Map<String, String> fieldErrors; // populated for 400 validation errors
}
