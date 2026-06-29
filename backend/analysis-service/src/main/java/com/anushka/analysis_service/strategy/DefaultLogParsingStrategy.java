package com.anushka.analysis_service.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DefaultLogParsingStrategy implements LogParsingStrategy {

    // Matches lines like: 2026-06-18 10:16:02 ERROR [thread-name] com.example.Class - message
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+(ERROR|WARN|INFO|DEBUG)\\s+.*"
    );

    // Matches lines like: java.lang.NullPointerException: message
    private static final Pattern EXCEPTION_PATTERN = Pattern.compile(
            "^(java\\.[a-zA-Z0-9.]+(?:Exception|Error))(?::\\s*(.*))?$"
    );

    // Matches stack trace lines like:     at com.example.Class.method(Class.java:42)
    private static final Pattern STACK_TRACE_LINE_PATTERN = Pattern.compile(
            "^\\s+at\\s+.*"
    );

     private static class ExceptionAggregate {
        String type;
        String firstMessage;
        String firstOccurrence;
        String firstStackTrace;
        int count = 0;
    }

    @Override
    public ParsedLogResult parse(List<String> logLines) {
        int errorCount =0;
        int warnCount =0;
        int infoCount =0;
        
        // Key = exception type, value = running aggregate for that type
        Map<String, ExceptionAggregate> exceptionAggregates = new LinkedHashMap<>();
       
        String currentTimeStamp = null;
        String currentExceptionType = null;
        String currentExceptionMessage = null;
        StringBuilder currentStackTrace = null;

         for (String line : logLines) {
            Matcher logLineMatcher = LOG_LINE_PATTERN.matcher(line);

            if (logLineMatcher.matches()) {
                flushCurrentException(exceptionAggregates, currentExceptionType,
                        currentExceptionMessage, currentTimeStamp, currentStackTrace);
                currentStackTrace = null;
                currentExceptionType = null;

                currentTimeStamp = logLineMatcher.group(1);
                String severity = logLineMatcher.group(2);

                switch (severity) {
                    case "ERROR" -> errorCount++;
                    case "WARN" -> warnCount++;
                    case "INFO" -> infoCount++;
                    default -> { }
                }

            } else if (STACK_TRACE_LINE_PATTERN.matcher(line).matches() && currentStackTrace != null) {
                currentStackTrace.append("\n").append(line);

            } else {
                Matcher exceptionMatcher = EXCEPTION_PATTERN.matcher(line.trim());
                if (exceptionMatcher.matches()) {
                    // A new exception starts — flush whatever exception we were building before
                    flushCurrentException(exceptionAggregates, currentExceptionType,
                            currentExceptionMessage, currentTimeStamp, currentStackTrace);

                    currentExceptionType = exceptionMatcher.group(1);
                    currentExceptionMessage = exceptionMatcher.group(2);
                    currentStackTrace = new StringBuilder(line);
                }
            }
        }

        // Flush whatever was being built at end of file
        flushCurrentException(exceptionAggregates, currentExceptionType,
                currentExceptionMessage, currentTimeStamp, currentStackTrace);

        List<ParsedLogResult.ExceptionInfo> exceptions = exceptionAggregates.values().stream()
                .map(agg -> ParsedLogResult.ExceptionInfo.builder()
                        .type(agg.type)
                        .message(agg.firstMessage)
                        .count(agg.count)
                        .firstOccurrence(agg.firstOccurrence)
                        .sampleStackTrace(agg.firstStackTrace)
                        .build())
                .toList();

        Map<String, Integer> severityBreakdown = new LinkedHashMap<>();
        severityBreakdown.put("ERROR", errorCount);
        severityBreakdown.put("WARN", warnCount);
        severityBreakdown.put("INFO", infoCount);

        log.info("Parsing complete: {} errors, {} warnings, {} infos, {} unique exception types",
                errorCount, warnCount, infoCount, exceptions.size());

        return ParsedLogResult.builder()
                .totalErrors(errorCount)
                .totalWarnings(warnCount)
                .totalInfos(infoCount)
                .exceptions(exceptions)
                .severityBreakdown(severityBreakdown)
                .build();
    }

    private void flushCurrentException(Map<String, ExceptionAggregate> aggregates,
                                        String type, String message,
                                        String timestamp, StringBuilder stackTrace) {
        if (type == null) {
            return;
        }

        ExceptionAggregate agg = aggregates.computeIfAbsent(type, t -> {
            ExceptionAggregate newAgg = new ExceptionAggregate();
            newAgg.type = t;
            newAgg.firstMessage = message;
            newAgg.firstOccurrence = timestamp;
            newAgg.firstStackTrace = stackTrace != null ? stackTrace.toString() : null;
            return newAgg;
        });

        agg.count++;
    }
    

    @Override
    public String getStrategyName() {
        return "DEFAULT";
    }
    
}
