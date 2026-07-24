package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.MaintenancePlan;

import java.time.Instant;

public record MaintenancePlanResponse(
        Long id,
        String name,
        Long assetId,
        String assetName,
        Long assetTypeId,
        String assetTypeName,
        Long checklistTemplateId,
        MaintenancePlan.FrequencyType frequencyType,
        int frequencyValue,
        String frequencyUnit,
        boolean active,
        Instant lastGeneratedAt,
        Instant nextDueAt,
        boolean overdue
) {
}
