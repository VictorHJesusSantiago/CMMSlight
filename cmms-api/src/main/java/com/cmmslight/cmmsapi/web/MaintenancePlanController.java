package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.MaintenancePlanRequest;
import com.cmmslight.cmmsapi.dto.MaintenancePlanResponse;
import com.cmmslight.cmmsapi.service.MaintenancePlanService;
import com.cmmslight.cmmsapi.service.MaintenanceSchedulerService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance-plans")
public class MaintenancePlanController {

    private final MaintenancePlanService maintenancePlanService;
    private final MaintenanceSchedulerService schedulerService;

    public MaintenancePlanController(MaintenancePlanService maintenancePlanService, MaintenanceSchedulerService schedulerService) {
        this.maintenancePlanService = maintenancePlanService;
        this.schedulerService = schedulerService;
    }

    @GetMapping
    public List<MaintenancePlanResponse> findAll() {
        return maintenancePlanService.findAll();
    }

    @GetMapping("/{id}")
    public MaintenancePlanResponse findById(@PathVariable Long id) {
        return maintenancePlanService.findById(id);
    }

    @GetMapping("/overdue")
    public List<MaintenancePlanResponse> overdue() {
        return maintenancePlanService.findOverdue();
    }

    @GetMapping("/calendar")
    public List<MaintenancePlanResponse> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return maintenancePlanService.calendar(from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenancePlanResponse create(@Valid @RequestBody MaintenancePlanRequest request) {
        return maintenancePlanService.create(request);
    }

    @PutMapping("/{id}")
    public MaintenancePlanResponse update(@PathVariable Long id, @Valid @RequestBody MaintenancePlanRequest request) {
        return maintenancePlanService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        maintenancePlanService.delete(id);
    }

    /** Dispara manualmente o motor de geracao de OS preventivas (a mesma logica do job diario as 02:00). */
    @PostMapping("/generate-due")
    public Map<String, Integer> generateDue() {
        return Map.of("generated", schedulerService.generateDueWorkOrders());
    }
}
