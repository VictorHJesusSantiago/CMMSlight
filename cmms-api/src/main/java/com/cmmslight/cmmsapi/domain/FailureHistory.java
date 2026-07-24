package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "failure_history")
@Getter
@Setter
@NoArgsConstructor
public class FailureHistory {

    public enum Classification { MECHANICAL, ELECTRICAL, OPERATIONAL, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "downtime_minutes")
    private Integer downtimeMinutes;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Classification classification = Classification.OTHER;

    @Column(name = "why_1", columnDefinition = "TEXT")
    private String why1;

    @Column(name = "why_2", columnDefinition = "TEXT")
    private String why2;

    @Column(name = "why_3", columnDefinition = "TEXT")
    private String why3;

    @Column(name = "why_4", columnDefinition = "TEXT")
    private String why4;

    @Column(name = "why_5", columnDefinition = "TEXT")
    private String why5;
}
