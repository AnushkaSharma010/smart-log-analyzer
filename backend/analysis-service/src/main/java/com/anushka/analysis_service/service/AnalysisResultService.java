package com.anushka.analysis_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.anushka.analysis_service.document.AnalysisResult;
import com.anushka.analysis_service.dto.AnalysisResultDTO;
import com.anushka.analysis_service.dto.ExceptionSummary;
import com.anushka.analysis_service.exception.AnalysisNotFoundException;
import com.anushka.analysis_service.repository.AnalysisResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultService {

    private final AnalysisResultRepository repository;
    
    private AnalysisResultDTO mapToDTO(AnalysisResult result){
        List<ExceptionSummary> summary = result.getExceptions() == null ? List.of() :
        result.getExceptions().stream().map(e -> ExceptionSummary.builder()
        .type(e.getType())
        .count(e.getCount())
        .firstOccurrence(e.getFirstOccurrence())
        .build())
        .collect(Collectors.toList());

        return AnalysisResultDTO.builder()
        .id(result.getId())
        .logId(result.getLogId())
        .totalErrors(result.getTotalErrors())
        .totalInfos(result.getTotalInfos())
        .totalWarnings(result.getTotalWarnings())
        .stackTraces(result.getStackTraces())
        .exceptions(summary)
        .severityBreakdown(result.getSeverityBreakdown())
        .analysisStrategy(result.getAnalysisStrategy())
        .analysisAt(result.getAnalysisAt())
        .build();
    }

   public AnalysisResultDTO getByLogId(Long logId) {
        AnalysisResult result = repository.findByLogId(logId)
                .orElseThrow(() -> new AnalysisNotFoundException(
                        "No analysis found for logId: " + logId));

        return mapToDTO(result);
    }

    public boolean isAlreadyAnalyzed(Long logId){
        return repository.existsByLogId(logId);
    }

    /**
     * TEMPORARY placeholder method for Phase 4.
     * Real parsing logic (Strategy Pattern) replaces this in Phase 6.
     * For now, this proves the MongoDB write path works end-to-end.
     */
    public AnalysisResultDTO createPlaceholderAnalysis(Long logId) {
        AnalysisResult result = AnalysisResult.builder()
                .logId(logId)
                .totalErrors(2)
                .totalWarnings(1)
                .totalInfos(5)
                .exceptions(List.of(
                        AnalysisResult.ExceptionDetail.builder()
                                .type("NullPointerException")
                                .count(2)
                                .firstOccurrence("2026-06-18T10:16:02")
                                .build()
                ))
                .stackTraces(List.of(
                        "java.lang.NullPointerException at OrderService.java:142"
                ))
                .severityBreakdown(Map.of("ERROR", 2, "WARN", 1, "INFO", 5))
                .analysisStrategy("PLACEHOLDER")
                .analysisAt(LocalDateTime.now())
                .build();

        AnalysisResult saved = repository.save(result);
        log.info("Placeholder analysis created for logId: {}", logId);

        return mapToDTO(saved);
    }

    public void deleteByLogId(Long logId) {
        AnalysisResult result = repository.findByLogId(logId)
                .orElseThrow(() -> new AnalysisNotFoundException(
                        "No analysis found for logId: " + logId));

        repository.delete(result);
        log.info("Deleted analysis for logId: {}", logId);
    }

}
