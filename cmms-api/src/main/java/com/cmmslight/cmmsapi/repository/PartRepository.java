package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {

    Optional<Part> findByCode(String code);

    boolean existsByCode(String code);

    @Query("select p from Part p where p.quantityOnHand < p.minQuantity")
    List<Part> findBelowMinimum();

    List<Part> findBySupplierId(Long supplierId);
}
