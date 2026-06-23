package com.anushka.analysis_service.exception;

public class FeignClientException extends RuntimeException {

    public FeignClientException(String msg){
        super(msg);
    }

    public FeignClientException(String msg, Throwable cause){
        super(msg,cause);
    }
    
}
