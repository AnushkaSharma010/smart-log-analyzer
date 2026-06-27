package com.anushka.analysis_service.ai;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.anushka.analysis_service.strategy.ParsedLogResult;

@Component
public class PromptBuilder {

    public String buildRootCausePrompt(ParsedLogResult parsedResult){
        
        String exceptionSummary = parsedResult.getExceptions().stream().map(e -> String.format("- %s (occurred %d times, first seen at %s)", e.getType(),e.getCount(),e.getFirstOccurrence())).collect(Collectors.joining("\n"));
        String stackTraceSample = parsedResult.getStackTraces().isEmpty()? "No stack trace found" : parsedResult.getStackTraces().get(0);
        
        return """
                You are a senior backend engineer reviewing application log analysis results.

                Summary of errors found:
                - Total errors: %d
                - Total warnings: %d
                - Total info logs: %d

                Exception types detected:
                %s

                Sample stack trace (most relevant occurrence):
                %s

                Based on this information, provide:
                1. The most likely root cause of the primary exception (2-3 sentences max)
                2. One concrete code-level fix suggestion (specific, not generic advice)

                Keep your entire response under 120 words. Be direct and technical, no preamble.
                """.formatted(
                        parsedResult.getTotalErrors(),
                        parsedResult.getTotalWarnings(),
                        parsedResult.getTotalInfos(),
                        exceptionSummary,
                        stackTraceSample
                );
    }
    
}
