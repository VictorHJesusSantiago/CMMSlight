package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.Asset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record AssetResponse(
        Long id,
        String code,
        String name,
        Long assetTypeId,
        String assetTypeName,
        Long parentAssetId,
        String location,
        String manufacturer,
        String model,
        String serialNumber,
        LocalDate installDate,
        Asset.Status status,
        Asset.Criticality criticality,
        CriticalityAlert criticalityAlert,
        String warrantyProvider,
        LocalDate warrantyExpiration,
        boolean warrantyExpired,
        String warrantyTerms,
        Integer estimatedLifespanMonths,
        BigDecimal acquisitionCost,
        LocalDate acquisitionDate,
        BigDecimal currentDepreciatedValue,
        Map<String, Object> customAttributes
) {
    public enum CriticalityAlert { NONE, WATCH, ALERT, CRITICAL_ALERT }
}
