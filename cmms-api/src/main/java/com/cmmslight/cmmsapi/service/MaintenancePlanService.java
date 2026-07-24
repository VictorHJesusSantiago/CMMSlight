package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.AssetType;
import com.cmmslight.cmmsapi.domain.ChecklistTemplate;
import com.cmmslight.cmmsapi.domain.MaintenancePlan;
import com.cmmslight.cmmsapi.dto.MaintenancePlanRequest;
import com.cmmslight.cmmsapi.dto.MaintenancePlanResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.AssetTypeRepository;
import com.cmmslight.cmmsapi.repository.ChecklistTemplateRepository;
import com.cmmslight.cmmsapi.repository.MaintenancePlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class MaintenancePlanService {

    private final MaintenancePlanRepository maintenancePlanRepository;
    private final AssetRepository assetRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;

    public MaintenancePlanService(MaintenancePlanRepository maintenancePlanRepository,
                                   AssetRepository assetRepository,
                                   AssetTypeRepository assetTypeRepository,
                                   ChecklistTemplateRepository checklistTemplateRepository) {
        this.maintenancePlanRepository = maintenancePlanRepository;
        this.assetRepository = assetRepository;
        this.assetTypeRepository = assetTypeRepository;
        this.checklistTemplateRepository = checklistTemplateRepository;
    }

    public List<MaintenancePlanResponse> findAll() {
        return maintenancePlanRepository.findAll().stream().map(this::toResponse).toList();
    }

    public MaintenancePlanResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<MaintenancePlanResponse> findOverdue() {
        Instant now = Instant.now();
        return maintenancePlanRepository.findByActiveTrue().stream()
                .filter(p -> p.getFrequencyType() == MaintenancePlan.FrequencyType.TIME)
                .filter(p -> {
                    Instant due = computeNextDueAt(p);
                    return due != null && due.isBefore(now);
                })
                .map(this::toResponse)
                .toList();
    }

    public List<MaintenancePlanResponse> calendar(Instant from, Instant to) {
        return maintenancePlanRepository.findByActiveTrue().stream()
                .filter(p -> p.getFrequencyType() == MaintenancePlan.FrequencyType.TIME)
                .filter(p -> {
                    Instant due = computeNextDueAt(p);
                    return due != null && !due.isBefore(from) && !due.isAfter(to);
                })
                .map(this::toResponse)
                .toList();
    }

    public MaintenancePlanResponse create(MaintenancePlanRequest request) {
        MaintenancePlan entity = new MaintenancePlan();
        applyRequest(entity, request);
        entity.setCreatedAt(Instant.now());
        return toResponse(maintenancePlanRepository.save(entity));
    }

    public MaintenancePlanResponse update(Long id, MaintenancePlanRequest request) {
        MaintenancePlan entity = getOrThrow(id);
        applyRequest(entity, request);
        return toResponse(maintenancePlanRepository.save(entity));
    }

    public void delete(Long id) {
        maintenancePlanRepository.delete(getOrThrow(id));
    }

    MaintenancePlan getOrThrow(Long id) {
        return maintenancePlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plano de manutencao nao encontrado: " + id));
    }

    private void applyRequest(MaintenancePlan entity, MaintenancePlanRequest request) {
        if (request.assetId() == null && request.assetTypeId() == null) {
            throw new ValidationException("Informe um ativo ou um tipo de ativo para o plano de manutencao");
        }
        entity.setName(request.name());

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

        ChecklistTemplate template = null;
        if (request.checklistTemplateId() != null) {
            template = checklistTemplateRepository.findById(request.checklistTemplateId())
                    .orElseThrow(() -> new NotFoundException("Template de checklist nao encontrado: " + request.checklistTemplateId()));
        }
        entity.setChecklistTemplate(template);

        entity.setFrequencyType(request.frequencyType());
        entity.setFrequencyValue(request.frequencyValue());
        entity.setFrequencyUnit(request.frequencyUnit() != null ? request.frequencyUnit() : "DAYS");
        entity.setActive(request.active());
    }

    /** Proxima data de vencimento para planos baseados em tempo (dias). */
    Instant computeNextDueAt(MaintenancePlan plan) {
        if (plan.getFrequencyType() != MaintenancePlan.FrequencyType.TIME) {
            return null;
        }
        Instant base = plan.getLastGeneratedAt();
        if (base == null) {
            base = plan.getCreatedAt() != null ? plan.getCreatedAt() : Instant.now();
        }
        return base.plus(plan.getFrequencyValue(), ChronoUnit.DAYS);
    }

    private MaintenancePlanResponse toResponse(MaintenancePlan entity) {
        Instant nextDueAt = computeNextDueAt(entity);
        boolean overdue = nextDueAt != null && nextDueAt.isBefore(Instant.now());
        return new MaintenancePlanResponse(
                entity.getId(),
                entity.getName(),
                entity.getAsset() != null ? entity.getAsset().getId() : null,
                entity.getAsset() != null ? entity.getAsset().getName() : null,
                entity.getAssetType() != null ? entity.getAssetType().getId() : null,
                entity.getAssetType() != null ? entity.getAssetType().getName() : null,
                entity.getChecklistTemplate() != null ? entity.getChecklistTemplate().getId() : null,
                entity.getFrequencyType(),
                entity.getFrequencyValue(),
                entity.getFrequencyUnit(),
                entity.isActive(),
                entity.getLastGeneratedAt(),
                nextDueAt,
                overdue
        );
    }
}
