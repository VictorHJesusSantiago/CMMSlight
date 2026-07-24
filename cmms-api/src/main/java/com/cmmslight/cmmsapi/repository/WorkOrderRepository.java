package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByCode(String code);

    boolean existsByCode(String code);

    List<WorkOrder> findByAssetId(Long assetId);

    List<WorkOrder> findByStatus(WorkOrder.Status status);

    List<WorkOrder> findByAssignedToId(Long userId);

    List<WorkOrder> findByMaintenancePlanId(Long maintenancePlanId);

    List<WorkOrder> findByScheduledAtBetween(Instant from, Instant to);

    long countByAssetIdAndStatusIn(Long assetId, List<WorkOrder.Status> statuses);
}
