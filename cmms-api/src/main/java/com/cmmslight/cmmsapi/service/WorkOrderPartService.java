package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Part;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.domain.WorkOrderPart;
import com.cmmslight.cmmsapi.dto.PartConsumptionResponse;
import com.cmmslight.cmmsapi.dto.WorkOrderPartRequest;
import com.cmmslight.cmmsapi.dto.WorkOrderPartResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.PartRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderPartRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkOrderPartService {

    private final WorkOrderPartRepository workOrderPartRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PartRepository partRepository;
    private final WorkOrderEventService eventService;

    public WorkOrderPartService(WorkOrderPartRepository workOrderPartRepository,
                                 WorkOrderRepository workOrderRepository,
                                 PartRepository partRepository,
                                 WorkOrderEventService eventService) {
        this.workOrderPartRepository = workOrderPartRepository;
        this.workOrderRepository = workOrderRepository;
        this.partRepository = partRepository;
        this.eventService = eventService;
    }

    public List<WorkOrderPartResponse> listByWorkOrder(Long workOrderId) {
        return workOrderPartRepository.findByWorkOrderId(workOrderId).stream().map(this::toResponse).toList();
    }

    /** Vincula peca a OS e da baixa automatica no estoque. */
    public WorkOrderPartResponse addPart(Long workOrderId, WorkOrderPartRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada: " + workOrderId));
        Part part = partRepository.findById(request.partId())
                .orElseThrow(() -> new NotFoundException("Peca nao encontrada: " + request.partId()));

        if (part.getQuantityOnHand().compareTo(request.quantityUsed()) < 0) {
            throw new ValidationException("Estoque insuficiente para a peca '" + part.getName()
                    + "' (disponivel: " + part.getQuantityOnHand() + ")");
        }

        WorkOrderPart existing = workOrderPartRepository.findByWorkOrderIdAndPartId(workOrderId, request.partId()).orElse(null);
        WorkOrderPart entity = existing != null ? existing : new WorkOrderPart();
        BigDecimal previousQuantity = existing != null ? existing.getQuantityUsed() : BigDecimal.ZERO;

        entity.setWorkOrder(workOrder);
        entity.setPart(part);
        entity.setQuantityUsed(previousQuantity.add(request.quantityUsed()));
        WorkOrderPart saved = workOrderPartRepository.save(entity);

        part.setQuantityOnHand(part.getQuantityOnHand().subtract(request.quantityUsed()));
        partRepository.save(part);

        eventService.record(workOrder, WorkOrderEvent.EventType.PART_USED,
                "Baixa de " + request.quantityUsed() + " " + part.getUnit() + " de '" + part.getName() + "'", null);

        return toResponse(saved);
    }

    public void removePart(Long workOrderId, Long workOrderPartId) {
        WorkOrderPart entity = workOrderPartRepository.findById(workOrderPartId)
                .orElseThrow(() -> new NotFoundException("Vinculo de peca nao encontrado: " + workOrderPartId));
        Part part = entity.getPart();
        part.setQuantityOnHand(part.getQuantityOnHand().add(entity.getQuantityUsed()));
        partRepository.save(part);
        workOrderPartRepository.delete(entity);
    }

    /** Consumo agregado por ativo e peca, para embasar previsao de substituicao. */
    public List<PartConsumptionResponse> consumptionByPart(Long partId) {
        List<WorkOrderPart> usages = workOrderPartRepository.findByPartId(partId);
        Map<Long, List<WorkOrderPart>> byAsset = usages.stream()
                .collect(Collectors.groupingBy(wop -> wop.getWorkOrder().getAsset().getId()));

        return byAsset.entrySet().stream()
                .map(e -> {
                    var first = e.getValue().get(0);
                    BigDecimal total = e.getValue().stream()
                            .map(WorkOrderPart::getQuantityUsed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new PartConsumptionResponse(
                            e.getKey(),
                            first.getWorkOrder().getAsset().getName(),
                            partId,
                            first.getPart().getName(),
                            total,
                            e.getValue().size()
                    );
                })
                .sorted(Comparator.comparing(PartConsumptionResponse::totalQuantityUsed).reversed())
                .toList();
    }

    private WorkOrderPartResponse toResponse(WorkOrderPart entity) {
        return new WorkOrderPartResponse(
                entity.getId(),
                entity.getWorkOrder().getId(),
                entity.getPart().getId(),
                entity.getPart().getName(),
                entity.getQuantityUsed()
        );
    }
}
