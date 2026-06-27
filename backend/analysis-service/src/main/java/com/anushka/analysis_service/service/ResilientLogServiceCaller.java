package com.anushka.analysis_service.service;

import org.springframework.stereotype.Component;

import com.anushka.analysis_service.client.LogServiceClient;
import com.anushka.analysis_service.dto.LogMetaDataDTO;
import com.anushka.analysis_service.exception.FeignClientException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientLogServiceCaller {

    private final LogServiceClient logServiceClient;

    // Fallback method — MUST match original method's signature + an extra Throwable/Exception param
    private LogMetaDataDTO fallbackGetLog (Long logId, Throwable throwable) {
        log.error("Log Service unavailable for logId: {} — circuit breaker fallback triggered. Reason: {}",
                logId, throwable.getMessage());

        if (throwable instanceof FeignException.NotFound) {
            throw new FeignClientException("Log not found in Log Service with id: " + logId, throwable);
        }

        throw new FeignClientException(
                "Log Service is currently unavailable. Please try again shortly. (logId: " + logId + ")",
                throwable
        );
    }

    
    @CircuitBreaker(name = "logService" , fallbackMethod = "fallbackGetLog")
    @Retry(name = "logService") 
    public LogMetaDataDTO fetchLogMetadata(Long logId){
        log.debug("Callig log service for log id {}" , logId);
        return logServiceClient.getLogById(logId);
    }
    
}
