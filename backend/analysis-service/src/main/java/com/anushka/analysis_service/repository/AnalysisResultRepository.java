package com.anushka.analysis_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.anushka.analysis_service.document.AnalysisResult;

@Repository
public interface AnalysisResultRepository extends MongoRepository<AnalysisResult, String> {

    Optional<AnalysisResult> findByLogId(Long logId);

    boolean existsByLogId(Long logId);

} 
