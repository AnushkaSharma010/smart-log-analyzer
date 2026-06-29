package com.anushka.analysis_service.strategy;

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
public class ParsedLogResult {

    private int totalErrors;
    private int totalWarnings;
    private int totalInfos;

    private List<ExceptionInfo> exceptions;
    private Map<String,Integer> severityBreakdown;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExceptionInfo {
        private String type;
        private String message;
        private int count;
        private String firstOccurrence;
        private String sampleStackTrace;
    }

    
}
