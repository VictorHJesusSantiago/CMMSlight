package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {

    Optional<AssetType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
