package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.SensorThresholdRuleRequest;
import com.cmmslight.cmmsapi.dto.SensorThresholdRuleResponse;
import com.cmmslight.cmmsapi.service.SensorThresholdRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensor-threshold-rules")
public class SensorThresholdRuleController {

    private final SensorThresholdRuleService thresholdRuleService;

    public SensorThresholdRuleController(SensorThresholdRuleService thresholdRuleService) {
        this.thresholdRuleService = thresholdRuleService;
    }

    @GetMapping
    public List<SensorThresholdRuleResponse> findAll() {
        return thresholdRuleService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorThresholdRuleResponse create(@Valid @RequestBody SensorThresholdRuleRequest request) {
        return thresholdRuleService.create(request);
    }

    @PutMapping("/{id}")
    public SensorThresholdRuleResponse update(@PathVariable Long id, @Valid @RequestBody SensorThresholdRuleRequest request) {
        return thresholdRuleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        thresholdRuleService.delete(id);
    }
}
