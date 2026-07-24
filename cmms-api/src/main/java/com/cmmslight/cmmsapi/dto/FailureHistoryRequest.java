package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.FailureHistory;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FailureHistoryRequest(
        @NotNull(message = "Ativo e obrigatorio") Long assetId,
        Long workOrderId,
        @NotNull(message = "Data da falha e obrigatoria") Instant failedAt,
        Instant resolvedAt,
        String description,
        String rootCause,
        @NotNull(message = "Classificacao e obrigatoria") FailureHistory.Classification classification,
        String why1,
        String why2,
        String why3,
        String why4,
        String why5
) {
}
