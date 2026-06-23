package com.anushka.analysis_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    

    private ResponseEntity<Map<String,Object>> buildErrorResponse(String message, HttpStatus status){
        Map<String,Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return new ResponseEntity<>(response, status);
        
    }

    @ExceptionHandler(AnalysisNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleAnalysisNotFoundException(AnalysisNotFoundException e){
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleException(Exception e){
        return buildErrorResponse("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FeignClientException.class)
    public ResponseEntity<Map<String,Object>> handleFeignClientException(FeignClientException e){
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_GATEWAY);
    }

}