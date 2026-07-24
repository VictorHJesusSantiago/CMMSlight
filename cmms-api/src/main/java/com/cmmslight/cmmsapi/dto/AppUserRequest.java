package com.cmmslight.cmmsapi.dto;

import com.cmmslight.cmmsapi.domain.AppUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppUserRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,
        @NotBlank(message = "Email e obrigatorio") @Email(message = "Email invalido") String email,
        @Size(min = 6, message = "Senha deve ter ao menos 6 caracteres") String password,
        @NotNull(message = "Perfil e obrigatorio") AppUser.Role role,
        boolean active
) {
}
