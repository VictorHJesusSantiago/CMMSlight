package com.cmmslight.cmmsapi.repository;

import com.cmmslight.cmmsapi.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
