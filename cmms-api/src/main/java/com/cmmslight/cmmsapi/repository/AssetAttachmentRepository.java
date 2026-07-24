package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.AssetAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetAttachmentRepository extends JpaRepository<AssetAttachment, Long> {

    List<AssetAttachment> findByAssetIdOrderByUploadedAtDesc(Long assetId);
}
