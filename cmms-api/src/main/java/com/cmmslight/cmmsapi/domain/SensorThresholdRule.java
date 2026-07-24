package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sensor_threshold_rule")
@Getter
@Setter
@NoArgsConstructor
public class SensorThresholdRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    @Column(name = "sensor_type", nullable = false, length = 50)
    private String sensorType;

    @Column(name = "min_value", precision = 14, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 14, scale = 4)
    private BigDecimal maxValue;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
