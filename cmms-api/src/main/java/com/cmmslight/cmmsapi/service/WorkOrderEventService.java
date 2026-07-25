package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.AppUser;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import com.cmmslight.cmmsapi.dto.WorkOrderEventResponse;
import com.cmmslight.cmmsapi.repository.WorkOrderEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class WorkOrderEventService {

    private final WorkOrderEventRepository eventRepository;

    public WorkOrderEventService(WorkOrderEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public WorkOrderEvent record(WorkOrder workOrder, WorkOrderEvent.EventType type, String message, AppUser createdBy) {
        WorkOrderEvent event = new WorkOrderEvent();
        event.setWorkOrder(workOrder);
        event.setEventType(type);
        event.setMessage(message);
        event.setCreatedBy(createdBy);
        event.setCreatedAt(Instant.now());
        return eventRepository.save(event);
    }

    public List<WorkOrderEventResponse> timeline(Long workOrderId) {
        return eventRepository.findByWorkOrderIdOrderByCreatedAtAsc(workOrderId).stream()
                .map(this::toResponse)
                .toList();
    }

    private WorkOrderEventResponse toResponse(WorkOrderEvent event) {
        return new WorkOrderEventResponse(
                event.getId(),
                event.getWorkOrder().getId(),
                event.getEventType(),
                event.getMessage(),
                event.getCreatedBy() != null ? event.getCreatedBy().getId() : null,
                event.getCreatedBy() != null ? event.getCreatedBy().getName() : null,
                event.getCreatedAt()
        );
    }
}
