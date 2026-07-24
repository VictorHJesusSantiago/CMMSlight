package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.SensorThresholdRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorThresholdRuleRepository extends JpaRepository<SensorThresholdRule, Long> {

    List<SensorThresholdRule> findByActiveTrue();

    List<SensorThresholdRule> findByAssetIdAndSensorTypeAndActiveTrue(Long assetId, String sensorType);

    List<SensorThresholdRule> findByAssetTypeIdAndSensorTypeAndActiveTrue(Long assetTypeId, String sensorType);
}
