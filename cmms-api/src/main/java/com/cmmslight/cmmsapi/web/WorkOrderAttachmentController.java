package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.domain.WorkOrderAttachment;
import com.cmmslight.cmmsapi.dto.WorkOrderAttachmentResponse;
import com.cmmslight.cmmsapi.service.WorkOrderAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders/{workOrderId}/attachments")
public class WorkOrderAttachmentController {

    private final WorkOrderAttachmentService attachmentService;

    public WorkOrderAttachmentController(WorkOrderAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<WorkOrderAttachmentResponse> list(@PathVariable Long workOrderId) {
        return attachmentService.listByWorkOrder(workOrderId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderAttachmentResponse upload(@PathVariable Long workOrderId,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "category", required = false) WorkOrderAttachment.Category category) {
        return attachmentService.upload(workOrderId, file, category);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long workOrderId, @PathVariable Long attachmentId) {
        WorkOrderAttachment attachment = attachmentService.getOrThrow(attachmentId);
        Resource resource = attachmentService.loadAsResource(attachmentId);
        String contentType = attachment.getContentType() != null ? attachment.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long workOrderId, @PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
    }
}
