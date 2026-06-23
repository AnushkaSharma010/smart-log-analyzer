package com.anushka.analysis_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anushka.analysis_service.dto.AnalysisResultDTO;
import com.anushka.analysis_service.service.AnalysisResultService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisResultService analysisResultService;

    @GetMapping("/{logId}")
    public ResponseEntity<AnalysisResultDTO> getAnalysisByLogId(@PathVariable("logId") Long logId){
        return ResponseEntity.ok(analysisResultService.getByLogId(logId));

    }
    @GetMapping("/{logId}/exists")
    public ResponseEntity<Boolean> checkAnalysisExists(@PathVariable("logId") Long logId){
        return ResponseEntity.ok(analysisResultService.isAlreadyAnalyzed(logId));
    } 

    @PostMapping("/trigger/{logId}")
    public ResponseEntity<AnalysisResultDTO> triggerPlaceholderAnalysis(@PathVariable("logId") Long logId){
        AnalysisResultDTO res = analysisResultService.createPlaceholderAnalysis(logId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable("logId") Long logId){
        analysisResultService.deleteByLogId(logId);
        return ResponseEntity.noContent().build();
    }
}
