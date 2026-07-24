package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.AssetLocationHistory;
import com.cmmslight.cmmsapi.domain.AssetType;
import com.cmmslight.cmmsapi.domain.AuditLog;
import com.cmmslight.cmmsapi.dto.AssetLocationHistoryResponse;
import com.cmmslight.cmmsapi.dto.AssetMoveRequest;
import com.cmmslight.cmmsapi.dto.AssetRequest;
import com.cmmslight.cmmsapi.dto.AssetResponse;
import com.cmmslight.cmmsapi.dto.CustomAttributeDefinition;
import com.cmmslight.cmmsapi.exception.ConflictException;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.AppUserRepository;
import com.cmmslight.cmmsapi.repository.AssetLocationHistoryRepository;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeService assetTypeService;
    private final AssetLocationHistoryRepository locationHistoryRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public AssetService(AssetRepository assetRepository,
                         AssetTypeService assetTypeService,
                         AssetLocationHistoryRepository locationHistoryRepository,
                         AppUserRepository appUserRepository,
                         ObjectMapper objectMapper,
                         AuditLogService auditLogService) {
        this.assetRepository = assetRepository;
        this.assetTypeService = assetTypeService;
        this.locationHistoryRepository = locationHistoryRepository;
        this.appUserRepository = appUserRepository;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    public List<AssetResponse> findAll() {
        return assetRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<AssetResponse> findRootAssets() {
        return assetRepository.findByParentAssetIsNull().stream().map(this::toResponse).toList();
    }

    public List<AssetResponse> findChildren(Long parentId) {
        getOrThrow(parentId);
        return assetRepository.findByParentAssetId(parentId).stream().map(this::toResponse).toList();
    }

    public AssetResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public AssetResponse create(AssetRequest request) {
        if (assetRepository.existsByCode(request.code())) {
            throw new ConflictException("Ja existe um ativo com o codigo '" + request.code() + "'");
        }
        Asset entity = new Asset();
        entity.setCode(request.code());
        applyRequest(entity, request, null);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        Asset saved = assetRepository.save(entity);
        if (request.location() != null && !request.location().isBlank()) {
            recordLocationHistory(saved, null, request.location(), null, "Cadastro inicial do ativo");
        }
        auditLogService.log("Asset", saved.getId(), AuditLog.Action.CREATE, "Ativo criado: " + saved.getCode());
        return toResponse(saved);
    }

    public AssetResponse update(Long id, AssetRequest request) {
        Asset entity = getOrThrow(id);
        assetRepository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Ja existe um ativo com o codigo '" + request.code() + "'");
            }
        });
        String previousLocation = entity.getLocation();
        entity.setCode(request.code());
        applyRequest(entity, request, id);
        entity.setUpdatedAt(Instant.now());
        Asset saved = assetRepository.save(entity);

        boolean locationChanged = request.location() != null && !request.location().equals(previousLocation);
        if (locationChanged) {
            recordLocationHistory(saved, previousLocation, request.location(), null, "Atualizacao de cadastro");
        }
        auditLogService.log("Asset", saved.getId(), AuditLog.Action.UPDATE, "Ativo atualizado: " + saved.getCode());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Asset entity = getOrThrow(id);
        if (!assetRepository.findByParentAssetId(id).isEmpty()) {
            throw new ConflictException("Nao e possivel excluir um ativo que possui ativos filhos vinculados");
        }
        assetRepository.delete(entity);
        auditLogService.log("Asset", id, AuditLog.Action.DELETE, "Ativo excluido: " + entity.getCode());
    }

    public AssetLocationHistoryResponse moveAsset(Long id, AssetMoveRequest request) {
        Asset entity = getOrThrow(id);
        String previousLocation = entity.getLocation();
        entity.setLocation(request.newLocation());
        entity.setUpdatedAt(Instant.now());
        assetRepository.save(entity);

        var movedBy = request.movedByUserId() == null ? null
                : appUserRepository.findById(request.movedByUserId())
                    .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + request.movedByUserId()));

        AssetLocationHistory history = recordLocationHistory(entity, previousLocation, request.newLocation(), movedBy, request.notes());
        return toHistoryResponse(history);
    }

    public List<AssetLocationHistoryResponse> getLocationHistory(Long assetId) {
        getOrThrow(assetId);
        return locationHistoryRepository.findByAssetIdOrderByMovedAtDesc(assetId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    /** Calcula o valor depreciado atual do ativo por depreciacao linear simples. */
    public BigDecimal calculateCurrentDepreciatedValue(Asset asset) {
        if (asset.getAcquisitionCost() == null || asset.getAcquisitionDate() == null
                || asset.getEstimatedLifespanMonths() == null || asset.getEstimatedLifespanMonths() <= 0) {
            return null;
        }
        long monthsElapsed = ChronoUnit.MONTHS.between(
                asset.getAcquisitionDate().withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(1));
        if (monthsElapsed <= 0) {
            return asset.getAcquisitionCost();
        }
        if (monthsElapsed >= asset.getEstimatedLifespanMonths()) {
            return BigDecimal.ZERO;
        }
        BigDecimal remainingFraction = BigDecimal.valueOf(asset.getEstimatedLifespanMonths() - monthsElapsed)
                .divide(BigDecimal.valueOf(asset.getEstimatedLifespanMonths()), 6, RoundingMode.HALF_UP);
        return asset.getAcquisitionCost().multiply(remainingFraction).setScale(2, RoundingMode.HALF_UP);
    }

    private AssetLocationHistory recordLocationHistory(Asset asset, String previousLocation, String newLocation,
                                                         com.cmmslight.cmmsapi.domain.AppUser movedBy, String notes) {
        AssetLocationHistory history = new AssetLocationHistory();
        history.setAsset(asset);
        history.setPreviousLocation(previousLocation);
        history.setNewLocation(newLocation);
        history.setMovedBy(movedBy);
        history.setMovedAt(Instant.now());
        history.setNotes(notes);
        return locationHistoryRepository.save(history);
    }

    private void applyRequest(Asset entity, AssetRequest request, Long currentId) {
        entity.setName(request.name());
        entity.setLocation(request.location());
        entity.setManufacturer(request.manufacturer());
        entity.setModel(request.model());
        entity.setSerialNumber(request.serialNumber());
        entity.setInstallDate(request.installDate());
        entity.setStatus(request.status());
        entity.setCriticality(request.criticality());
        entity.setWarrantyProvider(request.warrantyProvider());
        entity.setWarrantyExpiration(request.warrantyExpiration());
        entity.setWarrantyTerms(request.warrantyTerms());
        entity.setEstimatedLifespanMonths(request.estimatedLifespanMonths());
        entity.setAcquisitionCost(request.acquisitionCost());
        entity.setAcquisitionDate(request.acquisitionDate());

        AssetType assetType = null;
        if (request.assetTypeId() != null) {
            assetType = assetTypeService.getOrThrow(request.assetTypeId());
        }
        entity.setAssetType(assetType);

        if (request.parentAssetId() != null) {
            if (currentId != null && request.parentAssetId().equals(currentId)) {
                throw new ValidationException("Um ativo nao pode ser pai de si mesmo");
            }
            Asset parent = assetRepository.findById(request.parentAssetId())
                    .orElseThrow(() -> new NotFoundException("Ativo pai nao encontrado: " + request.parentAssetId()));
            if (currentId != null && isDescendant(parent, currentId)) {
                throw new ValidationException("Vinculo de hierarquia geraria um ciclo entre ativos");
            }
            entity.setParentAsset(parent);
        } else {
            entity.setParentAsset(null);
        }

        entity.setCustomAttributes(validateAndSerializeCustomAttributes(assetType, request.customAttributes()));
    }

    private boolean isDescendant(Asset candidateAncestor, Long targetId) {
        Asset cursor = candidateAncestor;
        while (cursor != null) {
            if (cursor.getId().equals(targetId)) {
                return true;
            }
            cursor = cursor.getParentAsset();
        }
        return false;
    }

    private String validateAndSerializeCustomAttributes(AssetType assetType, Map<String, Object> values) {
        if (assetType == null || assetType.getCustomAttributesSchema() == null) {
            return values == null ? null : writeMap(values);
        }
        List<CustomAttributeDefinition> schema = assetTypeService.readSchema(assetType.getCustomAttributesSchema());
        Map<String, Object> safeValues = values == null ? Map.of() : values;
        for (CustomAttributeDefinition def : schema) {
            Object value = safeValues.get(def.name());
            if (def.required() && (value == null || value.toString().isBlank())) {
                throw new ValidationException("Atributo customizado obrigatorio ausente: " + def.label());
            }
            if (value != null) {
                validateAttributeType(def, value);
            }
        }
        return writeMap(safeValues);
    }

    private void validateAttributeType(CustomAttributeDefinition def, Object value) {
        boolean valid = switch (def.type()) {
            case NUMBER -> value instanceof Number || (value instanceof String s && s.matches("-?\\d+(\\.\\d+)?"));
            case BOOLEAN -> value instanceof Boolean || "true".equalsIgnoreCase(String.valueOf(value)) || "false".equalsIgnoreCase(String.valueOf(value));
            case DATE -> {
                try {
                    LocalDate.parse(String.valueOf(value));
                    yield true;
                } catch (Exception e) {
                    yield false;
                }
            }
            case TEXT -> true;
        };
        if (!valid) {
            throw new ValidationException("Valor invalido para o atributo '" + def.label() + "': esperado tipo " + def.type());
        }
    }

    private String writeMap(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar atributos customizados", e);
        }
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao interpretar atributos customizados", e);
        }
    }

    Asset getOrThrow(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + id));
    }

    private AssetResponse.CriticalityAlert computeAlert(Asset asset) {
        boolean warrantyExpired = asset.getWarrantyExpiration() != null
                && asset.getWarrantyExpiration().isBefore(LocalDate.now());
        return switch (asset.getCriticality()) {
            case CRITICAL -> warrantyExpired ? AssetResponse.CriticalityAlert.CRITICAL_ALERT : AssetResponse.CriticalityAlert.ALERT;
            case HIGH -> warrantyExpired ? AssetResponse.CriticalityAlert.ALERT : AssetResponse.CriticalityAlert.WATCH;
            case MEDIUM -> warrantyExpired ? AssetResponse.CriticalityAlert.WATCH : AssetResponse.CriticalityAlert.NONE;
            case LOW -> AssetResponse.CriticalityAlert.NONE;
        };
    }

    private AssetResponse toResponse(Asset entity) {
        boolean warrantyExpired = entity.getWarrantyExpiration() != null
                && entity.getWarrantyExpiration().isBefore(LocalDate.now());
        return new AssetResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAssetType() != null ? entity.getAssetType().getId() : null,
                entity.getAssetType() != null ? entity.getAssetType().getName() : null,
                entity.getParentAsset() != null ? entity.getParentAsset().getId() : null,
                entity.getLocation(),
                entity.getManufacturer(),
                entity.getModel(),
                entity.getSerialNumber(),
                entity.getInstallDate(),
                entity.getStatus(),
                entity.getCriticality(),
                computeAlert(entity),
                entity.getWarrantyProvider(),
                entity.getWarrantyExpiration(),
                warrantyExpired,
                entity.getWarrantyTerms(),
                entity.getEstimatedLifespanMonths(),
                entity.getAcquisitionCost(),
                entity.getAcquisitionDate(),
                calculateCurrentDepreciatedValue(entity),
                readMap(entity.getCustomAttributes())
        );
    }

    private AssetLocationHistoryResponse toHistoryResponse(AssetLocationHistory history) {
        return new AssetLocationHistoryResponse(
                history.getId(),
                history.getAsset().getId(),
                history.getPreviousLocation(),
                history.getNewLocation(),
                history.getMovedAt(),
                history.getMovedBy() != null ? history.getMovedBy().getId() : null,
                history.getMovedBy() != null ? history.getMovedBy().getName() : null,
                history.getNotes()
        );
    }
}
