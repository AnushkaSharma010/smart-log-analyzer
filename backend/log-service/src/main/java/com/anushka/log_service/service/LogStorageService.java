package com.anushka.log_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.anushka.log_service.exception.InvalidFileException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogStorageService {

    @Value("${log.storage.base-path}")
    private String basePath;

    public String storeFile(MultipartFile file) {
        validateFile(file);

        try{
            Path uploadDir = Paths.get(basePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadDir.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully at: {}", targetPath);
            return targetPath.toString();

        } catch (IOException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("Failed to store file", e);
        }
    }
     // Capped at 100MB: current parser (Analysis Service) reads the full file into
        // memory via Files.readAllLines(). Beyond this size, heap pressure under
        // concurrent uploads becomes a real risk on a single-instance JVM. The next
        // architectural step for genuinely larger files is streaming line-by-line
        // parsing instead of full in-memory loading.
        private static final long maxSizeBytes = 100 * 1024 * 1024; // 100 MB
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.endsWith(".log") || fileName.endsWith(".txt"))) {
            throw new InvalidFileException("Only .log and .txt files are supported");
        }

       
        if (file.getSize() > maxSizeBytes) {
            double uploadedMB = file.getSize() / (1024.0 * 1024.0);
        double limitMB = maxSizeBytes / (1024.0 * 1024.0);
        throw new InvalidFileException(
                String.format("File size (%.1fMB) exceeds the maximum supported limit of %.0fMB. " +
                                "Please upload a smaller file or split large logs before uploading.",
                        uploadedMB, limitMB)
        );
        }
    }
    
    public void deleteFile(String filePath) {
    try {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
        log.info("Deleted file from disk: {}", filePath);
    } catch (IOException e) {
        log.error("Failed to delete file: {}", filePath, e);
    }
}
    
}
