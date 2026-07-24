package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.MaintenancePlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaintenancePlanRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,
        Long assetId,
        Long assetTypeId,
        Long checklistTemplateId,
        @NotNull(message = "Tipo de frequencia e obrigatorio") MaintenancePlan.FrequencyType frequencyType,
        @Positive(message = "Valor de frequencia deve ser positivo") int frequencyValue,
        String frequencyUnit,
        boolean active
) {
}
