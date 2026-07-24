package com.cmmslight.cmmsapi.dto;

import java.util.List;

public record ChecklistTemplateResponse(
        Long id,
        String name,
        String description,
        List<ChecklistItemResponse> items
) {
}
