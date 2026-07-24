package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "maintenance_plan")
@Getter
@Setter
@NoArgsConstructor
public class MaintenancePlan {

    public enum FrequencyType { TIME, USAGE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_template_id")
    private ChecklistTemplate checklistTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 20)
    private FrequencyType frequencyType;

    @Column(name = "frequency_value", nullable = false)
    private int frequencyValue;

    @Column(name = "frequency_unit", length = 20)
    private String frequencyUnit;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_generated_at")
    private Instant lastGeneratedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
