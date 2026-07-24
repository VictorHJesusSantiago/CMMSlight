package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.FailureHistory;

import java.time.Instant;

public record FailureHistoryResponse(
        Long id,
        Long assetId,
        String assetName,
        Long workOrderId,
        Instant failedAt,
        Instant resolvedAt,
        Integer downtimeMinutes,
        String description,
        String rootCause,
        FailureHistory.Classification classification,
        String why1,
        String why2,
        String why3,
        String why4,
        String why5
) {
}
