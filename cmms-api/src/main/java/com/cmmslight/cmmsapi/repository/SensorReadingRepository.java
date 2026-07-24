package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    List<SensorReading> findByAssetIdAndSensorTypeOrderByRecordedAtAsc(Long assetId, String sensorType);

    List<SensorReading> findByAssetIdOrderByRecordedAtDesc(Long assetId);

    List<SensorReading> findByAssetIdAndRecordedAtBetween(Long assetId, Instant from, Instant to);
}
