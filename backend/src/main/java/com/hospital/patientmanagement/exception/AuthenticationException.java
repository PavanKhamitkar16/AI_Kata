package com.hospital.patientmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when authentication fails (invalid credentials, locked account, etc.).
 * Maps to HTTP 401 Unauthorized via {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
