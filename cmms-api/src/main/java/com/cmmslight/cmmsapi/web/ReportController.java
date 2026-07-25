package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.service.ReportService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<String> list() {
        return reportService.listReports();
    }

    @PostMapping("/daily-summary")
    public Map<String, String> generate() {
        return Map.of("file", reportService.generateDailySummary());
    }

    @GetMapping("/{fileName}/download")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        try {
            Resource resource = new UrlResource(reportService.resolve(fileName).toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
