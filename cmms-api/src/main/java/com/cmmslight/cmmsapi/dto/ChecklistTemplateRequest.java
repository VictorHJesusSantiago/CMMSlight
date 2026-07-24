package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

public record ChecklistTemplateRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,
        String description
) {
}
