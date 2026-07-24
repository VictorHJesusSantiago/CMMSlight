package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.WorkOrderAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderAttachmentRepository extends JpaRepository<WorkOrderAttachment, Long> {

    List<WorkOrderAttachment> findByWorkOrderIdOrderByUploadedAtDesc(Long workOrderId);
}
