package com.cmmslight.cmmsapi.dto;

import java.util.List;

public record AssetTypeResponse(
        Long id,
        String name,
        String description,
        List<CustomAttributeDefinition> customAttributesSchema
) {
}
