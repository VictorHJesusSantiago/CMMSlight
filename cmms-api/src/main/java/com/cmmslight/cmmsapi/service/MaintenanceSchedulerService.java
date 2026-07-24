package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.MaintenancePlan;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.MaintenancePlanRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Motor de geracao automatica de OS preventivas a partir dos planos de manutencao baseados em tempo.
 * Roda localmente via @Scheduled (sem dependencia de servico externo de agendamento).
 */
@Service
public class MaintenanceSchedulerService {

    private static final List<WorkOrder.Status> OPEN_STATUSES = List.of(
            WorkOrder.Status.OPEN, WorkOrder.Status.SCHEDULED, WorkOrder.Status.IN_PROGRESS);

    private final MaintenancePlanRepository maintenancePlanRepository;
    private final MaintenancePlanService maintenancePlanService;
    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderEventService eventService;

    public MaintenanceSchedulerService(MaintenancePlanRepository maintenancePlanRepository,
                                        MaintenancePlanService maintenancePlanService,
                                        AssetRepository assetRepository,
                                        WorkOrderRepository workOrderRepository,
                                        WorkOrderEventService eventService) {
        this.maintenancePlanRepository = maintenancePlanRepository;
        this.maintenancePlanService = maintenancePlanService;
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
        this.eventService = eventService;
    }

    /** Roda todo dia as 02:00 (horario do servidor). */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void generateDueWorkOrdersScheduled() {
        generateDueWorkOrders();
    }

    /** Disparo manual (ex: endpoint de teste) da mesma logica do job agendado. */
    @Transactional
    public int generateDueWorkOrders() {
        Instant now = Instant.now();
        int generated = 0;
        for (MaintenancePlan plan : maintenancePlanRepository.findByActiveTrue()) {
            if (plan.getFrequencyType() != MaintenancePlan.FrequencyType.TIME) {
                continue;
            }
            Instant dueAt = maintenancePlanService.computeNextDueAt(plan);
            if (dueAt == null || dueAt.isAfter(now)) {
                continue;
            }
            List<Asset> targets = resolveTargetAssets(plan);
            boolean anyGenerated = false;
            for (Asset asset : targets) {
                if (hasOpenPreventiveWorkOrder(plan, asset)) {
                    continue;
                }
                createPreventiveWorkOrder(plan, asset, dueAt);
                generated++;
                anyGenerated = true;
            }
            if (anyGenerated || targets.isEmpty()) {
                plan.setLastGeneratedAt(now);
                maintenancePlanRepository.save(plan);
            }
        }
        return generated;
    }

    private List<Asset> resolveTargetAssets(MaintenancePlan plan) {
        if (plan.getAsset() != null) {
            return List.of(plan.getAsset());
        }
        if (plan.getAssetType() != null) {
            return assetRepository.findByAssetTypeId(plan.getAssetType().getId());
        }
        return List.of();
    }

    private boolean hasOpenPreventiveWorkOrder(MaintenancePlan plan, Asset asset) {
        return workOrderRepository.findByMaintenancePlanId(plan.getId()).stream()
                .anyMatch(wo -> wo.getAsset().getId().equals(asset.getId()) && OPEN_STATUSES.contains(wo.getStatus()));
    }

    private void createPreventiveWorkOrder(MaintenancePlan plan, Asset asset, Instant dueAt) {
        WorkOrder wo = new WorkOrder();
        wo.setCode("PM-" + plan.getId() + "-" + asset.getId() + "-" + System.currentTimeMillis());
        wo.setAsset(asset);
        wo.setMaintenancePlan(plan);
        wo.setType(WorkOrder.Type.PREVENTIVE);
        wo.setPriority(mapCriticalityToPriority(asset.getCriticality()));
        wo.setTitle("Manutencao preventiva: " + plan.getName());
        wo.setDescription("OS gerada automaticamente pelo plano de manutencao '" + plan.getName() + "'");
        wo.setStatus(WorkOrder.Status.SCHEDULED);
        wo.setScheduledAt(dueAt);
        wo.setOpenedAt(Instant.now());
        wo.setCreatedAt(Instant.now());
        wo.setUpdatedAt(Instant.now());
        WorkOrder saved = workOrderRepository.save(wo);
        eventService.record(saved, WorkOrderEvent.EventType.STATUS_CHANGE,
                "OS gerada automaticamente pelo motor de manutencao preventiva", null);
    }

    private WorkOrder.Priority mapCriticalityToPriority(Asset.Criticality criticality) {
        return switch (criticality) {
            case CRITICAL -> WorkOrder.Priority.URGENT;
            case HIGH -> WorkOrder.Priority.HIGH;
            case MEDIUM -> WorkOrder.Priority.MEDIUM;
            case LOW -> WorkOrder.Priority.LOW;
        };
    }
}
