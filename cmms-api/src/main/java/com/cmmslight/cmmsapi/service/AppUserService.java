package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.AppUser;
import com.cmmslight.cmmsapi.dto.AppUserRequest;
import com.cmmslight.cmmsapi.dto.AppUserResponse;
import com.cmmslight.cmmsapi.exception.ConflictException;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUserResponse> findAll() {
        return appUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AppUserResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public AppUserResponse create(AppUserRequest request) {
        if (appUserRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ConflictException("Ja existe um usuario com o email '" + request.email() + "'");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ValidationException("Senha e obrigatoria na criacao do usuario");
        }
        AppUser entity = new AppUser();
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setRole(request.role());
        entity.setActive(request.active());
        entity.setCreatedAt(Instant.now());
        return toResponse(appUserRepository.save(entity));
    }

    public AppUserResponse update(Long id, AppUserRequest request) {
        AppUser entity = getOrThrow(id);
        appUserRepository.findByEmailIgnoreCase(request.email()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Ja existe um usuario com o email '" + request.email() + "'");
            }
        });
        entity.setName(request.name());
        entity.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            entity.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        entity.setRole(request.role());
        entity.setActive(request.active());
        return toResponse(appUserRepository.save(entity));
    }

    public void delete(Long id) {
        appUserRepository.delete(getOrThrow(id));
    }

    AppUser getOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + id));
    }

    private AppUserResponse toResponse(AppUser entity) {
        return new AppUserResponse(entity.getId(), entity.getName(), entity.getEmail(), entity.getRole(), entity.isActive());
    }
}
