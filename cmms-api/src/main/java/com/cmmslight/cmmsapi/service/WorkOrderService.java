package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.AppUser;
import com.cmmslight.cmmsapi.domain.AuditLog;
import com.cmmslight.cmmsapi.domain.MaintenancePlan;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.dto.WorkOrderAssignRequest;
import com.cmmslight.cmmsapi.dto.WorkOrderCommentRequest;
import com.cmmslight.cmmsapi.dto.WorkOrderRequest;
import com.cmmslight.cmmsapi.dto.WorkOrderResponse;
import com.cmmslight.cmmsapi.dto.WorkOrderSignRequest;
import com.cmmslight.cmmsapi.dto.WorkOrderStatusChangeRequest;
import com.cmmslight.cmmsapi.exception.ConflictException;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.AppUserRepository;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.MaintenancePlanRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class WorkOrderService {

    private static final Map<WorkOrder.Status, Set<WorkOrder.Status>> ALLOWED_TRANSITIONS = new EnumMap<>(WorkOrder.Status.class);

    static {
        ALLOWED_TRANSITIONS.put(WorkOrder.Status.OPEN, EnumSet.of(WorkOrder.Status.SCHEDULED, WorkOrder.Status.IN_PROGRESS, WorkOrder.Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(WorkOrder.Status.SCHEDULED, EnumSet.of(WorkOrder.Status.IN_PROGRESS, WorkOrder.Status.OPEN, WorkOrder.Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(WorkOrder.Status.IN_PROGRESS, EnumSet.of(WorkOrder.Status.DONE, WorkOrder.Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(WorkOrder.Status.DONE, EnumSet.noneOf(WorkOrder.Status.class));
        ALLOWED_TRANSITIONS.put(WorkOrder.Status.CANCELLED, EnumSet.noneOf(WorkOrder.Status.class));
    }

    private final WorkOrderRepository workOrderRepository;
    private final AssetRepository assetRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;
    private final AppUserRepository appUserRepository;
    private final WorkOrderEventService eventService;
    private final AuditLogService auditLogService;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                             AssetRepository assetRepository,
                             MaintenancePlanRepository maintenancePlanRepository,
                             AppUserRepository appUserRepository,
                             WorkOrderEventService eventService,
                             AuditLogService auditLogService) {
        this.workOrderRepository = workOrderRepository;
        this.assetRepository = assetRepository;
        this.maintenancePlanRepository = maintenancePlanRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
        this.eventService = eventService;
    }

    public List<WorkOrderResponse> findAll() {
        return workOrderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public WorkOrderResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<WorkOrderResponse> findByAsset(Long assetId) {
        return workOrderRepository.findByAssetId(assetId).stream().map(this::toResponse).toList();
    }

    public List<WorkOrderResponse> findByStatus(WorkOrder.Status status) {
        return workOrderRepository.findByStatus(status).stream()
                .sorted((a, b) -> b.getPriority().ordinal() - a.getPriority().ordinal())
                .map(this::toResponse)
                .toList();
    }

    public List<WorkOrderResponse> findQueueOrderedByPriority() {
        return workOrderRepository.findAll().stream()
                .filter(wo -> wo.getStatus() == WorkOrder.Status.OPEN || wo.getStatus() == WorkOrder.Status.SCHEDULED)
                .sorted((a, b) -> {
                    int byPriority = b.getPriority().ordinal() - a.getPriority().ordinal();
                    if (byPriority != 0) return byPriority;
                    int byCriticality = b.getAsset().getCriticality().ordinal() - a.getAsset().getCriticality().ordinal();
                    if (byCriticality != 0) return byCriticality;
                    return a.getOpenedAt().compareTo(b.getOpenedAt());
                })
                .map(this::toResponse)
                .toList();
    }

    public WorkOrderResponse create(WorkOrderRequest request) {
        if (workOrderRepository.existsByCode(request.code())) {
            throw new ConflictException("Ja existe uma OS com o codigo '" + request.code() + "'");
        }
        WorkOrder entity = new WorkOrder();
        entity.setCode(request.code());
        applyRequest(entity, request);
        entity.setOpenedAt(Instant.now());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        WorkOrder saved = workOrderRepository.save(entity);
        eventService.record(saved, WorkOrderEvent.EventType.STATUS_CHANGE, "OS criada com status OPEN", null);
        auditLogService.log("WorkOrder", saved.getId(), AuditLog.Action.CREATE, "OS criada: " + saved.getCode());
        return toResponse(saved);
    }

    public WorkOrderResponse update(Long id, WorkOrderRequest request) {
        WorkOrder entity = getOrThrow(id);
        workOrderRepository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Ja existe uma OS com o codigo '" + request.code() + "'");
            }
        });
        entity.setCode(request.code());
        applyRequest(entity, request);
        entity.setUpdatedAt(Instant.now());
        WorkOrder saved = workOrderRepository.save(entity);
        auditLogService.log("WorkOrder", saved.getId(), AuditLog.Action.UPDATE, "OS atualizada: " + saved.getCode());
        return toResponse(saved);
    }

    public void delete(Long id) {
        WorkOrder entity = getOrThrow(id);
        if (entity.getStatus() == WorkOrder.Status.IN_PROGRESS || entity.getStatus() == WorkOrder.Status.DONE) {
            throw new ConflictException("Nao e possivel excluir uma OS em andamento ou concluida");
        }
        workOrderRepository.delete(entity);
        auditLogService.log("WorkOrder", id, AuditLog.Action.DELETE, "OS excluida: " + entity.getCode());
    }

    public WorkOrderResponse changeStatus(Long id, WorkOrderStatusChangeRequest request) {
        WorkOrder entity = getOrThrow(id);
        WorkOrder.Status current = entity.getStatus();
        WorkOrder.Status target = request.newStatus();

        if (current == target) {
            return toResponse(entity);
        }
        Set<WorkOrder.Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new ValidationException("Transicao de status invalida: " + current + " -> " + target);
        }

        AppUser changedBy = resolveUser(request.changedByUserId());

        if (target == WorkOrder.Status.IN_PROGRESS && entity.getStartedAt() == null) {
            entity.setStartedAt(Instant.now());
        }
        if (target == WorkOrder.Status.DONE) {
            entity.setCompletedAt(Instant.now());
        }
        entity.setStatus(target);
        entity.setUpdatedAt(Instant.now());
        workOrderRepository.save(entity);

        String message = "Status alterado de " + current + " para " + target
                + (request.comment() != null && !request.comment().isBlank() ? ": " + request.comment() : "");
        eventService.record(entity, WorkOrderEvent.EventType.STATUS_CHANGE, message, changedBy);

        return toResponse(entity);
    }

    public WorkOrderResponse assign(Long id, WorkOrderAssignRequest request) {
        WorkOrder entity = getOrThrow(id);
        AppUser assignee = appUserRepository.findById(request.assignedToId())
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + request.assignedToId()));
        entity.setAssignedTo(assignee);
        entity.setUpdatedAt(Instant.now());
        workOrderRepository.save(entity);

        AppUser changedBy = resolveUser(request.changedByUserId());
        eventService.record(entity, WorkOrderEvent.EventType.ASSIGNMENT, "OS atribuida a " + assignee.getName(), changedBy);
        return toResponse(entity);
    }

    public WorkOrderResponse addComment(Long id, WorkOrderCommentRequest request) {
        WorkOrder entity = getOrThrow(id);
        AppUser author = resolveUser(request.createdByUserId());
        eventService.record(entity, WorkOrderEvent.EventType.COMMENT, request.message(), author);
        return toResponse(entity);
    }

    public WorkOrderResponse sign(Long id, WorkOrderSignRequest request) {
        WorkOrder entity = getOrThrow(id);
        if (entity.getStatus() != WorkOrder.Status.DONE) {
            throw new ValidationException("Somente OS concluidas podem ser assinadas");
        }
        entity.setSignedByName(request.signedByName());
        entity.setSignedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        workOrderRepository.save(entity);
        eventService.record(entity, WorkOrderEvent.EventType.SIGNATURE,
                "OS assinada por " + request.signedByName(), null);
        return toResponse(entity);
    }

    public WorkOrderResponse reopen(Long id, String reason, Long requestedByUserId) {
        WorkOrder original = getOrThrow(id);
        if (original.getStatus() != WorkOrder.Status.DONE && original.getStatus() != WorkOrder.Status.CANCELLED) {
            throw new ValidationException("Somente OS concluidas ou canceladas podem ser reabertas");
        }
        long reworkCount = workOrderRepository.findAll().stream()
                .filter(wo -> wo.getReopenedFrom() != null && wo.getReopenedFrom().getId().equals(original.getId()))
                .count();

        WorkOrder rework = new WorkOrder();
        rework.setCode(original.getCode() + "-R" + (reworkCount + 1));
        rework.setAsset(original.getAsset());
        rework.setMaintenancePlan(original.getMaintenancePlan());
        rework.setType(WorkOrder.Type.CORRECTIVE);
        rework.setPriority(original.getPriority());
        rework.setTitle("[Retrabalho] " + original.getTitle());
        rework.setDescription(reason);
        rework.setRequestedBy(resolveUser(requestedByUserId));
        rework.setReopenedFrom(original);
        rework.setStatus(WorkOrder.Status.OPEN);
        rework.setOpenedAt(Instant.now());
        rework.setCreatedAt(Instant.now());
        rework.setUpdatedAt(Instant.now());

        WorkOrder saved = workOrderRepository.save(rework);
        eventService.record(saved, WorkOrderEvent.EventType.STATUS_CHANGE,
                "OS aberta como retrabalho da OS " + original.getCode(), null);
        return toResponse(saved);
    }

    private AppUser resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado: " + userId));
    }

    private void applyRequest(WorkOrder entity, WorkOrderRequest request) {
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + request.assetId()));
        entity.setAsset(asset);

        MaintenancePlan plan = null;
        if (request.maintenancePlanId() != null) {
            plan = maintenancePlanRepository.findById(request.maintenancePlanId())
                    .orElseThrow(() -> new NotFoundException("Plano de manutencao nao encontrado: " + request.maintenancePlanId()));
        }
        entity.setMaintenancePlan(plan);

        entity.setType(request.type());
        entity.setPriority(request.priority());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setRequestedBy(resolveUser(request.requestedById()));
        entity.setAssignedTo(resolveUser(request.assignedToId()));
        entity.setScheduledAt(request.scheduledAt());
        if (entity.getStatus() == null) {
            entity.setStatus(WorkOrder.Status.OPEN);
        }
    }

    WorkOrder getOrThrow(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada: " + id));
    }

    private Long computeExecutionMinutes(WorkOrder entity) {
        if (entity.getStartedAt() == null || entity.getCompletedAt() == null) {
            return null;
        }
        return Duration.between(entity.getStartedAt(), entity.getCompletedAt()).toMinutes();
    }

    private WorkOrderResponse toResponse(WorkOrder entity) {
        return new WorkOrderResponse(
                entity.getId(),
                entity.getCode(),
                entity.getAsset().getId(),
                entity.getAsset().getName(),
                entity.getMaintenancePlan() != null ? entity.getMaintenancePlan().getId() : null,
                entity.getType(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRequestedBy() != null ? entity.getRequestedBy().getId() : null,
                entity.getRequestedBy() != null ? entity.getRequestedBy().getName() : null,
                entity.getAssignedTo() != null ? entity.getAssignedTo().getId() : null,
                entity.getAssignedTo() != null ? entity.getAssignedTo().getName() : null,
                entity.getOpenedAt(),
                entity.getScheduledAt(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                computeExecutionMinutes(entity),
                entity.getSignedByName(),
                entity.getSignedAt(),
                entity.getReopenedFrom() != null ? entity.getReopenedFrom().getId() : null
        );
    }
}
