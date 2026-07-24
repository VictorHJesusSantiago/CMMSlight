package com.cmmslight.cmmsapi.service;

import com.cmmslight.cmmsapi.config.BackupProperties;
import com.cmmslight.cmmsapi.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Backup e restauracao do banco Postgres via pg_dump/psql, executados localmente
 * (sem envio para nenhum servico de armazenamento externo).
 */
@Service
public class BackupService {

    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final BackupProperties properties;
    private final Path backupDir;
    private final String datasourcePassword;

    public BackupService(BackupProperties properties, @Value("${spring.datasource.password}") String datasourcePassword) {
        this.properties = properties;
        this.datasourcePassword = datasourcePassword;
        this.backupDir = Path.of(properties.getDirectory()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel criar o diretorio de backups: " + backupDir, e);
        }
    }

    /** Roda toda semana, domingo as 03:00 (horario do servidor). */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void scheduledBackup() {
        runBackup();
    }

    public String runBackup() {
        String fileName = "backup-" + LocalDateTime.now().format(FILE_FMT) + ".sql";
        Path target = backupDir.resolve(fileName);

        List<String> command = new ArrayList<>(List.of(
                properties.getPgDumpPath(),
                "-h", properties.getHost(),
                "-p", properties.getPort(),
                "-U", properties.getUsername(),
                "-d", properties.getDatabase(),
                "-f", target.toString(),
                "--no-password"
        ));
        runProcess(command);
        return fileName;
    }

    public void restore(String fileName) {
        Path source = backupDir.resolve(fileName).normalize();
        if (!source.startsWith(backupDir) || !Files.exists(source)) {
            throw new ValidationException("Arquivo de backup nao encontrado: " + fileName);
        }
        List<String> command = List.of(
                properties.getPsqlPath(),
                "-h", properties.getHost(),
                "-p", properties.getPort(),
                "-U", properties.getUsername(),
                "-d", properties.getDatabase(),
                "-f", source.toString()
        );
        runProcess(command);
    }

    public List<String> listBackups() {
        try (Stream<Path> stream = Files.list(backupDir)) {
            return stream.filter(p -> p.toString().endsWith(".sql"))
                    .map(p -> p.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void runProcess(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.environment().put("PGPASSWORD", datasourcePassword);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ValidationException("Comando de backup/restore falhou (codigo " + exitCode + "): " + output);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao executar processo de backup/restore. Verifique se pg_dump/psql estao instalados e no PATH.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ValidationException("Processo de backup/restore interrompido");
        }
    }
}
