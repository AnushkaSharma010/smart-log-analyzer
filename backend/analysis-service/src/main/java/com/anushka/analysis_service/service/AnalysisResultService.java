package com.anushka.analysis_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.anushka.analysis_service.ai.RootCauseAnalyzer;
import com.anushka.analysis_service.client.LogServiceClient;
import com.anushka.analysis_service.document.AnalysisResult;
import com.anushka.analysis_service.dto.AnalysisResultDTO;
import com.anushka.analysis_service.dto.ExceptionSummary;
import com.anushka.analysis_service.dto.LogMetaDataDTO;
import com.anushka.analysis_service.exception.AnalysisNotFoundException;
import com.anushka.analysis_service.exception.FeignClientException;
import com.anushka.analysis_service.parser.LogFileReader;
import com.anushka.analysis_service.repository.AnalysisResultRepository;
import com.anushka.analysis_service.strategy.LogParsingStrategy;
import com.anushka.analysis_service.strategy.ParsedLogResult;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisResultService {

    private final AnalysisResultRepository repository;
    private final LogServiceClient logServiceClient;
    private final LogFileReader logFileReader;
    private final LogParsingStrategy logParsingStrategy;
    private final RootCauseAnalyzer rootCauseAnalyzer;
    private final ResilientLogServiceCaller resilientLogServiceCaller;
    
    private AnalysisResultDTO mapToDTO(AnalysisResult result){
        List<ExceptionSummary> summary = result.getExceptions() == null ? List.of() :
        result.getExceptions().stream().map(e -> ExceptionSummary.builder()
        .type(e.getType())
        .message(e.getMessage())
        .count(e.getCount())
        .firstOccurrence(e.getFirstOccurrence())
        .sampleStackTrace(e.getSampleStackTrace())
        .build())
        .collect(Collectors.toList());

        return AnalysisResultDTO.builder()
        .id(result.getId())
        .logId(result.getLogId())
        .totalErrors(result.getTotalErrors())
        .totalInfos(result.getTotalInfos())
        .totalWarnings(result.getTotalWarnings())
        .exceptions(summary)
        .severityBreakdown(result.getSeverityBreakdown())
        .analysisStrategy(result.getAnalysisStrategy())
        .analysisAt(result.getAnalysisAt())
        .aiInsight(result.getAiInsight())
        .build();
    }

     private LogMetaDataDTO fetchLogMetadata(Long logId) {
        try {
            return logServiceClient.getLogById(logId);
        } catch (FeignException.NotFound e) {
            throw new FeignClientException("Log not found in Log Service with id: " + logId, e);
        } catch (FeignException e) {
            throw new FeignClientException("Failed to communicate with Log Service for id: " + logId, e);
        }
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

    public AnalysisResultDTO analyzeLog(Long logId){
        Optional<AnalysisResult> existing = repository.findByLogId(logId);
    if (existing.isPresent()) {
        log.info("Analysis already exists for logId: {} — returning cached result", logId);
        return mapToDTO(existing.get());
    }
    return performAnalysis(logId);
    }

    public AnalysisResultDTO reanalyzeLog(Long logId) {
    repository.findByLogId(logId).ifPresent(existing -> {
        log.info("Forcing re-analysis for logId: {} — removing previous result", logId);
        repository.delete(existing);
    });

    return performAnalysis(logId);
}

    private AnalysisResultDTO performAnalysis(Long logId) {
        LogMetaDataDTO logMetadata = resilientLogServiceCaller.fetchLogMetadata(logId);
        log.info("Starting analysis for logId: {} (file: {})", logId, logMetadata.getOriginalFileName());

        List<String> logLines = logFileReader.readLines(logMetadata.getStoredFilePath());
        ParsedLogResult parsedResult = logParsingStrategy.parse(logLines);
        String aiInsight = rootCauseAnalyzer.generateInsight(parsedResult);

        AnalysisResult result = AnalysisResult.builder()
                .logId(logId)
                .totalErrors(parsedResult.getTotalErrors())
                .totalWarnings(parsedResult.getTotalWarnings())
                .totalInfos(parsedResult.getTotalInfos())
                .exceptions(parsedResult.getExceptions().stream()
                        .map(e -> AnalysisResult.ExceptionDetail.builder()
                                .type(e.getType())
                                .message(e.getMessage())
                                .count(e.getCount())
                                .firstOccurrence(e.getFirstOccurrence())
                                .sampleStackTrace(e.getSampleStackTrace())
                                .build())
                        .toList())
                .severityBreakdown(parsedResult.getSeverityBreakdown())
                .analysisStrategy(logParsingStrategy.getStrategyName())
                .analysisAt(LocalDateTime.now())
                .aiInsight(aiInsight)
                .build();

        AnalysisResult saved = repository.save(result);
        log.info("Analysis saved for logId: {} — {} errors, {} warnings, {} unique exceptions",
                logId, parsedResult.getTotalErrors(), parsedResult.getTotalWarnings(),
                parsedResult.getExceptions().size());

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
