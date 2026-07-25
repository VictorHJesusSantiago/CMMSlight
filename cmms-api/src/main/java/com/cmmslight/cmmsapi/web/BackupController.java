package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.service.BackupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping
    public List<String> list() {
        return backupService.listBackups();
    }

    @PostMapping
    public Map<String, String> runBackup() {
        return Map.of("file", backupService.runBackup());
    }

    @PostMapping("/{fileName}/restore")
    public Map<String, String> restore(@PathVariable String fileName) {
        backupService.restore(fileName);
        return Map.of("restored", fileName);
    }
}
