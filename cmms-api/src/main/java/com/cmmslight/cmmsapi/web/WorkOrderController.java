package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.dto.*;
import com.cmmslight.cmmsapi.service.WorkOrderEventService;
import com.cmmslight.cmmsapi.service.WorkOrderPdfService;
import com.cmmslight.cmmsapi.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderEventService eventService;
    private final WorkOrderPdfService pdfService;

    public WorkOrderController(WorkOrderService workOrderService, WorkOrderEventService eventService, WorkOrderPdfService pdfService) {
        this.workOrderService = workOrderService;
        this.eventService = eventService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public List<WorkOrderResponse> findAll(@RequestParam(required = false) WorkOrder.Status status,
                                            @RequestParam(required = false) Long assetId) {
        if (status != null) {
            return workOrderService.findByStatus(status);
        }
        if (assetId != null) {
            return workOrderService.findByAsset(assetId);
        }
        return workOrderService.findAll();
    }

    @GetMapping(params = "queue")
    public List<WorkOrderResponse> queue() {
        return workOrderService.findQueueOrderedByPriority();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse findById(@PathVariable Long id) {
        return workOrderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request) {
        return workOrderService.create(request);
    }

    @PutMapping("/{id}")
    public WorkOrderResponse update(@PathVariable Long id, @Valid @RequestBody WorkOrderRequest request) {
        return workOrderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workOrderService.delete(id);
    }

    @PostMapping("/{id}/status")
    public WorkOrderResponse changeStatus(@PathVariable Long id, @Valid @RequestBody WorkOrderStatusChangeRequest request) {
        return workOrderService.changeStatus(id, request);
    }

    @PostMapping("/{id}/assign")
    public WorkOrderResponse assign(@PathVariable Long id, @Valid @RequestBody WorkOrderAssignRequest request) {
        return workOrderService.assign(id, request);
    }

    @PostMapping("/{id}/comments")
    public WorkOrderResponse addComment(@PathVariable Long id, @Valid @RequestBody WorkOrderCommentRequest request) {
        return workOrderService.addComment(id, request);
    }

    @GetMapping("/{id}/timeline")
    public List<WorkOrderEventResponse> timeline(@PathVariable Long id) {
        return eventService.timeline(id);
    }

    @PostMapping("/{id}/sign")
    public WorkOrderResponse sign(@PathVariable Long id, @Valid @RequestBody WorkOrderSignRequest request) {
        return workOrderService.sign(id, request);
    }

    @PostMapping("/{id}/reopen")
    public WorkOrderResponse reopen(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reason = (String) body.get("reason");
        Long requestedByUserId = body.get("requestedByUserId") != null
                ? Long.valueOf(body.get("requestedByUserId").toString())
                : null;
        return workOrderService.reopen(id, reason, requestedByUserId);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] pdf(@PathVariable Long id) {
        return pdfService.generate(id);
    }
}
