package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.ChecklistItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChecklistItemRequest(
        @NotBlank(message = "Descricao e obrigatoria") String description,
        int sortOrder,
        @NotNull(message = "Tipo do item e obrigatorio") ChecklistItem.ItemType itemType,
        List<String> options,
        boolean required
) {
}
