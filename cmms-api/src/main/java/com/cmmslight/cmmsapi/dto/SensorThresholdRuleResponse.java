package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;

public record SensorThresholdRuleResponse(
        Long id,
        Long assetId,
        Long assetTypeId,
        String sensorType,
        BigDecimal minValue,
        BigDecimal maxValue,
        boolean active
) {
}
