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

    @Override
    public ParsedLogResult parse(List<String> logLines) {
        int errorCount =0;
        int warnCount =0;
        int infoCount =0;

        Map<String,Integer> exceptionCount = new LinkedHashMap<>();
        Map<String,String> exceptionFirstOccurrence = new HashMap<>();
        List<String> stackTrace = new ArrayList<>();

        String currentTimeStamp = null;
        StringBuilder currentStackTrace = null;

         for (String line : logLines) {
            Matcher logLineMatcher = LOG_LINE_PATTERN.matcher(line);

            if (logLineMatcher.matches()) {
                // A new log entry starts — flush any in-progress stack trace first
                flushStackTrace(currentStackTrace, stackTrace);
                currentStackTrace = null;

                currentTimeStamp = logLineMatcher.group(1);
                String severity = logLineMatcher.group(2);

                switch (severity) {
                    case "ERROR" -> errorCount++;
                    case "WARN" -> warnCount++;
                    case "INFO" -> infoCount++;
                    default -> { /* DEBUG and others not counted separately for now */ }
                }

            } else if (STACK_TRACE_LINE_PATTERN.matcher(line).matches() && currentStackTrace != null) {
                // Continuation of an existing stack trace
                currentStackTrace.append("\n").append(line);

            } else {
                Matcher exceptionMatcher = EXCEPTION_PATTERN.matcher(line.trim());
                if (exceptionMatcher.matches()) {
                    String exceptionType = exceptionMatcher.group(1);
                    exceptionCount.merge(exceptionType, 1, Integer::sum);
                    exceptionFirstOccurrence.putIfAbsent(exceptionType, currentTimeStamp);

                    flushStackTrace(currentStackTrace, stackTrace);
                    currentStackTrace = new StringBuilder(line);
                }
            }
        }

        flushStackTrace(currentStackTrace, stackTrace);

        List<ParsedLogResult.ExceptionInfo> exceptions = exceptionCount.entrySet().stream()
                .map(entry -> ParsedLogResult.ExceptionInfo.builder()
                        .type(entry.getKey())
                        .count(entry.getValue())
                        .firstOccurrence(exceptionFirstOccurrence.get(entry.getKey()))
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
                .stackTraces(stackTrace)
                .severityBreakdown(severityBreakdown)
                .build();
    }

    private void flushStackTrace(StringBuilder currentStackTrace, List<String> stackTraces) {
        if (currentStackTrace != null && currentStackTrace.length() > 0) {
            stackTraces.add(currentStackTrace.toString());
        }
    }
    

    @Override
    public String getStrategyName() {
        return "DEFAULT";
    }
    
}
