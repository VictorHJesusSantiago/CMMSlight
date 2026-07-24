package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.SensorAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorAlertRepository extends JpaRepository<SensorAlert, Long> {

    Optional<SensorAlert> findBySensorReadingIdAndThresholdRuleId(Long sensorReadingId, Long thresholdRuleId);
}
