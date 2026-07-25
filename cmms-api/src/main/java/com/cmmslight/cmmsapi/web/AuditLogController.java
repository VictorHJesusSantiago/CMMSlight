package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.AuditLogResponse;
import com.cmmslight.cmmsapi.service.AuditLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> findAll(@RequestParam(required = false) String entityName,
                                           @RequestParam(required = false) Long entityId) {
        if (entityName != null && entityId != null) {
            return auditLogService.findByEntity(entityName, entityId);
        }
        return auditLogService.findAll();
    }
}
