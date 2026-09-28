package com.qualification.exam.config;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.qualification.exam.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final AuditLogService auditLogService;
    private final JsonMapper jsonMapper;

    public ApiAuthenticationEntryPoint(
            AuditLogService auditLogService,
            JsonMapper jsonMapper
    ) {
        this.auditLogService = auditLogService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        auditLogService.recordAuthenticationFailure(
                request.getMethod(),
                request.getRequestURI(),
                "Authentication is required"
        );

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        ErrorBody body = new ErrorBody(
                Instant.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                "Valid API credentials are required",
                request.getRequestURI()
        );

        jsonMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }

    private record ErrorBody(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}