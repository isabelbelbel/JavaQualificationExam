package com.qualification.exam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger AUDIT_LOGGER =
            LoggerFactory.getLogger("AUDIT");

    public void record(
            String action,
            Long projectId,
            Long taskId,
            String description
    ) {
        AUDIT_LOGGER.info(
                "status=SUCCESS | action={} | projectId={} "
                        + "| taskId={} | description={}",
                sanitize(action),
                projectId,
                taskId,
                sanitize(description)
        );
    }

    public void recordFailure(
            String action,
            Long projectId,
            Long taskId,
            String exceptionType,
            String description
    ) {
        AUDIT_LOGGER.error(
                "status=FAILED | action={} | projectId={} "
                        + "| taskId={} | exception={} "
                        + "| description={}",
                sanitize(action),
                projectId,
                taskId,
                sanitize(exceptionType),
                sanitize(description)
        );
    }

    public void recordException(
            Throwable throwable,
            String httpMethod,
            String requestPath
    ) {
        AUDIT_LOGGER.error(
                "status=FAILED | action=HTTP_REQUEST "
                        + "| method={} | path={} "
                        + "| exception={} | description={}",
                sanitize(httpMethod),
                sanitize(requestPath),
                throwable.getClass().getSimpleName(),
                sanitize(throwable.getMessage())
        );
    }

    public void recordAuthenticationFailure(
            String httpMethod,
            String requestPath,
            String description
    ) {
        AUDIT_LOGGER.warn(
                "status=FAILED | action=AUTHENTICATION "
                        + "| method={} | path={} "
                        + "| description={}",
                sanitize(httpMethod),
                sanitize(requestPath),
                sanitize(description)
        );
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }

        return value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('|', '/');
    }
}