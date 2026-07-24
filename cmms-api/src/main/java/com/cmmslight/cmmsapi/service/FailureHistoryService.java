package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.Asset;
import com.cmmslight.cmmsapi.domain.FailureHistory;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.dto.AssetReliabilityStats;
import com.cmmslight.cmmsapi.dto.FailureHistoryRequest;
import com.cmmslight.cmmsapi.dto.FailureHistoryResponse;
import com.cmmslight.cmmsapi.exception.NotFoundException;
import com.cmmslight.cmmsapi.repository.AssetRepository;
import com.cmmslight.cmmsapi.repository.FailureHistoryRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FailureHistoryService {

    private final FailureHistoryRepository failureHistoryRepository;
    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;

    public FailureHistoryService(FailureHistoryRepository failureHistoryRepository,
                                  AssetRepository assetRepository,
                                  WorkOrderRepository workOrderRepository) {
        this.failureHistoryRepository = failureHistoryRepository;
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public List<FailureHistoryResponse> findAll() {
        return failureHistoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public FailureHistoryResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<FailureHistoryResponse> findByAsset(Long assetId) {
        return failureHistoryRepository.findByAssetIdOrderByFailedAtDesc(assetId).stream()
                .map(this::toResponse)
                .toList();
    }

    public FailureHistoryResponse create(FailureHistoryRequest request) {
        FailureHistory entity = new FailureHistory();
        applyRequest(entity, request);
        return toResponse(failureHistoryRepository.save(entity));
    }

    public FailureHistoryResponse update(Long id, FailureHistoryRequest request) {
        FailureHistory entity = getOrThrow(id);
        applyRequest(entity, request);
        return toResponse(failureHistoryRepository.save(entity));
    }

    public void delete(Long id) {
        failureHistoryRepository.delete(getOrThrow(id));
    }

    FailureHistory getOrThrow(Long id) {
        return failureHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registro de falha nao encontrado: " + id));
    }

    private void applyRequest(FailureHistory entity, FailureHistoryRequest request) {
        Asset asset = assetRepository.findById(request.assetId())
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + request.assetId()));
        entity.setAsset(asset);

        WorkOrder workOrder = null;
        if (request.workOrderId() != null) {
            workOrder = workOrderRepository.findById(request.workOrderId())
                    .orElseThrow(() -> new NotFoundException("Ordem de servico nao encontrada: " + request.workOrderId()));
        }
        entity.setWorkOrder(workOrder);

        entity.setFailedAt(request.failedAt());
        entity.setResolvedAt(request.resolvedAt());
        entity.setDescription(request.description());
        entity.setRootCause(request.rootCause());
        entity.setClassification(request.classification());
        entity.setWhy1(request.why1());
        entity.setWhy2(request.why2());
        entity.setWhy3(request.why3());
        entity.setWhy4(request.why4());
        entity.setWhy5(request.why5());

        if (request.failedAt() != null && request.resolvedAt() != null) {
            entity.setDowntimeMinutes((int) Duration.between(request.failedAt(), request.resolvedAt()).toMinutes());
        } else {
            entity.setDowntimeMinutes(null);
        }
    }

    /** MTBF/MTTR por ativo, ordenado como ranking de Pareto (mais falhas primeiro). */
    public List<AssetReliabilityStats> reliabilityRanking() {
        Map<Long, List<FailureHistory>> byAsset = failureHistoryRepository.findAllByOrderByFailedAtAsc().stream()
                .collect(Collectors.groupingBy(f -> f.getAsset().getId()));

        return byAsset.entrySet().stream()
                .map(e -> buildStats(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(AssetReliabilityStats::failureCount).reversed())
                .toList();
    }

    public AssetReliabilityStats reliabilityForAsset(Long assetId) {
        List<FailureHistory> failures = failureHistoryRepository.findByAssetIdOrderByFailedAtDesc(assetId);
        return buildStats(assetId, failures);
    }

    private AssetReliabilityStats buildStats(Long assetId, List<FailureHistory> failures) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Ativo nao encontrado: " + assetId));

        BigDecimal mtbf = null;
        if (failures.size() > 1) {
            List<FailureHistory> sorted = failures.stream()
                    .sorted(Comparator.comparing(FailureHistory::getFailedAt))
                    .toList();
            Duration span = Duration.between(sorted.get(0).getFailedAt(), sorted.get(sorted.size() - 1).getFailedAt());
            double hours = span.toMinutes() / 60.0;
            mtbf = BigDecimal.valueOf(hours / (sorted.size() - 1)).setScale(2, RoundingMode.HALF_UP);
        }

        List<Integer> downtimes = failures.stream()
                .map(FailureHistory::getDowntimeMinutes)
                .filter(d -> d != null)
                .toList();
        BigDecimal mttr = null;
        if (!downtimes.isEmpty()) {
            double avgMinutes = downtimes.stream().mapToInt(Integer::intValue).average().orElse(0);
            mttr = BigDecimal.valueOf(avgMinutes / 60.0).setScale(2, RoundingMode.HALF_UP);
        }

        return new AssetReliabilityStats(asset.getId(), asset.getCode(), asset.getName(), failures.size(), mtbf, mttr);
    }

    private FailureHistoryResponse toResponse(FailureHistory entity) {
        return new FailureHistoryResponse(
                entity.getId(),
                entity.getAsset().getId(),
                entity.getAsset().getName(),
                entity.getWorkOrder() != null ? entity.getWorkOrder().getId() : null,
                entity.getFailedAt(),
                entity.getResolvedAt(),
                entity.getDowntimeMinutes(),
                entity.getDescription(),
                entity.getRootCause(),
                entity.getClassification(),
                entity.getWhy1(),
                entity.getWhy2(),
                entity.getWhy3(),
                entity.getWhy4(),
                entity.getWhy5()
        );
    }
}
