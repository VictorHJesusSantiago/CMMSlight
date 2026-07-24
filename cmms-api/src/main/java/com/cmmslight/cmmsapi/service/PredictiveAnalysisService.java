package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.SensorReading;
import com.cmmslight.cmmsapi.domain.SensorThresholdRule;
import com.cmmslight.cmmsapi.dto.SensorTrendResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.SensorReadingRepository;
import com.cmmslight.cmmsapi.repository.SensorThresholdRuleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Analise preditiva simples baseada em estatistica classica (media, desvio padrao,
 * regressao linear), sem dependencia de servicos de ML em nuvem.
 */
@Service
public class PredictiveAnalysisService {

    private final SensorReadingRepository sensorReadingRepository;
    private final AssetRepository assetRepository;
    private final SensorThresholdRuleRepository thresholdRuleRepository;

    public PredictiveAnalysisService(SensorReadingRepository sensorReadingRepository,
                                      AssetRepository assetRepository,
                                      SensorThresholdRuleRepository thresholdRuleRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.assetRepository = assetRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
    }

    public SensorTrendResponse trend(Long assetId, String sensorType) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + assetId));

        List<SensorReading> readings = sensorReadingRepository.findByAssetIdAndSensorTypeOrderByRecordedAtAsc(assetId, sensorType);
        List<SensorTrendResponse.Point> points = readings.stream()
                .map(r -> new SensorTrendResponse.Point(r.getRecordedAt(), r.getValue()))
                .toList();

        if (readings.isEmpty()) {
            return new SensorTrendResponse(assetId, sensorType, points, null, null, List.of(), null, null);
        }

        double mean = readings.stream().mapToDouble(r -> r.getValue().doubleValue()).average().orElse(0);
        double variance = readings.stream()
                .mapToDouble(r -> Math.pow(r.getValue().doubleValue() - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        List<SensorTrendResponse.Point> anomalies = new ArrayList<>();
        for (SensorReading r : readings) {
            if (Math.abs(r.getValue().doubleValue() - mean) > 2 * stdDev) {
                anomalies.add(new SensorTrendResponse.Point(r.getRecordedAt(), r.getValue()));
            }
        }

        Double slopePerHour = linearRegressionSlope(readings);
        Instant estimatedBreach = estimateThresholdBreach(asset, sensorType, readings, slopePerHour);

        return new SensorTrendResponse(
                assetId,
                sensorType,
                points,
                BigDecimal.valueOf(mean).setScale(4, RoundingMode.HALF_UP),
                BigDecimal.valueOf(stdDev).setScale(4, RoundingMode.HALF_UP),
                anomalies,
                slopePerHour != null ? BigDecimal.valueOf(slopePerHour).setScale(6, RoundingMode.HALF_UP) : null,
                estimatedBreach
        );
    }

    /** Regressao linear simples (minimos quadrados) de valor em funcao do tempo em horas. */
    private Double linearRegressionSlope(List<SensorReading> readings) {
        if (readings.size() < 2) {
            return null;
        }
        Instant t0 = readings.get(0).getRecordedAt();
        double n = readings.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (SensorReading r : readings) {
            double x = ChronoUnit.MINUTES.between(t0, r.getRecordedAt()) / 60.0;
            double y = r.getValue().doubleValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        double denominator = (n * sumXX - sumX * sumX);
        if (denominator == 0) {
            return null;
        }
        return (n * sumXY - sumX * sumY) / denominator;
    }

    private Instant estimateThresholdBreach(Asset asset, String sensorType, List<SensorReading> readings, Double slopePerHour) {
        if (slopePerHour == null || slopePerHour == 0) {
            return null;
        }
        List<SensorThresholdRule> rules = new ArrayList<>();
        rules.addAll(thresholdRuleRepository.findByAssetIdAndSensorTypeAndActiveTrue(asset.getId(), sensorType));
        if (asset.getAssetType() != null) {
            rules.addAll(thresholdRuleRepository.findByAssetTypeIdAndSensorTypeAndActiveTrue(asset.getAssetType().getId(), sensorType));
        }
        if (rules.isEmpty()) {
            return null;
        }

        SensorReading last = readings.get(readings.size() - 1);
        double currentValue = last.getValue().doubleValue();

        Double closestHours = null;
        for (SensorThresholdRule rule : rules) {
            if (slopePerHour > 0 && rule.getMaxValue() != null) {
                double hours = (rule.getMaxValue().doubleValue() - currentValue) / slopePerHour;
                if (hours > 0 && (closestHours == null || hours < closestHours)) {
                    closestHours = hours;
                }
            }
            if (slopePerHour < 0 && rule.getMinValue() != null) {
                double hours = (rule.getMinValue().doubleValue() - currentValue) / slopePerHour;
                if (hours > 0 && (closestHours == null || hours < closestHours)) {
                    closestHours = hours;
                }
            }
        }
        if (closestHours == null) {
            return null;
        }
        return last.getRecordedAt().plus(Math.round(closestHours * 60), ChronoUnit.MINUTES);
    }
}
