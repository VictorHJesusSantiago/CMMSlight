package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.AssetTypeRequest;
import com.cmmslight.cmmsapi.dto.AssetTypeResponse;
import com.cmmslight.cmmsapi.service.AssetTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-types")
public class AssetTypeController {

    private final AssetTypeService assetTypeService;

    public AssetTypeController(AssetTypeService assetTypeService) {
        this.assetTypeService = assetTypeService;
    }

    @GetMapping
    public List<AssetTypeResponse> findAll() {
        return assetTypeService.findAll();
    }

    @GetMapping("/{id}")
    public AssetTypeResponse findById(@PathVariable Long id) {
        return assetTypeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetTypeResponse create(@Valid @RequestBody AssetTypeRequest request) {
        return assetTypeService.create(request);
    }

    @PutMapping("/{id}")
    public AssetTypeResponse update(@PathVariable Long id, @Valid @RequestBody AssetTypeRequest request) {
        return assetTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assetTypeService.delete(id);
    }
}
