package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record SensorReadingRequest(
        @NotNull(message = "Ativo e obrigatorio") Long assetId,
        @NotBlank(message = "Tipo de sensor e obrigatorio") String sensorType,
        @NotNull(message = "Valor e obrigatorio") BigDecimal value,
        String unit,
        Instant recordedAt
) {
}
