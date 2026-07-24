package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.WorkOrderAttachment;

import java.time.Instant;

public record WorkOrderAttachmentResponse(
        Long id,
        Long workOrderId,
        String fileName,
        String contentType,
        long sizeBytes,
        WorkOrderAttachment.Category category,
        Instant uploadedAt,
        String downloadUrl
) {
}
