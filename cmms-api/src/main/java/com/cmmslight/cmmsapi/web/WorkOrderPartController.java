package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.WorkOrderPartRequest;
import com.cmmslight.cmmsapi.dto.WorkOrderPartResponse;
import com.cmmslight.cmmsapi.service.WorkOrderPartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}/parts")
public class WorkOrderPartController {

    private final WorkOrderPartService workOrderPartService;

    public WorkOrderPartController(WorkOrderPartService workOrderPartService) {
        this.workOrderPartService = workOrderPartService;
    }

    @GetMapping
    public List<WorkOrderPartResponse> list(@PathVariable Long workOrderId) {
        return workOrderPartService.listByWorkOrder(workOrderId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderPartResponse addPart(@PathVariable Long workOrderId, @Valid @RequestBody WorkOrderPartRequest request) {
        return workOrderPartService.addPart(workOrderId, request);
    }

    @DeleteMapping("/{workOrderPartId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePart(@PathVariable Long workOrderId, @PathVariable Long workOrderPartId) {
        workOrderPartService.removePart(workOrderId, workOrderPartId);
    }
}
