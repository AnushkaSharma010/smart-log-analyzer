package com.anushka.analysis_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.anushka.analysis_service.dto.LogMetaDataDTO;

@FeignClient(name = "log-service")
public interface LogServiceClient {

    @GetMapping("/api/logs/{id}")
    LogMetaDataDTO getLogById(@PathVariable("id") Long id);
}
