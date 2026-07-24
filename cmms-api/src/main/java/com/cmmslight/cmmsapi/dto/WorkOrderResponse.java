package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.WorkOrder;

import java.time.Instant;

public record WorkOrderResponse(
        Long id,
        String code,
        Long assetId,
        String assetName,
        Long maintenancePlanId,
        WorkOrder.Type type,
        WorkOrder.Status status,
        WorkOrder.Priority priority,
        String title,
        String description,
        Long requestedById,
        String requestedByName,
        Long assignedToId,
        String assignedToName,
        Instant openedAt,
        Instant scheduledAt,
        Instant startedAt,
        Instant completedAt,
        Long executionMinutes,
        String signedByName,
        Instant signedAt,
        Long reopenedFromId
) {
}
