package com.cmmslight.cmmsapi.service.storage;

import com.cmmslight.cmmsapi.config.FileStorageProperties;
import com.cmmslight.cmmsapi.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

/** Armazena arquivos em disco local, organizados por entidade/ano/mes. Sem dependencia de cloud. */
@Service
public class FileStorageService {

    private final Path baseDir;

    public FileStorageService(FileStorageProperties properties) {
        this.baseDir = Path.of(properties.getBaseDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Nao foi possivel criar o diretorio de armazenamento: " + baseDir, e);
        }
    }

    /** Grava o arquivo em disco e retorna o caminho relativo (para persistir no banco). */
    public String store(String subFolder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Arquivo vazio ou nao enviado");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "arquivo" : file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new ValidationException("Nome de arquivo invalido");
        }
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot);
        }
        LocalDate today = LocalDate.now();
        String relativeDir = subFolder + "/" + today.getYear() + "/" + String.format("%02d", today.getMonthValue());
        Path targetDir = baseDir.resolve(relativeDir).normalize();

        if (!targetDir.startsWith(baseDir)) {
            throw new ValidationException("Caminho de destino invalido");
        }

        try {
            Files.createDirectories(targetDir);
            String storedFileName = UUID.randomUUID() + extension;
            Path targetPath = targetDir.resolve(storedFileName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return relativeDir + "/" + storedFileName;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar arquivo em disco", e);
        }
    }

    public Path resolve(String relativePath) {
        Path resolved = baseDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new ValidationException("Caminho de arquivo invalido");
        }
        return resolved;
    }

    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolve(relativePath));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao remover arquivo", e);
        }
    }
}
