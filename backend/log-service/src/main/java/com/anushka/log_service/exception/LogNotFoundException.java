package com.anushka.log_service.exception;

public class LogNotFoundException extends RuntimeException {
    
    public LogNotFoundException(String message) {
        super(message);
    }
    
}
