package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WorkOrderPartRequest(
        @NotNull(message = "Peca e obrigatoria") Long partId,
        @NotNull(message = "Quantidade e obrigatoria") @DecimalMin(value = "0.01", message = "Quantidade deve ser positiva") BigDecimal quantityUsed
) {
}
