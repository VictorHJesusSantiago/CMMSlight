package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.ChecklistItem;

import java.util.List;

public record ChecklistItemResponse(
        Long id,
        Long checklistTemplateId,
        String description,
        int sortOrder,
        ChecklistItem.ItemType itemType,
        List<String> options,
        boolean required
) {
}
