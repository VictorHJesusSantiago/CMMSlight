package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.AppUserResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.AppUserRepository;
import com.cmmslight.cmmsapi.domain.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository appUserRepository;

    public AuthController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/me")
    public AppUserResponse me(Authentication authentication) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Usuario autenticado nao encontrado"));
        return new AppUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }
}
