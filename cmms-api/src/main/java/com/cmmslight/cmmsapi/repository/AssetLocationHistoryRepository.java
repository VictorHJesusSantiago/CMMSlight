package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.AssetLocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetLocationHistoryRepository extends JpaRepository<AssetLocationHistory, Long> {

    List<AssetLocationHistory> findByAssetIdOrderByMovedAtDesc(Long assetId);
}
