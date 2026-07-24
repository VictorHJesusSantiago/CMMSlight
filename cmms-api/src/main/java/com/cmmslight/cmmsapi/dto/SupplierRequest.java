package com.cmmslight.cmmsapi.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,
        String contactName,
        String phone,
        String email,
        String notes
) {
}
