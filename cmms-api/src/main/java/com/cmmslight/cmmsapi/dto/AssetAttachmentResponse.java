package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.AssetAttachment;

import java.time.Instant;

public record AssetAttachmentResponse(
        Long id,
        Long assetId,
        String fileName,
        String contentType,
        long sizeBytes,
        AssetAttachment.Category category,
        Instant uploadedAt,
        String downloadUrl
) {
}
