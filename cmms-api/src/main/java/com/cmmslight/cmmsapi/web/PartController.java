package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.PartConsumptionResponse;
import com.cmmslight.cmmsapi.dto.PartRequest;
import com.cmmslight.cmmsapi.dto.PartResponse;
import com.cmmslight.cmmsapi.service.PartService;
import com.cmmslight.cmmsapi.service.WorkOrderPartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final PartService partService;
    private final WorkOrderPartService workOrderPartService;

    public PartController(PartService partService, WorkOrderPartService workOrderPartService) {
        this.partService = partService;
        this.workOrderPartService = workOrderPartService;
    }

    @GetMapping
    public List<PartResponse> findAll() {
        return partService.findAll();
    }

    @GetMapping("/{id}")
    public PartResponse findById(@PathVariable Long id) {
        return partService.findById(id);
    }

    @GetMapping("/below-minimum")
    public List<PartResponse> belowMinimum() {
        return partService.findBelowMinimum();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartResponse create(@Valid @RequestBody PartRequest request) {
        return partService.create(request);
    }

    @PutMapping("/{id}")
    public PartResponse update(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        return partService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        partService.delete(id);
    }

    @GetMapping("/{id}/consumption")
    public List<PartConsumptionResponse> consumption(@PathVariable Long id) {
        return workOrderPartService.consumptionByPart(id);
    }
}
