package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.AuditLog;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String entityName,
        Long entityId,
        AuditLog.Action action,
        Long performedByUserId,
        String performedByUserName,
        Instant performedAt,
        String details
) {
}
