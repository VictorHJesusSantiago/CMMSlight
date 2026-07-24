package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotNull;

public record ChecklistAnswerRequest(
        @NotNull(message = "Item de checklist e obrigatorio") Long checklistItemId,
        String value,
        String notes
) {
}
