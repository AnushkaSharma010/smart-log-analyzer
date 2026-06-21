package com.anushka.log_service.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "log_metadata")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogMetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFilePath;

    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    private String uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogStatus status;

    public enum LogStatus{
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    };
    
}
