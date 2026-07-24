package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

public record AssetMoveRequest(
        @NotBlank(message = "Novo local e obrigatorio") String newLocation,
        Long movedByUserId,
        String notes
) {
}
