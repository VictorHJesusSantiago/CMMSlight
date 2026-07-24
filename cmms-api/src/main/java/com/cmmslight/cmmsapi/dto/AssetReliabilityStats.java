package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;

/** MTBF/MTTR e contagem de falhas de um ativo, usados no ranking de Pareto. */
public record AssetReliabilityStats(
        Long assetId,
        String assetCode,
        String assetName,
        long failureCount,
        BigDecimal mtbfHours,
        BigDecimal mttrHours
) {
}
