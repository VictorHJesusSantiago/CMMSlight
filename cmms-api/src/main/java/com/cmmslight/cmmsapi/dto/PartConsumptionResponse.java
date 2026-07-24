package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;

/** Consumo agregado de uma peca por ativo, usado para prever substituicoes futuras. */
public record PartConsumptionResponse(
        Long assetId,
        String assetName,
        Long partId,
        String partName,
        BigDecimal totalQuantityUsed,
        long workOrderCount
) {
}
