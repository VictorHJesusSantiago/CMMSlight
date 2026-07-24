package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkOrderCommentRequest(
        @NotBlank(message = "Mensagem e obrigatoria") String message,
        Long createdByUserId
) {
}
