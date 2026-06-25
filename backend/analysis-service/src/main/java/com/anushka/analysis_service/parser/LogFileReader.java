package com.anushka.analysis_service.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LogFileReader {

    public List<String> readLines(String filePath){
        try {
            Path path = Paths.get(filePath);
            
            if(!Files.exists(path)){
                throw new RuntimeException("Log file not found at path: " + filePath);
            }
            List<String> lines = Files.readAllLines(path);
            return lines;
        } catch (IOException e) {
            log.error("Failed to read log file: {}", filePath, e);
            throw new RuntimeException("Failed to read log file: " + filePath, e);
        }
    }
    
}
