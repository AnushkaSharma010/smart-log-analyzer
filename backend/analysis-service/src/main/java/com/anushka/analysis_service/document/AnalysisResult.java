package com.anushka.analysis_service.document;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "analysis_results")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    private String id;

    @Indexed
    private Long logId;

    private int totalErrors;
    private int totalWarnings;
    private int totalInfos;

    private List<ExceptionDetail> exceptions;
    private List<String> stackTraces;
    private Map<String,Integer> severityBreakdown;

    private String analysisStrategy;
    private LocalDateTime analysisAt;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExceptionDetail {
        private String type;
        private int count;
        private String firstOccurrence;
    }
    
}
