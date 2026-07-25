package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.ChecklistAnswerRequest;
import com.cmmslight.cmmsapi.dto.ChecklistComplianceResponse;
import com.cmmslight.cmmsapi.dto.WorkOrderChecklistResultResponse;
import com.cmmslight.cmmsapi.service.WorkOrderChecklistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}/checklist")
public class WorkOrderChecklistController {

    private final WorkOrderChecklistService checklistService;

    public WorkOrderChecklistController(WorkOrderChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping("/init")
    public List<WorkOrderChecklistResultResponse> initialize(@PathVariable Long workOrderId, @RequestBody Map<String, Long> body) {
        return checklistService.initialize(workOrderId, body.get("checklistTemplateId"));
    }

    @GetMapping
    public List<WorkOrderChecklistResultResponse> list(@PathVariable Long workOrderId) {
        return checklistService.listResults(workOrderId);
    }

    @PostMapping("/answers")
    public WorkOrderChecklistResultResponse answer(@PathVariable Long workOrderId, @Valid @RequestBody ChecklistAnswerRequest request) {
        return checklistService.answer(workOrderId, request);
    }

    @GetMapping("/compliance")
    public ChecklistComplianceResponse compliance(@PathVariable Long workOrderId) {
        return checklistService.compliance(workOrderId);
    }
}
