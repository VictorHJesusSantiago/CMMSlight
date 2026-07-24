package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SensorThresholdRuleRequest(
        Long assetId,
        Long assetTypeId,
        @NotBlank(message = "Tipo de sensor e obrigatorio") String sensorType,
        BigDecimal minValue,
        BigDecimal maxValue,
        boolean active
) {
}
