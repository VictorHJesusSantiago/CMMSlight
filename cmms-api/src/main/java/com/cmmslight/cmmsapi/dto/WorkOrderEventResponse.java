package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.WorkOrderEvent;

import java.time.Instant;

public record WorkOrderEventResponse(
        Long id,
        Long workOrderId,
        WorkOrderEvent.EventType eventType,
        String message,
        Long createdByUserId,
        String createdByUserName,
        Instant createdAt
) {
}
