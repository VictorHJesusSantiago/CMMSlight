package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AssetTypeRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,
        String description,
        List<CustomAttributeDefinition> customAttributesSchema
) {
}
