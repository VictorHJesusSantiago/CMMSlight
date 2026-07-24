package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByCode(String code);

    boolean existsByCode(String code);

    List<Asset> findByParentAssetId(Long parentAssetId);

    List<Asset> findByParentAssetIsNull();

    List<Asset> findByAssetTypeId(Long assetTypeId);

    List<Asset> findByStatus(Asset.Status status);

    List<Asset> findByCriticality(Asset.Criticality criticality);
}
