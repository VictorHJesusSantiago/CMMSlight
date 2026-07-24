package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SensorReadingResponse(
        Long id,
        Long assetId,
        String sensorType,
        BigDecimal value,
        String unit,
        Instant recordedAt
) {
}
