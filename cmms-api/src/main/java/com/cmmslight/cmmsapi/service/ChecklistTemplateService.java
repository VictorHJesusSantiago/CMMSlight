package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.ChecklistItem;
import com.cmmslight.cmmsapi.domain.ChecklistTemplate;
import com.cmmslight.cmmsapi.dto.ChecklistItemRequest;
import com.cmmslight.cmmsapi.dto.ChecklistItemResponse;
import com.cmmslight.cmmsapi.dto.ChecklistTemplateRequest;
import com.cmmslight.cmmsapi.dto.ChecklistTemplateResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.ChecklistItemRepository;
import com.cmmslight.cmmsapi.repository.ChecklistTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository templateRepository;
    private final ChecklistItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public ChecklistTemplateService(ChecklistTemplateRepository templateRepository,
                                     ChecklistItemRepository itemRepository,
                                     ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    public List<ChecklistTemplateResponse> findAll() {
        return templateRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ChecklistTemplateResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public ChecklistTemplateResponse create(ChecklistTemplateRequest request) {
        ChecklistTemplate entity = new ChecklistTemplate();
        entity.setName(request.name());
        entity.setDescription(request.description());
        return toResponse(templateRepository.save(entity));
    }

    public ChecklistTemplateResponse update(Long id, ChecklistTemplateRequest request) {
        ChecklistTemplate entity = getOrThrow(id);
        entity.setName(request.name());
        entity.setDescription(request.description());
        return toResponse(templateRepository.save(entity));
    }

    public void delete(Long id) {
        templateRepository.delete(getOrThrow(id));
    }

    public ChecklistItemResponse addItem(Long templateId, ChecklistItemRequest request) {
        ChecklistTemplate template = getOrThrow(templateId);
        if (request.itemType() == ChecklistItem.ItemType.MULTIPLE_CHOICE
                && (request.options() == null || request.options().isEmpty())) {
            throw new ValidationException("Itens do tipo MULTIPLE_CHOICE exigem uma lista de opcoes");
        }
        ChecklistItem item = new ChecklistItem();
        item.setChecklistTemplate(template);
        applyItemRequest(item, request);
        return toItemResponse(itemRepository.save(item));
    }

    public ChecklistItemResponse updateItem(Long templateId, Long itemId, ChecklistItemRequest request) {
        getOrThrow(templateId);
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item de checklist nao encontrado: " + itemId));
        applyItemRequest(item, request);
        return toItemResponse(itemRepository.save(item));
    }

    public void deleteItem(Long templateId, Long itemId) {
        getOrThrow(templateId);
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item de checklist nao encontrado: " + itemId));
        itemRepository.delete(item);
    }

    ChecklistTemplate getOrThrow(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Template de checklist nao encontrado: " + id));
    }

    private void applyItemRequest(ChecklistItem item, ChecklistItemRequest request) {
        item.setDescription(request.description());
        item.setSortOrder(request.sortOrder());
        item.setItemType(request.itemType());
        item.setRequired(request.required());
        item.setOptions(writeOptions(request.options()));
    }

    private String writeOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar opcoes do item", e);
        }
    }

    private List<String> readOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao interpretar opcoes do item", e);
        }
    }

    private ChecklistTemplateResponse toResponse(ChecklistTemplate entity) {
        List<ChecklistItemResponse> items = itemRepository.findByChecklistTemplateIdOrderBySortOrderAsc(entity.getId())
                .stream().map(this::toItemResponse).toList();
        return new ChecklistTemplateResponse(entity.getId(), entity.getName(), entity.getDescription(), items);
    }

    private ChecklistItemResponse toItemResponse(ChecklistItem item) {
        return new ChecklistItemResponse(
                item.getId(),
                item.getChecklistTemplate().getId(),
                item.getDescription(),
                item.getSortOrder(),
                item.getItemType(),
                readOptions(item.getOptions()),
                item.isRequired()
        );
    }
}
