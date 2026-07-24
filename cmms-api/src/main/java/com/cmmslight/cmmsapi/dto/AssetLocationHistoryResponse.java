package com.cmmslight.cmmsapi.dto;

import java.time.Instant;

public record AssetLocationHistoryResponse(
        Long id,
        Long assetId,
        String previousLocation,
        String newLocation,
        Instant movedAt,
        Long movedByUserId,
        String movedByUserName,
        String notes
) {
}
