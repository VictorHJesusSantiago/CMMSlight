package com.cmmslight.cmmsapi.web;

import com.cmmslight.cmmsapi.dto.AssetLocationHistoryResponse;
import com.cmmslight.cmmsapi.dto.AssetMoveRequest;
import com.cmmslight.cmmsapi.dto.AssetRequest;
import com.cmmslight.cmmsapi.dto.AssetResponse;
import com.cmmslight.cmmsapi.service.AssetService;
import com.cmmslight.cmmsapi.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;
    private final QrCodeService qrCodeService;

    public AssetController(AssetService assetService, QrCodeService qrCodeService) {
        this.assetService = assetService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public List<AssetResponse> findAll() {
        return assetService.findAll();
    }

    @GetMapping(params = "roots")
    public List<AssetResponse> findRoots() {
        return assetService.findRootAssets();
    }

    @GetMapping("/{id}")
    public AssetResponse findById(@PathVariable Long id) {
        return assetService.findById(id);
    }

    @GetMapping("/{id}/children")
    public List<AssetResponse> findChildren(@PathVariable Long id) {
        return assetService.findChildren(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        return assetService.create(request);
    }

    @PutMapping("/{id}")
    public AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return assetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assetService.delete(id);
    }

    @PostMapping("/{id}/move")
    public AssetLocationHistoryResponse move(@PathVariable Long id, @Valid @RequestBody AssetMoveRequest request) {
        return assetService.moveAsset(id, request);
    }

    @GetMapping("/{id}/location-history")
    public List<AssetLocationHistoryResponse> locationHistory(@PathVariable Long id) {
        return assetService.getLocationHistory(id);
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrCode(@PathVariable Long id, @RequestParam(defaultValue = "300") int size) {
        AssetResponse asset = assetService.findById(id);
        String content = qrCodeService.buildAssetQrContent(asset.code());
        return qrCodeService.generatePng(content, size);
    }
}
