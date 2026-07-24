package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;

public record PartResponse(
        Long id,
        String code,
        String name,
        String unit,
        BigDecimal quantityOnHand,
        BigDecimal minQuantity,
        boolean belowMinimum,
        Long supplierId,
        String supplierName
) {
}
