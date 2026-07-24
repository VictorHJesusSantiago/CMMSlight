package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Supplier;
import com.cmmslight.cmmsapi.dto.SupplierRequest;
import com.cmmslight.cmmsapi.dto.SupplierResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SupplierResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public SupplierResponse create(SupplierRequest request) {
        Supplier entity = new Supplier();
        applyRequest(entity, request);
        return toResponse(supplierRepository.save(entity));
    }

    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier entity = getOrThrow(id);
        applyRequest(entity, request);
        return toResponse(supplierRepository.save(entity));
    }

    public void delete(Long id) {
        supplierRepository.delete(getOrThrow(id));
    }

    Supplier getOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fornecedor nao encontrado: " + id));
    }

    private void applyRequest(Supplier entity, SupplierRequest request) {
        entity.setName(request.name());
        entity.setContactName(request.contactName());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setNotes(request.notes());
    }

    private SupplierResponse toResponse(Supplier entity) {
        return new SupplierResponse(entity.getId(), entity.getName(), entity.getContactName(),
                entity.getPhone(), entity.getEmail(), entity.getNotes());
    }
}
