package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.AssetAttachment;
import com.cmmslight.cmmsapi.dto.AssetAttachmentResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.AssetAttachmentRepository;
import com.cmmslight.cmmsapi.repository.AssetRepository;
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
public class AssetAttachmentService {

    private static final String SUB_FOLDER = "assets";

    private final AssetAttachmentRepository attachmentRepository;
    private final AssetRepository assetRepository;
    private final FileStorageService fileStorageService;

    public AssetAttachmentService(AssetAttachmentRepository attachmentRepository,
                                   AssetRepository assetRepository,
                                   FileStorageService fileStorageService) {
        this.attachmentRepository = attachmentRepository;
        this.assetRepository = assetRepository;
        this.fileStorageService = fileStorageService;
    }

    public AssetAttachmentResponse upload(Long assetId, MultipartFile file, AssetAttachment.Category category) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + assetId));

        String storedPath = fileStorageService.store(SUB_FOLDER, file);

        AssetAttachment attachment = new AssetAttachment();
        attachment.setAsset(asset);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setStoredPath(storedPath);
        attachment.setContentType(file.getContentType());
        attachment.setSizeBytes(file.getSize());
        attachment.setCategory(category != null ? category : AssetAttachment.Category.OTHER);

        return toResponse(attachmentRepository.save(attachment));
    }

    public List<AssetAttachmentResponse> listByAsset(Long assetId) {
        return attachmentRepository.findByAssetIdOrderByUploadedAtDesc(assetId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Resource loadAsResource(Long attachmentId) {
        AssetAttachment attachment = getOrThrow(attachmentId);
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

    public AssetAttachment getOrThrow(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Anexo nao encontrado: " + attachmentId));
    }

    public void delete(Long attachmentId) {
        AssetAttachment attachment = getOrThrow(attachmentId);
        fileStorageService.delete(attachment.getStoredPath());
        attachmentRepository.delete(attachment);
    }

    private AssetAttachmentResponse toResponse(AssetAttachment attachment) {
        return new AssetAttachmentResponse(
                attachment.getId(),
                attachment.getAsset().getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getCategory(),
                attachment.getUploadedAt(),
                "/api/assets/" + attachment.getAsset().getId() + "/attachments/" + attachment.getId() + "/download"
        );
    }
}
