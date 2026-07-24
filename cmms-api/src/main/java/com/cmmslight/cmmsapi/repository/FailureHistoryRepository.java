package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.FailureHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailureHistoryRepository extends JpaRepository<FailureHistory, Long> {

    List<FailureHistory> findByAssetIdOrderByFailedAtDesc(Long assetId);

    List<FailureHistory> findAllByOrderByFailedAtAsc();
}
