package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.domain.AssetAttachment;
import com.cmmslight.cmmsapi.dto.AssetAttachmentResponse;
import com.cmmslight.cmmsapi.service.AssetAttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assets/{assetId}/attachments")
public class AssetAttachmentController {

    private final AssetAttachmentService attachmentService;

    public AssetAttachmentController(AssetAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<AssetAttachmentResponse> list(@PathVariable Long assetId) {
        return attachmentService.listByAsset(assetId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AssetAttachmentResponse upload(@PathVariable Long assetId,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "category", required = false) AssetAttachment.Category category) {
        return attachmentService.upload(assetId, file, category);
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long assetId, @PathVariable Long attachmentId) {
        AssetAttachment attachment = attachmentService.getOrThrow(attachmentId);
        Resource resource = attachmentService.loadAsResource(attachmentId);
        String contentType = attachment.getContentType() != null ? attachment.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long assetId, @PathVariable Long attachmentId) {
        attachmentService.delete(attachmentId);
    }
}
