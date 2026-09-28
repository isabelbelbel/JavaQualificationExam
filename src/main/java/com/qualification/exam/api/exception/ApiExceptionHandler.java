package com.qualification.exam.api.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.qualification.exam.exception.CircularDependencyException;
import com.qualification.exam.exception.InvalidProjectPlanException;
import com.qualification.exam.exception.ResourceNotFoundException;
import com.qualification.exam.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(
        basePackages =
                "com.qualification.exam.api.controller"
)
public class ApiExceptionHandler {

    private final AuditLogService auditLogService;

    public ApiExceptionHandler(
            AuditLogService auditLogService
    ) {
        this.auditLogService = auditLogService;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        record(exception, request);

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(CircularDependencyException.class)
    public ResponseEntity<Map<String, Object>>
    handleCircularDependency(
            CircularDependencyException exception,
            HttpServletRequest request
    ) {
        record(exception, request);

        Map<String, Object> body = createBody(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI()
        );

        body.put(
                "affectedTaskKeys",
                exception.getAffectedTaskKeys()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(InvalidProjectPlanException.class)
    public ResponseEntity<Map<String, Object>>
    handleInvalidProjectPlan(
            InvalidProjectPlanException exception,
            HttpServletRequest request
    ) {
        record(exception, request);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        record(exception, request);

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = createBody(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request.getRequestURI()
        );

        body.put("fieldErrors", fieldErrors);

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>>
    handleUnreadableJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        record(exception, request);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request body contains invalid JSON",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        record(exception, request);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "The application could not complete the request",
                request.getRequestURI()
        );
    }

    private void record(
            Exception exception,
            HttpServletRequest request
    ) {
        auditLogService.recordException(
                exception,
                request.getMethod(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        return ResponseEntity
                .status(status)
                .body(createBody(status, message, path));
    }

    private Map<String, Object> createBody(
            HttpStatus status,
            String message,
            String path
    ) {
        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        return body;
    }
}