package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.domain.WorkOrderAttachment;
import com.cmmslight.cmmsapi.dto.WorkOrderAttachmentResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.WorkOrderAttachmentRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import com.cmmslight.cmmsapi.service.storage.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.util.List;

@Service
@Transactional
public class WorkOrderAttachmentService {

    private static final String SUB_FOLDER = "work-orders";

    private final WorkOrderAttachmentRepository attachmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final FileStorageService fileStorageService;

    public WorkOrderAttachmentService(WorkOrderAttachmentRepository attachmentRepository,
                                       WorkOrderRepository workOrderRepository,
                                       FileStorageService fileStorageService) {
        this.attachmentRepository = attachmentRepository;
        this.workOrderRepository = workOrderRepository;
        this.fileStorageService = fileStorageService;
    }

    public WorkOrderAttachmentResponse upload(Long workOrderId, MultipartFile file, WorkOrderAttachment.Category category) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada: " + workOrderId));

        String storedPath = fileStorageService.store(SUB_FOLDER, file);

        WorkOrderAttachment attachment = new WorkOrderAttachment();
        attachment.setWorkOrder(workOrder);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setStoredPath(storedPath);
        attachment.setContentType(file.getContentType());
        attachment.setSizeBytes(file.getSize());
        attachment.setCategory(category != null ? category : WorkOrderAttachment.Category.OTHER);

        return toResponse(attachmentRepository.save(attachment));
    }

    public List<WorkOrderAttachmentResponse> listByWorkOrder(Long workOrderId) {
        return attachmentRepository.findByWorkOrderIdOrderByUploadedAtDesc(workOrderId).stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkOrderAttachment getOrThrow(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Anexo nao encontrado: " + attachmentId));
    }

    public Resource loadAsResource(Long attachmentId) {
        WorkOrderAttachment attachment = getOrThrow(attachmentId);
        try {
            Resource resource = new UrlResource(fileStorageService.resolve(attachment.getStoredPath()).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("Arquivo do anexo nao encontrado em disco: " + attachmentId);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void delete(Long attachmentId) {
        WorkOrderAttachment attachment = getOrThrow(attachmentId);
        fileStorageService.delete(attachment.getStoredPath());
        attachmentRepository.delete(attachment);
    }

    private WorkOrderAttachmentResponse toResponse(WorkOrderAttachment attachment) {
        return new WorkOrderAttachmentResponse(
                attachment.getId(),
                attachment.getWorkOrder().getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getCategory(),
                attachment.getUploadedAt(),
                "/api/work-orders/" + attachment.getWorkOrder().getId() + "/attachments/" + attachment.getId() + "/download"
        );
    }
}
