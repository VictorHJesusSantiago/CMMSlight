package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {

    List<MaintenancePlan> findByActiveTrue();

    List<MaintenancePlan> findByAssetId(Long assetId);

    List<MaintenancePlan> findByAssetTypeId(Long assetTypeId);
}
