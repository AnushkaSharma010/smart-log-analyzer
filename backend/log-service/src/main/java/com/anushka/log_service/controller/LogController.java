package com.anushka.log_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.anushka.log_service.dto.LogMetaDataDTO;
import com.anushka.log_service.dto.LogUploadResponse;
import com.anushka.log_service.entity.LogMetaData;
import com.anushka.log_service.service.LogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    
    private final LogService logService;

    @PostMapping("/upload")
    public ResponseEntity<LogUploadResponse> uploadLog(@RequestParam("file") MultipartFile file, @RequestParam(value = "uploadedBy", defaultValue = "anonymous") String uploadedBy){
        LogUploadResponse response = logService.uploadLog(file, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogMetaDataDTO> getLogById(@PathVariable("id") Long id){
        LogMetaDataDTO log = logService.getLogById(id);
        return ResponseEntity.ok(log);
    }

    @GetMapping
    public ResponseEntity<Page<LogMetaDataDTO>> getAllLogs(Pageable pageable){
        Page<LogMetaDataDTO> logs = logService.getAllLogs(pageable);
        return ResponseEntity.ok(logs);
    }

     @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") LogMetaData.LogStatus status) {

        logService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable("id") Long id) {
        logService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }
}
