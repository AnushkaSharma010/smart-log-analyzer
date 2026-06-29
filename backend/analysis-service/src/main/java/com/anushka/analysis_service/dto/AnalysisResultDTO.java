package com.anushka.analysis_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisResultDTO {

    private String id;
    private Long logId;
    private int totalErrors;
    private int totalWarnings;
    private int totalInfos;
    private List<ExceptionSummary> exceptions;
    private Map<String,Integer> severityBreakdown;
    private String analysisStrategy;
    private LocalDateTime analysisAt;
    private String aiInsight;
    
}
