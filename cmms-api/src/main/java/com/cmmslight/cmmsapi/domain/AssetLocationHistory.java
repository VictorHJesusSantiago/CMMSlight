package com.cmmslight.cmmsapi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "asset_location_history")
@Getter
@Setter
@NoArgsConstructor
public class AssetLocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "previous_location", length = 150)
    private String previousLocation;

    @Column(name = "new_location", nullable = false, length = 150)
    private String newLocation;

    @Column(name = "moved_at", nullable = false)
    private Instant movedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moved_by_id")
    private AppUser movedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
