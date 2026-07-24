package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.Asset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record AssetRequest(
        @NotBlank(message = "Codigo e obrigatorio") String code,
        @NotBlank(message = "Nome e obrigatorio") String name,
        Long assetTypeId,
        Long parentAssetId,
        String location,
        String manufacturer,
        String model,
        String serialNumber,
        LocalDate installDate,
        @NotNull(message = "Status e obrigatorio") Asset.Status status,
        @NotNull(message = "Criticidade e obrigatoria") Asset.Criticality criticality,
        String warrantyProvider,
        LocalDate warrantyExpiration,
        String warrantyTerms,
        Integer estimatedLifespanMonths,
        BigDecimal acquisitionCost,
        LocalDate acquisitionDate,
        Map<String, Object> customAttributes
) {
}
