package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.WorkOrderChecklistResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderChecklistResultRepository extends JpaRepository<WorkOrderChecklistResult, Long> {

    List<WorkOrderChecklistResult> findByWorkOrderId(Long workOrderId);

    Optional<WorkOrderChecklistResult> findByWorkOrderIdAndChecklistItemId(Long workOrderId, Long checklistItemId);
}
