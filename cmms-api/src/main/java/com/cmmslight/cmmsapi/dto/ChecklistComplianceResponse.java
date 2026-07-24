package com.cmmslight.cmmsapi.dto;

import java.math.BigDecimal;

public record ChecklistComplianceResponse(
        Long workOrderId,
        int totalItems,
        int answeredItems,
        int compliantItems,
        BigDecimal compliancePercentage
) {
}
