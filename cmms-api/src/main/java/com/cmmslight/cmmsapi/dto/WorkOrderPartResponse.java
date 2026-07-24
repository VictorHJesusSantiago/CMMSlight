package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;

public record WorkOrderPartResponse(
        Long id,
        Long workOrderId,
        Long partId,
        String partName,
        BigDecimal quantityUsed
) {
}
