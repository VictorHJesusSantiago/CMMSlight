package com.cmmslight.cmmsapi.dto;

public record SupplierResponse(
        Long id,
        String name,
        String contactName,
        String phone,
        String email,
        String notes
) {
}
