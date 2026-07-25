package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.SensorReadingRequest;
import com.cmmslight.cmmsapi.dto.SensorReadingResponse;
import com.cmmslight.cmmsapi.dto.SensorTrendResponse;
import com.cmmslight.cmmsapi.service.PredictiveAnalysisService;
import com.cmmslight.cmmsapi.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets/{assetId}/sensor-readings")
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;
    private final PredictiveAnalysisService predictiveAnalysisService;

    public SensorReadingController(SensorReadingService sensorReadingService, PredictiveAnalysisService predictiveAnalysisService) {
        this.sensorReadingService = sensorReadingService;
        this.predictiveAnalysisService = predictiveAnalysisService;
    }

    @GetMapping
    public List<SensorReadingResponse> list(@PathVariable Long assetId) {
        return sensorReadingService.findByAsset(assetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorReadingResponse create(@PathVariable Long assetId, @Valid @RequestBody SensorReadingRequest request) {
        return sensorReadingService.create(request);
    }

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Integer> importCsv(@PathVariable Long assetId, @RequestParam("file") MultipartFile file) {
        return Map.of("imported", sensorReadingService.importCsv(assetId, file));
    }

    @GetMapping("/trend")
    public SensorTrendResponse trend(@PathVariable Long assetId, @RequestParam String sensorType) {
        return predictiveAnalysisService.trend(assetId, sensorType);
    }
}
