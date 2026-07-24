package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.WorkOrderPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderPartRepository extends JpaRepository<WorkOrderPart, Long> {

    List<WorkOrderPart> findByWorkOrderId(Long workOrderId);

    Optional<WorkOrderPart> findByWorkOrderIdAndPartId(Long workOrderId, Long partId);

    List<WorkOrderPart> findByPartId(Long partId);
}
