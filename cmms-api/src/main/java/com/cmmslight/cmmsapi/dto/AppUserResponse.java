package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.AppUser;

public record AppUserResponse(
        Long id,
        String name,
        String email,
        AppUser.Role role,
        boolean active
) {
}
