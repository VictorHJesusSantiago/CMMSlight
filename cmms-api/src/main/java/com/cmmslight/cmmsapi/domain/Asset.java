package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "asset")
@Getter
@Setter
@NoArgsConstructor
public class Asset {

    public enum Status { ACTIVE, INACTIVE, DECOMMISSIONED, UNDER_MAINTENANCE }
    public enum Criticality { LOW, MEDIUM, HIGH, CRITICAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_asset_id")
    private Asset parentAsset;

    @Column(length = 150)
    private String location;

    @Column(length = 150)
    private String manufacturer;

    @Column(length = 150)
    private String model;

    @Column(name = "serial_number", length = 150)
    private String serialNumber;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Criticality criticality = Criticality.MEDIUM;

    @Column(name = "warranty_provider", length = 150)
    private String warrantyProvider;

    @Column(name = "warranty_expiration")
    private LocalDate warrantyExpiration;

    @Column(name = "warranty_terms", columnDefinition = "TEXT")
    private String warrantyTerms;

    @Column(name = "estimated_lifespan_months")
    private Integer estimatedLifespanMonths;

    @Column(name = "acquisition_cost", precision = 14, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    /** Valores dos atributos customizados declarados em AssetType.customAttributesSchema, ex: {"tensao": 220}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    private String customAttributes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
