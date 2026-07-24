package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "sensor_alert")
@Getter
@Setter
@NoArgsConstructor
public class SensorAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_reading_id", nullable = false)
    private SensorReading sensorReading;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "threshold_rule_id", nullable = false)
    private SensorThresholdRule thresholdRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt = Instant.now();
}
