package com.anushka.log_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogUploadResponse {
    private Long id;
    private String originalFileName;
    private String status;
    private String message;
    
}
