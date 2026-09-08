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
                "action={} | projectId={} | taskId={} | description={}",
                sanitize(action),
                projectId,
                taskId,
                sanitize(description)
        );
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('|', '/');
    }
}