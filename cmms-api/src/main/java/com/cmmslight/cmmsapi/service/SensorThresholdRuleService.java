package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.AssetType;
import com.cmmslight.cmmsapi.domain.SensorThresholdRule;
import com.cmmslight.cmmsapi.dto.SensorThresholdRuleRequest;
import com.cmmslight.cmmsapi.dto.SensorThresholdRuleResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.AssetTypeRepository;
import com.cmmslight.cmmsapi.repository.SensorThresholdRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SensorThresholdRuleService {

    private final SensorThresholdRuleRepository ruleRepository;
    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;

    public SensorThresholdRuleService(SensorThresholdRuleRepository ruleRepository,
                                       AssetRepository assetRepository,
                                       AssetTypeRepository assetTypeRepository) {
        this.ruleRepository = ruleRepository;
        this.assetRepository = assetRepository;
        this.assetTypeRepository = assetTypeRepository;
    }

    public List<SensorThresholdRuleResponse> findAll() {
        return ruleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SensorThresholdRuleResponse create(SensorThresholdRuleRequest request) {
        SensorThresholdRule entity = new SensorThresholdRule();
        applyRequest(entity, request);
        return toResponse(ruleRepository.save(entity));
    }

    public SensorThresholdRuleResponse update(Long id, SensorThresholdRuleRequest request) {
        SensorThresholdRule entity = getOrThrow(id);
        applyRequest(entity, request);
        return toResponse(ruleRepository.save(entity));
    }

    public void delete(Long id) {
        ruleRepository.delete(getOrThrow(id));
    }

    SensorThresholdRule getOrThrow(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Regra de limite nao encontrada: " + id));
    }

    private void applyRequest(SensorThresholdRule entity, SensorThresholdRuleRequest request) {
        if (request.assetId() == null && request.assetTypeId() == null) {
            throw new ValidationException("Informe um ativo ou um tipo de ativo para a regra de limite");
        }
        if (request.minValue() == null && request.maxValue() == null) {
            throw new ValidationException("Informe ao menos um limite (minimo ou maximo)");
        }

        Asset asset = null;
        if (request.assetId() != null) {
            asset = assetRepository.findById(request.assetId())
                    .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + request.assetId()));
        }
        entity.setAsset(asset);

        AssetType assetType = null;
        if (request.assetTypeId() != null) {
            assetType = assetTypeRepository.findById(request.assetTypeId())
                    .orElseThrow(() -> new NotFoundException("Tipo de ativo nao encontrado: " + request.assetTypeId()));
        }
        entity.setAssetType(assetType);

        entity.setSensorType(request.sensorType());
        entity.setMinValue(request.minValue());
        entity.setMaxValue(request.maxValue());
        entity.setActive(request.active());
    }

    private SensorThresholdRuleResponse toResponse(SensorThresholdRule entity) {
        return new SensorThresholdRuleResponse(
                entity.getId(),
                entity.getAsset() != null ? entity.getAsset().getId() : null,
                entity.getAssetType() != null ? entity.getAssetType().getId() : null,
                entity.getSensorType(),
                entity.getMinValue(),
                entity.getMaxValue(),
                entity.isActive()
        );
    }
}
