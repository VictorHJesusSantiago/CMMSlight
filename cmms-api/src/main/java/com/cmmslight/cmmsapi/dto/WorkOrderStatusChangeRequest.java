package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.WorkOrder;
import jakarta.validation.constraints.NotNull;

public record WorkOrderStatusChangeRequest(
        @NotNull(message = "Novo status e obrigatorio") WorkOrder.Status newStatus,
        Long changedByUserId,
        String comment
) {
}
