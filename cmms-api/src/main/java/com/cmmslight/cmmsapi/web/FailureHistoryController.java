package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.AssetReliabilityStats;
import com.cmmslight.cmmsapi.dto.FailureHistoryRequest;
import com.cmmslight.cmmsapi.dto.FailureHistoryResponse;
import com.cmmslight.cmmsapi.service.FailureHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/failures")
public class FailureHistoryController {

    private final FailureHistoryService failureHistoryService;

    public FailureHistoryController(FailureHistoryService failureHistoryService) {
        this.failureHistoryService = failureHistoryService;
    }

    @GetMapping
    public List<FailureHistoryResponse> findAll(@RequestParam(required = false) Long assetId) {
        return assetId != null ? failureHistoryService.findByAsset(assetId) : failureHistoryService.findAll();
    }

    @GetMapping("/{id}")
    public FailureHistoryResponse findById(@PathVariable Long id) {
        return failureHistoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FailureHistoryResponse create(@Valid @RequestBody FailureHistoryRequest request) {
        return failureHistoryService.create(request);
    }

    @PutMapping("/{id}")
    public FailureHistoryResponse update(@PathVariable Long id, @Valid @RequestBody FailureHistoryRequest request) {
        return failureHistoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        failureHistoryService.delete(id);
    }

    @GetMapping("/reliability/ranking")
    public List<AssetReliabilityStats> ranking() {
        return failureHistoryService.reliabilityRanking();
    }

    @GetMapping("/reliability/{assetId}")
    public AssetReliabilityStats reliabilityForAsset(@PathVariable Long assetId) {
        return failureHistoryService.reliabilityForAsset(assetId);
    }
}
