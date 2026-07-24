package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sensor_reading")
@Getter
@Setter
@NoArgsConstructor
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "sensor_type", nullable = false, length = 50)
    private String sensorType;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal value;

    @Column(length = 20)
    private String unit;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();
}
