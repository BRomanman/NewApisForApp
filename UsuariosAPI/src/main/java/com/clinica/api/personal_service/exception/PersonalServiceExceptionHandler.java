package com.clinica.api.personal_service.exception;

import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackages = "com.clinica.api.personal_service.controller")
public class PersonalServiceExceptionHandler {

    private static final String DEFAULT_BUSINESS_CODE = "BUSINESS_RULE_VIOLATION";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String message = extractMessage(ex);
        String codigo = resolveBusinessCode(message);
        return ResponseEntity.status(ex.getStatusCode())
            .body(new ApiErrorResponse(codigo, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Solicitud inválida";
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse(resolveBusinessCode(message), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Solicitud inválida";
        }
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("ENTITY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("DATA_INTEGRITY_VIOLATION", message));
    }

    private String extractMessage(ResponseStatusException ex) {
        if (ex.getReason() != null && !ex.getReason().isBlank()) {
            return ex.getReason();
        }
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage();
        }
        return ex.getStatusCode().getReasonPhrase();
    }

    private String resolveBusinessCode(String message) {
        if (message == null) {
            return DEFAULT_BUSINESS_CODE;
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("correo") && normalized.contains("ya existe")) {
            return "EMAIL_ALREADY_EXISTS";
        }
        if (normalized.contains("correo") && normalized.contains("requerido")) {
            return "EMAIL_REQUIRED";
        }
        if (normalized.contains("especialidad")) {
            return "INVALID_SPECIALTY";
        }
        if (normalized.contains("rol doctor")) {
            return "DOCTOR_ROLE_NOT_CONFIGURED";
        }
        if (normalized.contains("fechanacimiento") || normalized.contains("fecha de nacimiento")) {
            return "BIRTHDATE_ERROR";
        }
        return DEFAULT_BUSINESS_CODE;
    }
}
