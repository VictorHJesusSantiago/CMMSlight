package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Part;
import com.cmmslight.cmmsapi.domain.Supplier;
import com.cmmslight.cmmsapi.dto.PartRequest;
import com.cmmslight.cmmsapi.dto.PartResponse;
import com.cmmslight.cmmsapi.exception.ConflictException;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.PartRepository;
import com.cmmslight.cmmsapi.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PartService {

    private final PartRepository partRepository;
    private final SupplierRepository supplierRepository;

    public PartService(PartRepository partRepository, SupplierRepository supplierRepository) {
        this.partRepository = partRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<PartResponse> findAll() {
        return partRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PartResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<PartResponse> findBelowMinimum() {
        return partRepository.findBelowMinimum().stream().map(this::toResponse).toList();
    }

    public PartResponse create(PartRequest request) {
        if (partRepository.existsByCode(request.code())) {
            throw new ConflictException("Ja existe uma peca com o codigo '" + request.code() + "'");
        }
        Part entity = new Part();
        entity.setCode(request.code());
        applyRequest(entity, request);
        return toResponse(partRepository.save(entity));
    }

    public PartResponse update(Long id, PartRequest request) {
        Part entity = getOrThrow(id);
        partRepository.findByCode(request.code()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Ja existe uma peca com o codigo '" + request.code() + "'");
            }
        });
        entity.setCode(request.code());
        applyRequest(entity, request);
        return toResponse(partRepository.save(entity));
    }

    public void delete(Long id) {
        partRepository.delete(getOrThrow(id));
    }

    Part getOrThrow(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Peca nao encontrada: " + id));
    }

    private void applyRequest(Part entity, PartRequest request) {
        entity.setName(request.name());
        entity.setUnit(request.unit() != null ? request.unit() : "UN");
        entity.setQuantityOnHand(request.quantityOnHand());
        entity.setMinQuantity(request.minQuantity());

        Supplier supplier = null;
        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new NotFoundException("Fornecedor nao encontrado: " + request.supplierId()));
        }
        entity.setSupplier(supplier);
    }

    private PartResponse toResponse(Part entity) {
        boolean belowMinimum = entity.getQuantityOnHand().compareTo(entity.getMinQuantity()) < 0;
        return new PartResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getUnit(),
                entity.getQuantityOnHand(),
                entity.getMinQuantity(),
                belowMinimum,
                entity.getSupplier() != null ? entity.getSupplier().getId() : null,
                entity.getSupplier() != null ? entity.getSupplier().getName() : null
        );
    }
}
