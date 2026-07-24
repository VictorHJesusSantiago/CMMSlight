package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkOrderSignRequest(
        @NotBlank(message = "Nome do assinante e obrigatorio") String signedByName
) {
}
