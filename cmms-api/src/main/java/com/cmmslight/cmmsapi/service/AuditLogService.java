package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.AppUser;
import com.cmmslight.cmmsapi.domain.AuditLog;
import com.cmmslight.cmmsapi.dto.AuditLogResponse;
import com.cmmslight.cmmsapi.repository.AppUserRepository;
import com.cmmslight.cmmsapi.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AppUserRepository appUserRepository) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    /** Registra uma acao de auditoria, resolvendo o usuario autenticado a partir do contexto de seguranca. */
    public void log(String entityName, Long entityId, AuditLog.Action action, String details) {
        AuditLog entry = new AuditLog();
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setDetails(details);
        entry.setPerformedAt(Instant.now());
        entry.setPerformedBy(currentUser());
        auditLogRepository.save(entry);
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return appUserRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByPerformedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<AuditLogResponse> findByEntity(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByPerformedAtDesc(entityName, entityId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getEntityName(),
                entity.getEntityId(),
                entity.getAction(),
                entity.getPerformedBy() != null ? entity.getPerformedBy().getId() : null,
                entity.getPerformedBy() != null ? entity.getPerformedBy().getName() : null,
                entity.getPerformedAt(),
                entity.getDetails()
        );
    }
}
