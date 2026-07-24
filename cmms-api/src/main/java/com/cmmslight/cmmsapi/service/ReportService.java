package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.domain.MaintenancePlan;
import com.cmmslight.cmmsapi.domain.Part;
import com.cmmslight.cmmsapi.domain.WorkOrder;
import com.cmmslight.cmmsapi.repository.MaintenancePlanRepository;
import com.cmmslight.cmmsapi.repository.PartRepository;
import com.cmmslight.cmmsapi.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Geracao de relatorios agendados localmente, salvos em disco como CSV
 * (sem envio por e-mail ou upload para servico externo).
 */
@Service
public class ReportService {

    private final Path reportsDir;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenancePlanRepository maintenancePlanRepository;
    private final MaintenancePlanService maintenancePlanService;
    private final PartRepository partRepository;

    public ReportService(@Value("${cmms.reports.directory:./data/reports}") String reportsDirectory,
                          WorkOrderRepository workOrderRepository,
                          MaintenancePlanRepository maintenancePlanRepository,
                          MaintenancePlanService maintenancePlanService,
                          PartRepository partRepository) {
        this.reportsDir = Path.of(reportsDirectory).toAbsolutePath().normalize();
        this.workOrderRepository = workOrderRepository;
        this.maintenancePlanRepository = maintenancePlanRepository;
        this.maintenancePlanService = maintenancePlanService;
        this.partRepository = partRepository;
        try {
            Files.createDirectories(this.reportsDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel criar o diretorio de relatorios: " + this.reportsDir, e);
        }
    }

    /** Roda todo dia as 06:00, gerando o resumo operacional do dia. */
    @Scheduled(cron = "0 0 6 * * *")
    public void scheduledDailySummary() {
        generateDailySummary();
    }

    public String generateDailySummary() {
        String fileName = "resumo-diario-" + LocalDate.now() + ".csv";
        Path target = reportsDir.resolve(fileName);

        StringBuilder sb = new StringBuilder();
        sb.append("secao,chave,valor\n");

        List<WorkOrder> openOrders = workOrderRepository.findByStatus(WorkOrder.Status.OPEN);
        sb.append("os_abertas,total,").append(openOrders.size()).append("\n");
        for (WorkOrder wo : openOrders) {
            sb.append("os_abertas,").append(csv(wo.getCode())).append(",").append(csv(wo.getAsset().getName())).append("\n");
        }

        long overduePlans = maintenancePlanRepository.findByActiveTrue().stream()
                .filter(p -> p.getFrequencyType() == MaintenancePlan.FrequencyType.TIME)
                .filter(p -> {
                    Instant due = maintenancePlanService.computeNextDueAt(p);
                    return due != null && due.isBefore(Instant.now());
                })
                .count();
        sb.append("planos_vencidos,total,").append(overduePlans).append("\n");

        List<Part> lowStock = partRepository.findBelowMinimum();
        sb.append("estoque_baixo,total,").append(lowStock.size()).append("\n");
        for (Part p : lowStock) {
            sb.append("estoque_baixo,").append(csv(p.getCode())).append(",").append(p.getQuantityOnHand()).append("\n");
        }

        try {
            Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar relatorio", e);
        }
        return fileName;
    }

    public List<String> listReports() {
        try {
            return Files.list(reportsDir)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path resolve(String fileName) {
        Path resolved = reportsDir.resolve(fileName).normalize();
        if (!resolved.startsWith(reportsDir)) {
            throw new IllegalArgumentException("Caminho de relatorio invalido");
        }
        return resolved;
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
