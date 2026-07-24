package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SensorTrendResponse(
        Long assetId,
        String sensorType,
        List<Point> points,
        BigDecimal average,
        BigDecimal standardDeviation,
        List<Point> anomalies,
        BigDecimal trendSlopePerHour,
        Instant estimatedThresholdBreachAt
) {
    public record Point(Instant recordedAt, BigDecimal value) {
    }
}
