package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.ChecklistItemRequest;
import com.cmmslight.cmmsapi.dto.ChecklistItemResponse;
import com.cmmslight.cmmsapi.dto.ChecklistTemplateRequest;
import com.cmmslight.cmmsapi.dto.ChecklistTemplateResponse;
import com.cmmslight.cmmsapi.service.ChecklistTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklist-templates")
public class ChecklistTemplateController {

    private final ChecklistTemplateService checklistTemplateService;

    public ChecklistTemplateController(ChecklistTemplateService checklistTemplateService) {
        this.checklistTemplateService = checklistTemplateService;
    }

    @GetMapping
    public List<ChecklistTemplateResponse> findAll() {
        return checklistTemplateService.findAll();
    }

    @GetMapping("/{id}")
    public ChecklistTemplateResponse findById(@PathVariable Long id) {
        return checklistTemplateService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChecklistTemplateResponse create(@Valid @RequestBody ChecklistTemplateRequest request) {
        return checklistTemplateService.create(request);
    }

    @PutMapping("/{id}")
    public ChecklistTemplateResponse update(@PathVariable Long id, @Valid @RequestBody ChecklistTemplateRequest request) {
        return checklistTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        checklistTemplateService.delete(id);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ChecklistItemResponse addItem(@PathVariable Long id, @Valid @RequestBody ChecklistItemRequest request) {
        return checklistTemplateService.addItem(id, request);
    }

    @PutMapping("/{id}/items/{itemId}")
    public ChecklistItemResponse updateItem(@PathVariable Long id, @PathVariable Long itemId,
                                             @Valid @RequestBody ChecklistItemRequest request) {
        return checklistTemplateService.updateItem(id, itemId, request);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
        checklistTemplateService.deleteItem(id, itemId);
    }
}
