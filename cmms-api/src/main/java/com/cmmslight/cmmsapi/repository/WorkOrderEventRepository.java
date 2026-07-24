package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.WorkOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderEventRepository extends JpaRepository<WorkOrderEvent, Long> {

    List<WorkOrderEvent> findByWorkOrderIdOrderByCreatedAtAsc(Long workOrderId);
}
