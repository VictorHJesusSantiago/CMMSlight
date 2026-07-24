package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PartRequest(
        @NotBlank(message = "Codigo e obrigatorio") String code,
        @NotBlank(message = "Nome e obrigatorio") String name,
        String unit,
        @NotNull(message = "Quantidade em estoque e obrigatoria") BigDecimal quantityOnHand,
        @NotNull(message = "Quantidade minima e obrigatoria") BigDecimal minQuantity,
        Long supplierId
) {
}
