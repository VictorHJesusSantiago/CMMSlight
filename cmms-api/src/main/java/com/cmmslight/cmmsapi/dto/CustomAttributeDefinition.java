package com.cmmslight.cmmsapi.dto;

/** Um item do schema de atributos customizaveis de um AssetType. */
public record CustomAttributeDefinition(
        String name,
        String label,
        AttributeType type,
        boolean required
) {
    public enum AttributeType { TEXT, NUMBER, BOOLEAN, DATE }
}
