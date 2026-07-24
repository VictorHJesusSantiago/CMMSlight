package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotNull;

public record WorkOrderAssignRequest(
        @NotNull(message = "Tecnico e obrigatorio") Long assignedToId,
        Long changedByUserId
) {
}
