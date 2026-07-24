package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.SensorAlert;
import com.cmmslight.cmmsapi.domain.SensorReading;
import com.cmmslight.cmmsapi.domain.SensorThresholdRule;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.dto.SensorReadingRequest;
import com.cmmslight.cmmsapi.dto.SensorReadingResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.SensorAlertRepository;
import com.cmmslight.cmmsapi.repository.SensorReadingRepository;
import com.cmmslight.cmmsapi.repository.SensorThresholdRuleRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final AssetRepository assetRepository;
    private final SensorThresholdRuleRepository thresholdRuleRepository;
    private final SensorAlertRepository sensorAlertRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderEventService eventService;

    public SensorReadingService(SensorReadingRepository sensorReadingRepository,
                                 AssetRepository assetRepository,
                                 SensorThresholdRuleRepository thresholdRuleRepository,
                                 SensorAlertRepository sensorAlertRepository,
                                 WorkOrderRepository workOrderRepository,
                                 WorkOrderEventService eventService) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.assetRepository = assetRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.sensorAlertRepository = sensorAlertRepository;
        this.workOrderRepository = workOrderRepository;
        this.eventService = eventService;
    }

    public List<SensorReadingResponse> findByAsset(Long assetId) {
        return sensorReadingRepository.findByAssetIdOrderByRecordedAtDesc(assetId).stream()
                .map(this::toResponse)
                .toList();
    }

    public SensorReadingResponse create(SensorReadingRequest request) {
        SensorReading reading = save(request);
        return toResponse(reading);
    }

    /** Importacao manual de leituras via CSV: colunas sensorType,value,unit,recordedAt(ISO-8601). */
    public int importCsv(Long assetId, MultipartFile file) {
        assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + assetId));

        int imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (firstLine && line.toLowerCase().startsWith("sensortype")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    throw new ValidationException("Linha CSV invalida: " + line);
                }
                String sensorType = parts[0].trim();
                BigDecimal value = new BigDecimal(parts[1].trim());
                String unit = parts.length > 2 && !parts[2].isBlank() ? parts[2].trim() : null;
                Instant recordedAt = parts.length > 3 && !parts[3].isBlank() ? Instant.parse(parts[3].trim()) : Instant.now();

                save(new SensorReadingRequest(assetId, sensorType, value, unit, recordedAt));
                imported++;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler arquivo CSV", e);
        } catch (NumberFormatException e) {
            throw new ValidationException("Valor numerico invalido no CSV: " + e.getMessage());
        }
        return imported;
    }

    private SensorReading save(SensorReadingRequest request) {
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + request.assetId()));

        SensorReading reading = new SensorReading();
        reading.setAsset(asset);
        reading.setSensorType(request.sensorType());
        reading.setValue(request.value());
        reading.setUnit(request.unit());
        reading.setRecordedAt(request.recordedAt() != null ? request.recordedAt() : Instant.now());
        SensorReading saved = sensorReadingRepository.save(reading);

        checkThresholds(saved, asset);
        return saved;
    }

    private void checkThresholds(SensorReading reading, Asset asset) {
        List<SensorThresholdRule> rules = new ArrayList<>();
        rules.addAll(thresholdRuleRepository.findByAssetIdAndSensorTypeAndActiveTrue(asset.getId(), reading.getSensorType()));
        if (asset.getAssetType() != null) {
            rules.addAll(thresholdRuleRepository.findByAssetTypeIdAndSensorTypeAndActiveTrue(
                    asset.getAssetType().getId(), reading.getSensorType()));
        }

        for (SensorThresholdRule rule : rules) {
            boolean breached = (rule.getMinValue() != null && reading.getValue().compareTo(rule.getMinValue()) < 0)
                    || (rule.getMaxValue() != null && reading.getValue().compareTo(rule.getMaxValue()) > 0);
            if (breached) {
                triggerAlert(reading, asset, rule);
            }
        }
    }

    private void triggerAlert(SensorReading reading, Asset asset, SensorThresholdRule rule) {
        WorkOrder wo = new WorkOrder();
        wo.setCode("PRED-" + asset.getId() + "-" + System.currentTimeMillis());
        wo.setAsset(asset);
        wo.setType(WorkOrder.Type.PREDICTIVE);
        wo.setPriority(WorkOrder.Priority.HIGH);
        wo.setTitle("Alerta preditivo: " + reading.getSensorType() + " fora do limite");
        wo.setDescription("Leitura de " + reading.getValue() + " " + (reading.getUnit() != null ? reading.getUnit() : "")
                + " fora do limite configurado (min=" + rule.getMinValue() + ", max=" + rule.getMaxValue() + ")");
        wo.setStatus(WorkOrder.Status.OPEN);
        wo.setOpenedAt(Instant.now());
        wo.setCreatedAt(Instant.now());
        wo.setUpdatedAt(Instant.now());
        WorkOrder savedWo = workOrderRepository.save(wo);

        eventService.record(savedWo, WorkOrderEvent.EventType.STATUS_CHANGE,
                "OS gerada automaticamente por alerta preditivo de sensor", null);

        SensorAlert alert = new SensorAlert();
        alert.setSensorReading(reading);
        alert.setThresholdRule(rule);
        alert.setWorkOrder(savedWo);
        alert.setTriggeredAt(Instant.now());
        sensorAlertRepository.save(alert);
    }

    private SensorReadingResponse toResponse(SensorReading entity) {
        return new SensorReadingResponse(
                entity.getId(),
                entity.getAsset().getId(),
                entity.getSensorType(),
                entity.getValue(),
                entity.getUnit(),
                entity.getRecordedAt()
        );
    }
}
