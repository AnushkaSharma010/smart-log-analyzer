package com.anushka.log_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogMetaDataDTO {
    private Long id;
    private String originalFileName;
    private String storedFilePath;
    private Long fileSize;
    private LocalDateTime uploadTime;
    private String status;
    
}
