package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.WorkOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record WorkOrderRequest(
        @NotBlank(message = "Codigo e obrigatorio") String code,
        @NotNull(message = "Ativo e obrigatorio") Long assetId,
        Long maintenancePlanId,
        @NotNull(message = "Tipo e obrigatorio") WorkOrder.Type type,
        @NotNull(message = "Prioridade e obrigatoria") WorkOrder.Priority priority,
        @NotBlank(message = "Titulo e obrigatorio") String title,
        String description,
        Long requestedById,
        Long assignedToId,
        Instant scheduledAt
) {
}
