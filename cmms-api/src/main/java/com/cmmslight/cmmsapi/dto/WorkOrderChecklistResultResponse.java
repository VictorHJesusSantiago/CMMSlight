package com.cmmslight.cmmsapi.dto;

public record WorkOrderChecklistResultResponse(
        Long id,
        Long workOrderId,
        Long checklistItemId,
        String itemDescription,
        boolean completed,
        String value,
        String notes
) {
}
