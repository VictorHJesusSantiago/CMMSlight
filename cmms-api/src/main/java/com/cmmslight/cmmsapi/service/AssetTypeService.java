package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.AssetType;
import com.cmmslight.cmmsapi.dto.AssetTypeRequest;
import com.cmmslight.cmmsapi.dto.AssetTypeResponse;
import com.cmmslight.cmmsapi.dto.CustomAttributeDefinition;
import com.cmmslight.cmmsapi.exception.ConflictException;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.AssetTypeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AssetTypeService {

    private final AssetTypeRepository assetTypeRepository;
    private final ObjectMapper objectMapper;

    public AssetTypeService(AssetTypeRepository assetTypeRepository, ObjectMapper objectMapper) {
        this.assetTypeRepository = assetTypeRepository;
        this.objectMapper = objectMapper;
    }

    public List<AssetTypeResponse> findAll() {
        return assetTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AssetTypeResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public AssetTypeResponse create(AssetTypeRequest request) {
        if (assetTypeRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Ja existe um tipo de ativo com o nome '" + request.name() + "'");
        }
        AssetType entity = new AssetType();
        applyRequest(entity, request);
        return toResponse(assetTypeRepository.save(entity));
    }

    public AssetTypeResponse update(Long id, AssetTypeRequest request) {
        AssetType entity = getOrThrow(id);
        assetTypeRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Ja existe um tipo de ativo com o nome '" + request.name() + "'");
            }
        });
        applyRequest(entity, request);
        return toResponse(assetTypeRepository.save(entity));
    }

    public void delete(Long id) {
        AssetType entity = getOrThrow(id);
        assetTypeRepository.delete(entity);
    }

    AssetType getOrThrow(Long id) {
        return assetTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de ativo nao encontrado: " + id));
    }

    private void applyRequest(AssetType entity, AssetTypeRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCustomAttributesSchema(writeSchema(request.customAttributesSchema()));
    }

    private String writeSchema(List<CustomAttributeDefinition> schema) {
        if (schema == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar schema de atributos", e);
        }
    }

    List<CustomAttributeDefinition> readSchema(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CustomAttributeDefinition>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao interpretar schema de atributos", e);
        }
    }

    private AssetTypeResponse toResponse(AssetType entity) {
        return new AssetTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                readSchema(entity.getCustomAttributesSchema())
        );
    }
}
