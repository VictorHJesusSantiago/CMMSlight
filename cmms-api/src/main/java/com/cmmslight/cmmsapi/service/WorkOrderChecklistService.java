package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.ChecklistItem;
import com.cmmslight.cmmsapi.domain.ChecklistTemplate;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderChecklistResult;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.dto.ChecklistAnswerRequest;
import com.cmmslight.cmmsapi.dto.ChecklistComplianceResponse;
import com.cmmslight.cmmsapi.dto.WorkOrderChecklistResultResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.exception.ValidationException;
import com.cmmslight.cmmsapi.repository.ChecklistItemRepository;
import com.cmmslight.cmmsapi.repository.ChecklistTemplateRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderChecklistResultRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class WorkOrderChecklistService {

    private final WorkOrderChecklistResultRepository resultRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ChecklistTemplateRepository checklistTemplateRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final WorkOrderEventService eventService;

    public WorkOrderChecklistService(WorkOrderChecklistResultRepository resultRepository,
                                      WorkOrderRepository workOrderRepository,
                                      ChecklistTemplateRepository checklistTemplateRepository,
                                      ChecklistItemRepository checklistItemRepository,
                                      WorkOrderEventService eventService) {
        this.resultRepository = resultRepository;
        this.workOrderRepository = workOrderRepository;
        this.checklistTemplateRepository = checklistTemplateRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.eventService = eventService;
    }

    /** Vincula um template de checklist a uma OS, criando os itens pendentes de resposta. */
    public List<WorkOrderChecklistResultResponse> initialize(Long workOrderId, Long checklistTemplateId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada: " + workOrderId));
        ChecklistTemplate template = checklistTemplateRepository.findById(checklistTemplateId)
                .orElseThrow(() -> new NotFoundException("Template de checklist nao encontrado: " + checklistTemplateId));

        List<ChecklistItem> items = checklistItemRepository.findByChecklistTemplateIdOrderBySortOrderAsc(template.getId());
        for (ChecklistItem item : items) {
            boolean alreadyExists = resultRepository.findByWorkOrderIdAndChecklistItemId(workOrderId, item.getId()).isPresent();
            if (!alreadyExists) {
                WorkOrderChecklistResult result = new WorkOrderChecklistResult();
                result.setWorkOrder(workOrder);
                result.setChecklistItem(item);
                result.setCompleted(false);
                resultRepository.save(result);
            }
        }
        eventService.record(workOrder, WorkOrderEvent.EventType.CHECKLIST,
                "Checklist '" + template.getName() + "' vinculado a OS", null);
        return listResults(workOrderId);
    }

    public List<WorkOrderChecklistResultResponse> listResults(Long workOrderId) {
        return resultRepository.findByWorkOrderId(workOrderId).stream().map(this::toResponse).toList();
    }

    public WorkOrderChecklistResultResponse answer(Long workOrderId, ChecklistAnswerRequest request) {
        WorkOrderChecklistResult result = resultRepository.findByWorkOrderIdAndChecklistItemId(workOrderId, request.checklistItemId())
                .orElseThrow(() -> new NotFoundException("Item de checklist nao iniciado para esta OS: " + request.checklistItemId()));

        ChecklistItem item = result.getChecklistItem();
        validateValue(item, request.value());

        result.setValue(request.value());
        result.setNotes(request.notes());
        result.setCompleted(request.value() != null && !request.value().isBlank());
        WorkOrderChecklistResult saved = resultRepository.save(result);

        eventService.record(result.getWorkOrder(), WorkOrderEvent.EventType.CHECKLIST,
                "Item respondido: " + item.getDescription() + " = " + request.value(), null);

        return toResponse(saved);
    }

    public ChecklistComplianceResponse compliance(Long workOrderId) {
        List<WorkOrderChecklistResult> results = resultRepository.findByWorkOrderId(workOrderId);
        int total = results.size();
        int answered = (int) results.stream().filter(WorkOrderChecklistResult::isCompleted).count();
        int compliant = (int) results.stream().filter(this::isCompliant).count();
        BigDecimal percentage = total == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(compliant).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return new ChecklistComplianceResponse(workOrderId, total, answered, compliant, percentage);
    }

    private boolean isCompliant(WorkOrderChecklistResult result) {
        if (!result.isCompleted()) {
            return false;
        }
        if (result.getChecklistItem().getItemType() == ChecklistItem.ItemType.YES_NO) {
            return "true".equalsIgnoreCase(result.getValue());
        }
        return true;
    }

    private void validateValue(ChecklistItem item, String value) {
        if (item.isRequired() && (value == null || value.isBlank())) {
            throw new ValidationException("Item obrigatorio sem resposta: " + item.getDescription());
        }
        if (value == null || value.isBlank()) {
            return;
        }
        switch (item.getItemType()) {
            case YES_NO -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new ValidationException("Valor invalido para item sim/nao: " + item.getDescription());
                }
            }
            case NUMBER -> {
                if (!value.matches("-?\\d+(\\.\\d+)?")) {
                    throw new ValidationException("Valor invalido para item numerico: " + item.getDescription());
                }
            }
            case MULTIPLE_CHOICE -> {
            }
            case TEXT -> {
            }
        }
    }

    private WorkOrderChecklistResultResponse toResponse(WorkOrderChecklistResult result) {
        return new WorkOrderChecklistResultResponse(
                result.getId(),
                result.getWorkOrder().getId(),
                result.getChecklistItem().getId(),
                result.getChecklistItem().getDescription(),
                result.isCompleted(),
                result.getValue(),
                result.getNotes()
        );
    }
}
