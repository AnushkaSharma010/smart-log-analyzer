package com.anushka.log_service.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.anushka.log_service.dto.LogMetaDataDTO;
import com.anushka.log_service.dto.LogUploadResponse;
import com.anushka.log_service.entity.LogMetaData;
import com.anushka.log_service.exception.LogNotFoundException;
import com.anushka.log_service.repository.LogMetaDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogService {

    private final LogMetaDataRepository repository;
    private final LogStorageService storageService;


    private LogMetaDataDTO mapToDTO(LogMetaData metadata) {
        return LogMetaDataDTO.builder()
                .id(metadata.getId())
                .originalFileName(metadata.getOriginalFileName())
                .storedFilePath(metadata.getStoredFilePath())
                .fileSize(metadata.getFileSize())
                .uploadTime(metadata.getUploadTime())
                .status(metadata.getStatus().name())
                .build();
    }

    @Transactional
    public LogUploadResponse uploadLog (MultipartFile file, String uploadedBy){
        String storedPath = storageService.storeFile(file);
        // Create log metadata and save it to the repository
        LogMetaData metaData = LogMetaData.builder()
        .originalFileName(file.getOriginalFilename())
        .storedFilePath(storedPath)
        .fileSize(file.getSize())
        .uploadTime(LocalDateTime.now())
        .uploadedBy(uploadedBy)
        .status(LogMetaData.LogStatus.PENDING)
        .build();

        LogMetaData saved = repository.save(metaData);
        log.info("Log metadata saved with ID: {}", saved.getId());

        return LogUploadResponse.builder()
        .id(saved.getId())
        .originalFileName(saved.getOriginalFileName())
        .status(saved.getStatus().name())
        .message("File uploaded successfully!!")
        .build();
    }

    public LogMetaDataDTO getLogById(Long id){
        LogMetaData metadata = repository.findById(id).orElseThrow(() -> new RuntimeException("Log not found with id: " + id));
        return mapToDTO(metadata);
    }

    public Page<LogMetaDataDTO> getAllLogs(Pageable pageable){
        return repository.findAllByOrderByUploadTimeDesc(pageable).map(this::mapToDTO);
    }

     @Transactional
    public void updateStatus(Long id, LogMetaData.LogStatus status) {
        LogMetaData metadata = repository.findById(id)
                .orElseThrow(() -> new LogNotFoundException("Log not found with id: " + id));
        metadata.setStatus(status);
        repository.save(metadata);
        log.info("Updated status for log id {} to {}", id, status);
    }

    @Transactional
public void deleteLog(Long id) {
    LogMetaData metadata = repository.findById(id)
            .orElseThrow(() -> new LogNotFoundException("Log not found with id: " + id));

    storageService.deleteFile(metadata.getStoredFilePath());
    repository.deleteById(id);
    log.info("Deleted log with id: {}", id);
}

    
}
